package com.example.einzelbeispiel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity{
    //declare vars, consts, objects ...
    public static final int SERVERPORT = 53212;
    public static final String SERVERIP = "se2-isys.aau.at";
    private ClientThread clientThread = new ClientThread();
    private ModuloThread moduloThread = new ModuloThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //NETWORKING PART
        Button sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText numImput = findViewById(R.id.numImput);
                TextView resOutput = findViewById(R.id.resOutput);
                Button sendBtn = findViewById(R.id.sendBtn);
                sendBtn.setClickable(false);

                if(numImput.getText().toString().length() > 0) {
                    int num = Integer.parseInt(numImput.getText().toString());
                    String numString = num + "";

                    if (!numString.isEmpty()) {
                        Thread t1 = new Thread(clientThread);
                        t1.start();
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            Log.i("error", "asd");
                        }

                        String serverResponse = clientThread.getSentence();
                        Log.i("####serverResponse####", serverResponse + "");
                        resOutput.setText(serverResponse);
                    }
                } else {
                    resOutput.setText(getString(R.string.missingMatrikelNote));
                }
                sendBtn.setClickable(true);
            }
        });

        //MODULO PART
        Button calcMod = findViewById(R.id.calcMod);
        calcMod.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText numImput = findViewById(R.id.numImput);
                TextView resOutput = findViewById(R.id.resOutput);

                if(numImput.getText().toString().length() > 0) {
                    int num = Integer.parseInt(numImput.getText().toString());
                    String numString = num + "";

                    if (!numString.isEmpty()) {
                        Thread t2 = new Thread(moduloThread);
                        t2.start();
                        try {
                            t2.join();
                        } catch (InterruptedException e) {
                            Log.i("ERROR", "something went wrong" + e);
                        }
                        String modulo = moduloThread.getModulo();
                        resOutput.setText(modulo);
                    }
                } else {
                    resOutput.setText(getString(R.string.missingMatrikelNote));
                }
            }
        });

    }



    class ClientThread implements Runnable {

        private Socket cleintSocket;
        private BufferedReader input;
        private volatile String sentence;

        @Override
        public void run() {
            Log.i("INFO", "CleintThread is being called");
            try {
                cleintSocket = new Socket(SERVERIP, SERVERPORT);
                Log.i("INFO HOST", "HOST IS KNOWN" + cleintSocket);

                    EditText numImput = findViewById(R.id.numImput);
                    int num = Integer.parseInt(numImput.getText().toString());

                    this.input = new BufferedReader(new InputStreamReader(System.in));

                    String message = num+"";
                    if (!message.isEmpty()) {
                        int i = 1;
                        while(i <= 2){
                            try {
                                DataOutputStream outToServer = new DataOutputStream(cleintSocket.getOutputStream());
                                Log.i("INFO", "DataOutputStream: " + i + outToServer);
                                outToServer.writeBytes(message + '\n');
                                i++;

                                this.input = new BufferedReader(new InputStreamReader(cleintSocket.getInputStream()));
                                sentence = new String();
                                sentence = input.readLine();
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    } else {
                        Log.i("ERROR", "No number was given");
                    }
            } catch (UnknownHostException e1) {
                Log.i("INFO HOST", "HOST IS NOT KNOWN");
                e1.printStackTrace();
            } catch (IOException e1) {
                Log.i("INFO HOST", "IOException");
                e1.printStackTrace();
            }

        }

        private String getSentence(){
            return sentence+"";
        }
    }

    class ModuloThread implements Runnable{

        private volatile int modulo;

        @Override
        public void run() {
            Log.i("INFO", "ModuloThread is being called");
            try {
                EditText numImput = findViewById(R.id.numImput);
                modulo = Integer.parseInt(numImput.getText().toString());
            } catch (Error e){
                e.printStackTrace();
            }
        }



        private String getModulo(){
            //my matrikel mod 7 returns 6...so: "Matrikelnummer sortieren, wobei zuerst alle geraden dann alle ungeraden Ziffern gereiht sind"
            int lenghtOfInput = String.valueOf(modulo).length();
            String even = new String();
            String odd = new String();

            if (lenghtOfInput != 8){
                return modulo + getString(R.string.notValidMatrikelNotePt1) + lenghtOfInput + getString(R.string.notValidMatrikelNotePt2);
            } else {
                int[] arr = new int[lenghtOfInput];

                //make an array of digits
                int i = 0;
                do {
                    arr[i] = modulo % 10;
                    modulo = modulo / 10;
                    i++;
                } while (modulo != 0);

                //seperate even digits from odd ones
                for (int runner = 0; runner < lenghtOfInput; runner++){
                    if(arr[runner] % 2 == 0){
                        even += arr[runner];
                    } else {
                        odd += arr[runner];
                    }
                }

                //Sorting of the char-rays which are numbers
                char[] evenSorted = even.toCharArray();
                Arrays.sort(evenSorted);
                String evenSortedString = new String(evenSorted);
                char[] oddSorted = odd.toCharArray();
                Arrays.sort(oddSorted);
                String oddSortedString = new String(oddSorted);

                return  getString(R.string.solutionTxt) + "\n" + evenSortedString + oddSortedString;
            }
        }
    }
}