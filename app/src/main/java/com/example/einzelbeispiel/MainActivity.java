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


public class MainActivity extends AppCompatActivity{

    //new
    public static final int SERVERPORT = 53212;
    public static final String SERVERIP = "se2-isys.aau.at";
    private ClientThread clientThread = new ClientThread();
    //private SendMessage sendMessage;
    private Thread thread;

    //*

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

                //TODO: handle the case of empty sending
                if(numImput.getText().toString().length() > 0) {
                    int num = Integer.parseInt(numImput.getText().toString());
                    String numString = num + "";
                    //resOutput.setText(numString);

                    if (clientThread != null && num + "".length() > 0) {
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
                TextView resOutput = (TextView) findViewById(R.id.resOutput);
                resOutput.setText("Modulo Stuff");
            }
        });

    }



    class ClientThread implements Runnable {

        private Socket cleintSocket;
        private BufferedReader input;
        private volatile String sentence;

        @Override
        public void run() {
            Log.i("INFO", "HOST IS KNOWN");
            try {
                //InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                //socket = new Socket(serverAddr, SERVERPORT);
                cleintSocket = new Socket(SERVERIP, 53212);
                Log.i("INFO HOST", "HOST IS KNOWN" + cleintSocket);

                //while (!Thread.currentThread().isInterrupted()) {
                    EditText numImput = (EditText) findViewById(R.id.numImput);
                    int num = Integer.parseInt(numImput.getText().toString());

                    this.input = new BufferedReader(new InputStreamReader(System.in));

                    String message = num+"";
                    if (null != message && message.length() > 0) {
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
                                Log.i("INFO####", sentence);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                i++;
                            }
                            i++;
                        }

                    } else {
                        Log.i("ERROR", "No number was given");
                    }

               //}
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

        /*public void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("INFO2", "WORKS SENDMESSAGE " + message);
                    try {
                        if (cleintSocket != null) {
                            Log.i("INFO3", "WORKS SENDMESSAGE");
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(cleintSocket.getOutputStream())),
                                    true);
                            out.println(message);
                        } else {
                            Log.i("INFO", "NO SOCKET");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }*/
    }

    /*class SendMessage implements Runnable {
        String message;
        Socket clientSocket;
        public void SendMessage(final String message, final Socket clientSocket){
            this.message = message;
            this.clientSocket = clientSocket;
        }
        @Override
        public void run() {
            Log.i("INFO2", "WORKS SENDMESSAGE " + this.message);
            try {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                Log.i("INFO", "DataOutputStream: " + outToServer);
                outToServer.writeBytes(message + "\n");
                //PrintWriter out = new PrintWriter(new BufferedWriter(
                //        new OutputStreamWriter(clientSocket.getOutputStream())),
                //        true);
                //out.println(this.message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

}
