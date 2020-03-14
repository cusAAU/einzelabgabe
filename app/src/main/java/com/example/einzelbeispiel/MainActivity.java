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

        //NETWORKING STUFF
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText numImput = (EditText) findViewById(R.id.numImput);
                TextView resOutput = (TextView) findViewById(R.id.resOutput);

                if(numImput.getText().toString().length() > 0) {
                    int num = Integer.parseInt(numImput.getText().toString());
                    String numString = num + "";
                    //resOutput.setText(numString);

                    if (!numString.isEmpty()) {
                        Thread t1 = new Thread(clientThread);
                        t1.start();
                        //clientThread.sendMessage(num + "");
                        Log.i("MAIN", "CLEINT THREAD IS OK: " + t1);
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            Log.i("error", "asd");
                        }

                        String serverResponse = clientThread.getSentence();
                        Log.i("####serverResponse####", serverResponse + "");
                        resOutput.setText(serverResponse + "");
                    }
                } else {
                    resOutput.setText("You kinda forgot to put the matrikel nr.");
                }
                //KURS: tips and tricks
                // log.d(TAG, LOG)
                // NETWORK:
                // thread += new t('tasktodo'); --> t.start() calls the thread
                // button deaktivieren as soon as clicked. then activate again
            }
        });

        //MODULO STUFF
        Button calcMod = (Button) findViewById(R.id.calcMod);
        calcMod.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TextView resOutput = (TextView) findViewById(R.id.resOutput);
                //resOutput.setText("Modulo Stuff");

                EditText numImput = (EditText) findViewById(R.id.numImput);
                TextView resOutput = (TextView) findViewById(R.id.resOutput);

                if(numImput.getText().toString().length() > 0) {
                    int num = Integer.parseInt(numImput.getText().toString());
                    String numString = num + "";
                    //resOutput.setText(numString);

                    if (!numString.isEmpty()) {
                        Thread t2 = new Thread(moduloThread);
                        t2.start();
                        Log.i("MAIN", "Modulo thread is OK: " + t2);
                        try {
                            t2.join();
                        } catch (InterruptedException e) {
                            Log.i("ERROR", "something went wrong" + e);
                        }

                        String modulo = moduloThread.getModulo();

                        resOutput.setText(modulo + "");
                    }
                } else {
                    resOutput.setText("You kinda forgot to put the matrikel nr.");
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

                    EditText numImput = (EditText) findViewById(R.id.numImput);
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

        public String getSentence(){
            return sentence+"";
        }
    };

    class ModuloThread implements Runnable{

        private volatile int modulo;

        @Override
        public void run() {
            Log.i("INFO", "ModuloThread is being called");
            try {
                EditText numImput = (EditText) findViewById(R.id.numImput);
                int num = Integer.parseInt(numImput.getText().toString());

                modulo = num;

            } catch (Error e){
                e.printStackTrace();
            }
        }



        public String getModulo(){
            //my matrikel mod 7 returns 6...so: "Matrikelnummer sortieren, wobei zuerst alle geraden dann alle ungeraden Ziffern gereiht sind"
            int lenghtOfInput = String.valueOf(modulo).length();

            if (lenghtOfInput != 7){
                String response = modulo + "...is not a valid matrikel. It should contain 7 digits. This one has: " + lenghtOfInput + " digits.";
                return response;
            } else {
                int[] arr = new int[lenghtOfInput];

                //make an array of digits
                int i = 0;
                do {
                    arr[i] = modulo % 10;
                    modulo /= 10;
                    i++;
                } while (modulo != 0);

                //seperate even digits from odd ones
                String even = new String();
                String odd = new String();
                for (int runner = 0; runner < lenghtOfInput; runner++){
                    if(arr[runner] % 2 == 0){
                        even += arr[runner];
                    } else {
                        odd += arr[runner];
                    }
                }

                //Sorting of the char-rays :) which are numbers
                char[] evenSorted = even.toCharArray();
                Arrays.sort(evenSorted);
                String evenSortedString = new String(evenSorted);
                char[] oddSorted = odd.toCharArray();
                Arrays.sort(oddSorted);
                String oddSortedString = new String(oddSorted);

                return "Here, even numbers: " +
                        evenSortedString + '\n' + " And odd numbers: " +
                        oddSortedString + '\n' +
                        "Solution: " + evenSortedString + oddSortedString;
            }
        }
    }

    // wanna do a JS-like array.push()...
    private static int[] push(int[] array, int push) {
        int[] longer = new int[array.length + 1];
        for (int i = 0; i < array.length; i++)
            longer[i] = array[i];
        longer[array.length] = push;
        return longer;
    }
}
