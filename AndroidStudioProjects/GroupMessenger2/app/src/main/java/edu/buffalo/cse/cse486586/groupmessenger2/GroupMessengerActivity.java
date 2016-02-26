package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * For implementing FIFO ordering, it is necessary that we keep track of which process sent which message in which order
 * A separate queue for all processes fit these requirements perfectly, as we can search through the queue and
 * adjust any out-of-order message.
 * NOTE : This however is not optimal in terms of time complexity.
 * TODO : Look for some optimal way to do this.
 */

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final int SERVER_PORT = 10000;
    static final String TAG      = GroupMessengerActivity.class.getName();
    static final String[] REMOTE_PORT = {"11108","11112","11116","11120","11124"};
    static public String myPort  = "";
    private BufferedReader in    = null;
    private PrintWriter out      = null;
    private Socket clientSocket  = null;

    static public EditText editText = null;
    static public TextView textView = null;
    public static SQLHelperClass sql= null;
    /** Helper data structures for FIFO ordering**/
    private String deliverMessages;
    private static Map<String,String> processPortMap;
    private int dbSeqNumber      = 0; // message # inserted in DB
    private int[] deliverSeqNumber; //keeps track of maximum sequence number received,
    private int[] sentSeqNumber ;   //sent sequence number along with every message
    //sentSeqNumber[i] --> denotes the latest seq # sent OUT to process i by this process
    private LinkedList<String>[] queue;
    private Object lock; // used to synchronize between message receive & actual delivery

    /**
     * creates map of <port,processNum> to be used in sending multicast messages
     */
    private static void createPortProcessMap() {
        Map<String,String> processPortMap = new HashMap<String, String>();
        int counter = 1;
        for(String port:REMOTE_PORT) {
            processPortMap.put(port,"P" + counter);
            counter++;
        }
        for(Map.Entry<String,String> entry : processPortMap.entrySet()) {
            Log.e(TAG,entry.getKey() + " =>" + entry.getValue());
        }
        Log.e(TAG,"processPortMap size :" + processPortMap.size());
    }

    /**
     *
     */
    private void createServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket " + e.getMessage());
            return;
        }

    }

    /**
     * Looks for the proper place to insert the message
     * Proper place is decided by the recvSeqNum parameter
     * The receving buffer is maintained in ascending order of recvSeqNum
     * @param recvSeqNum
     * @param message
     * @return True if the message was inserted in between the two ends of the queue
     *         False if the message was inserted in the end
     * If true is returned, then it means
     */
    private boolean insertIntoReceiveBuffer(int pNum,int recvSeqNum,String message) {
        int prev = Integer.MIN_VALUE;
        for(int i =1 ; i < queue[pNum].size(); i++) {
            prev = Integer.valueOf( queue[pNum].get(i-1).substring(0,queue[pNum].indexOf(':')));
            if (recvSeqNum < prev) {
                queue[pNum].add(i-1,  message);
                if (prev - recvSeqNum == 1) { //potentially completes a sequence
                    if (i -1 == 0) { //definitely completes a sequence

                    }
                }
            }

        }
        queue[pNum].add(recvSeqNum + ":" + message);
        return false;
    }
    /**
     *
     * @param procNum
     * @param recvSeqNum
     * @return
     */
    private boolean checkIntoDataQueue(String procNum, int recvSeqNum, String message)  {
        int pNum = Integer.valueOf(procNum.charAt(1));
        if (recvSeqNum == deliverSeqNumber[pNum] + 1) // takes care of both the scenarios of recvSeqNum = 0, and recvSeqNum > 0
        {
            deliverSeqNumber[pNum] = recvSeqNum;
            return true;
        } else {
            insertIntoReceiveBuffer(pNum,recvSeqNum,message);
        }
        return false;
    }

    /**
     *
     * @param message
     */
    private void deliverMessage(String message) {
        Log.e(TAG,"message is :" + message);
        int posn = message.indexOf('_');

        String procNum = message.substring(0, posn);
            Log.e(TAG,"deliverMessage procNum " + procNum);
        message  = message.substring(posn + 1);
        int recvSeqNum = Integer.valueOf(message.substring(0, message.indexOf('=')));
            Log.e(TAG,"deliverMessage recvSeqNum " + recvSeqNum);
        message  = message.substring(posn + 1);
            Log.e(TAG," deliverMessage message :" + message);

        boolean bRes = checkIntoDataQueue(procNum,recvSeqNum,message);
        if (bRes) {
            if (deliverSeqNumber[Integer.valueOf(procNum)] == recvSeqNum) { // this message can be delivered
                /*
                * Check if previously buffered messages can be delivered as well
                */

                if (!queue[Integer.valueOf(procNum)].isEmpty()) {

                } else {
                    deliverMessages = message;
                }

            }
        }
        lock.notify();
    }
    /**
     *
     */
    private void createDataQueues() {
        queue = new LinkedList[REMOTE_PORT.length];
        for(int i=0; i < REMOTE_PORT.length; i++) {
            queue[i] = new LinkedList<String>();
        }
    }
    /**
     *
     */
    private void createSendButtonEvent() {
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                Log.e(TAG,"within send button msg :" + msg);
                editText.setText(""); //reset text
                //textView.append("\n" + msg); //append local message
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        sql = SQLHelperClass.getInstance(getApplicationContext());
        //TODO: Add the code for telephony here

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.e(TAG, "Local port found " + myPort);
        createServerSocket();
        createDataQueues();
        createPortProcessMap();

       // deliverSeqNumber = new int[REMOTE_PORT.length];
        deliverSeqNumber = new int[]{-1,-1,-1,-1,-1};
        sentSeqNumber    = new int[REMOTE_PORT.length];
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textViewSend);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        editText = (EditText) findViewById(R.id.editText1);
        textView = (TextView) findViewById(R.id.textViewSend);

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        createSendButtonEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            Log.e(TAG,"Within background server function");
            ServerSocket serverSocket = sockets[0];
            String readLine = "";
            boolean bTrue   = true;
            /* TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             * Referred from : Last assignment
             */
            try {
                boolean bCanAccept = true;
                while(bCanAccept) {
                    clientSocket = serverSocket.accept();
                    Log.e(TAG, "client socket accepted");
                    try {
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        readLine = in.readLine();
                        if (readLine != null) {
                            //lock.wait();
                            deliverMessage(readLine);
                            lock.wait();
                            readLine = deliverMessages; // deliver the messages
                            deliverMessages = ""; // reset
                            publishProgress(readLine);
                            updateDB(readLine);
                        }
                        in.close();

                    } catch (Exception ex) {
                        Log.e(TAG, "exception in processing client messages " + ex.getMessage());
                        bCanAccept = false;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG,"Exception occured in accepting client connections " + e.getMessage());
            }

            return null;
        }
        protected void updateDB(String message) {
            synchronized (this) {
                ContentValues cv = new ContentValues();
                cv.put("key", dbSeqNumber++);
                cv.put("value",message);
                //cv.put(message,++dbSeqNumber);
                Log.e(TAG,"to insert into db :" + message + " seq# " + dbSeqNumber);
                sql.insertValues(cv);
            }
        }
        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            GroupMessengerActivity.textView.append(strReceived + "\n");
            GroupMessengerActivity.textView.append("\n");
        }
    }
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket   = null;
                String msgToSend= null;
                OutputStream out= null;
                PrintWriter  pw = null;
                int i=0;
                for(String remotePort :REMOTE_PORT) { // bind to all the listed ports
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    //Log.e(TAG,processPortMap.get(myPort));
                   /* Log.e(TAG,"myPort in client Task :" + myPort + ":" + msgs[0] + " : " + msgSeqNumber);
                    if (GroupMessengerActivity.processPortMap == null) {
                        Log.e(TAG,"WHY ??");
                    }
                    for(Map.Entry<String,String> entry : GroupMessengerActivity.processPortMap.entrySet()) {
                        Log.e(TAG,entry.getKey() + " =>" + entry.getValue());
                    }*/

                    msgToSend = processPortMap.get(myPort) + "_" + sentSeqNumber[i]  + "=" + msgs[0] ;
                    sentSeqNumber[i]++;
                    i++;
                    //msgSeqNumber++;
                    //  Log.e(TAG,"msgToSend :" + msgToSend + " to send to remote port :" + remotePort);
                    /**
                     * Code for sending message to clients referred from SimpleMessenger application
                     */
                    out = socket.getOutputStream();
                    pw   = new PrintWriter(out);

                    pw.println(msgToSend);
                    pw.flush();
                    pw.close();
                    //Log.e(TAG,"Sending message  " + msgToSend + " at address port " + remotePort);
                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException " + e.getMessage());
            }
            return null;
        }
    }

    /**
     *
     */

}
