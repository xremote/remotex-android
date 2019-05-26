package com.fiction.remotex;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class SocketService extends Service {
    public Bitmap bmpimage = null;
    public String Result ="nothing";
    public String clientname="client";
    public String servername ="server";
    public String tryhostname = null;
    public boolean isservicerunning = false;
    public boolean isrecieving = false;
    public int  percentdownloaded = 0;
    PrintWriter out;
    InputStreamReader inputreader;
    InputStream is;
    DataInputStream dis;
    Socket socket;
    InetAddress serverAddr;
    BufferedReader br;
    private static final String TAG = "zedmessage";


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub

        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();
   // TCPClient mTcpClient = new TCPClient();

    public class LocalBinder extends Binder {
        public SocketService getService() {

            return SocketService.this;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    public void IsBoundable(){


    }



    public void recieveimage() throws IOException {

                isrecieving = true;

                 File dir = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices/temp");
            if(!dir.exists())
                dir.mkdirs();


            String outFileName = Environment.getExternalStorageDirectory() + "/Download/Remote Devices/temp/tempfile.jpg";

            File myfile = new File(outFileName);
                try{

            myfile.setWritable(true, false);
           myfile.createNewFile();

            } catch (IOException e) {

                    e.printStackTrace();

                }

            FileOutputStream myOutput = new FileOutputStream(myfile, false);
            try
            {

                myOutput = new FileOutputStream(outFileName);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }


            //transfer bytes from the inputfile to the outputfile
            int bytesrecieved = 0;
            int count = 1;
            int totalbytes = 0;
            byte[] buffer = new byte[1024 * 8];
            is.read(buffer, 0, 8);
            int length;
            int recievedl = 0;

            ByteBuffer buffer1 = ByteBuffer.wrap(buffer);
            buffer1.order(ByteOrder.LITTLE_ENDIAN);
            totalbytes = (int) buffer1.getLong();


            while (recievedl < totalbytes && (length = is.read(buffer, 0, buffer.length)) > 0) {
                myOutput.write(buffer, 0, length);
                recievedl += length;
            }

            //Close the streams

                //Log.i(TAG,totalbytes + " recieved: " + recievedl);
//            Toast.makeText(SocketService.this, recievedl + "copied" + totalbytes, Toast.LENGTH_LONG).show();

            myOutput.flush();
            myOutput.close();
         //   is.close();

        isrecieving=false;   bmpimage = BitmapFactory.decodeFile(outFileName);
            // is.close();



    }



    public void recievefile(String savename) throws IOException {


            int bytesrecieved = 0;
            int count = 1;
            int totalbytes = 0;

            File dir = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices/Downloaded");

        if(!dir.exists())
                dir.mkdirs();

            String outFileName = Environment.getExternalStorageDirectory() + "/Download/Remote Devices/Downloaded/" + savename;
            File myfile = new File(outFileName);

        try{

                myfile.setWritable(true, false);
                myfile.createNewFile();

            } catch (IOException e) {

                e.printStackTrace();

            }

            FileOutputStream myOutput = new FileOutputStream(myfile, false);
            try
            {

                myOutput = new FileOutputStream(outFileName);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }


            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024 * 128];

            is.read(buffer, 0, 8);
            int length;

            int recievedl = 0;
            ByteBuffer buffer1 = ByteBuffer.wrap(buffer);
            buffer1.order(ByteOrder.LITTLE_ENDIAN);
            totalbytes = (int) buffer1.getLong();


        if(totalbytes>0) {
            int unitpercent = totalbytes / 100;
            while (recievedl < totalbytes && (length = is.read(buffer, 0, buffer.length)) > 0) {


                myOutput.write(buffer, 0, length);

                recievedl += length;

                percentdownloaded = (int) (1.0 * recievedl / unitpercent);
            }
        }

        percentdownloaded=100;
            //Close the streams


            //Toast.makeText(SocketService.this, recievedl + "copied" + totalbytes, Toast.LENGTH_LONG).show();

            myOutput.flush();
            myOutput.close();
            //   is.close();



    }






    public void sendMessage(final String message){
        final Thread tm = new Thread() {
            @Override
            public void run() {
                Log.i(TAG,"sendmsg: "+ message);
                if (out != null && !out.checkError()) {
                    Log.i(TAG,"sendmsg2: "+ message);
                    //Toast.makeText(SocketService.this,"message",Toast.LENGTH_LONG).show();
                    try {
                        out.println(message);
                    } catch (Exception e) {
                        Log.e(TAG,"sendmsg3: "+ e);
                    }
                    out.flush();
                    Log.i(TAG,"sendmsg4: "+ message);
                }

            }
        };
        tm.start();

    }




    public String recieveMessage(){
        String line = null;


        try{
            line = br.readLine();
 }
        catch(Exception e){
         Log.i(TAG,"exe " + e);
            //Toast.makeText(SocketService.this, "exe " + e, Toast.LENGTH_LONG).show();
        }
      if(line==null)
          line ="nula";
           return line;
    }






    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);

            return START_STICKY;
    }

    public void connect(String hostname)
    {
        if(socket==null){

            tryhostname = hostname;
            Runnable connect = new connectSocket();
            new Thread(connect).start();

        }
    }

    public boolean isconnected()
    {


        try{
            if(socket == null)
                return false;
            else
                return true;
        }
        catch (Exception e){
            Log.i(TAG,"er "+e);
            return  false;
        }


    }


    public void disconnect(){

        try { socket.shutdownInput(); } catch (Exception e) { Log.i(TAG,""+e);}
        try { socket.shutdownOutput(); } catch (Exception e) { Log.i(TAG,""+e);}
        try {  socket.close(); } catch (Exception e) { Log.i(TAG,""+e);}

        socket =null;
        Log.i(TAG,"g ");
    }


    public String GetServerName()
    {
       return servername;
    }


    public String GetClientName()
    {
        return clientname;
    }


    public String checker()
    {
            return Result;
    }
    class connectSocket implements Runnable {

        @Override
        public void run() {


            int port = 2055;

           try {
               try {
                   socket = new Socket(tryhostname, port);
               }
               catch(Exception e){

                   tryhostname=null;
               }

                   InetSocketAddress clAddress = (InetSocketAddress)socket.getLocalSocketAddress();
               clientname = clAddress.getHostName();

                InetSocketAddress seAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
                servername = seAddress.getHostName();

                try {


                    //send the message to the server
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    dis =   new DataInputStream(socket.getInputStream());
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    is = socket.getInputStream();
                    inputreader = new InputStreamReader(socket.getInputStream());
                }
                catch (Exception e) {

                    Log.e("TCP", "S: Error", e);

                }
            } catch (Exception e) {

                Log.e("TCP", "C: Error", e);

            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }


}
