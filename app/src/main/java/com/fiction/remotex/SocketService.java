package com.fiction.remotex;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class SocketService extends Service {
    public Bitmap bmpimage = null;
    public String clientname = "client";
    public String servername = "server";
    public String tryhostname = null;
    public boolean isservicerunning = false;
    public int percentdownloaded = 0;
    PrintWriter output_writer;
    InputStreamReader inputreader;
    InputStream input_stream;
    Socket socket;
    BufferedReader buffer_reader;
    public boolean downloadingfile = false;
    public boolean cleaning_stream = false;
    private int heartbeat_count = 0;


    //not used as of now
    public String GetServerName() {
        return servername;
    }
    public String GetClientName() {
        return clientname;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start_heartbeat_counting();
    }

    public void start_heartbeat_counting(){

        // check if exactly one service input_stream running  using logs.. keep counting indefinitely
        final Thread heartbeat_thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    Log.e(this.getClass().toString(), heartbeat_count + "");
                    heartbeat_count++;
                    android.os.SystemClock.sleep(2000);
                }
            }
        };
        heartbeat_thread.start();
    }


    public void recieveimage() throws IOException {

        File download_directory = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices/temp");
        if (!download_directory.exists())
            download_directory.mkdirs();


        String output_filename = Environment.getExternalStorageDirectory() + "/Download/Remote Devices/temp/tempfile.jpg";

        File output_file = new File(output_filename);
        try {
            output_file.setWritable(true, false);
            output_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }

        FileOutputStream output_stream = new FileOutputStream(output_file, false);
        try {
            output_stream = new FileOutputStream(output_filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }

        //transfer bytes from the inputfile to the outputfile
        int totalbytes = 0;
        byte[] buffer = new byte[1024 * 8];
        input_stream.read(buffer, 0, 8);
        int length;
        int received_bytes = 0;

        ByteBuffer tmp_buffer = ByteBuffer.wrap(buffer); // change order to little endian
        tmp_buffer.order(ByteOrder.LITTLE_ENDIAN);
        totalbytes = (int) tmp_buffer.getLong();


        while (received_bytes < totalbytes && (length = input_stream.read(buffer, 0, buffer.length)) > 0) {
            output_stream.write(buffer, 0, length);
            received_bytes += length;
        }

        //Close the streams
        output_stream.flush();
        output_stream.close();
        bmpimage = BitmapFactory.decodeFile(output_filename);
    }


    public void recievefile(String savename) throws IOException {

        int totalbytes = 0;
        downloadingfile = true;

        File download_directory = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices/Downloaded");

        if (!download_directory.exists())
            download_directory.mkdirs();

        String output_filename = Environment.getExternalStorageDirectory() + "/Download/Remote Devices/Downloaded/" + savename;
        File output_file = new File(output_filename);

        try {

            output_file.setWritable(true, false);
            output_file.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }

        FileOutputStream output_stream = new FileOutputStream(output_file, false);
        try {
            output_stream = new FileOutputStream(output_filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024 * 128];
        cleaning_stream = true;
        input_stream.read(buffer, 0, 8);
        int length;

        int received_bytes = 0;

        ByteBuffer tmp_buffer = ByteBuffer.wrap(buffer); // temp buffer to change order to little endian
        tmp_buffer.order(ByteOrder.LITTLE_ENDIAN);
        totalbytes = (int) tmp_buffer.getLong();

        boolean server_timed_out = false; // did server stop sending because of time output_writer

        if (totalbytes > 0) {
            int unitpercent = totalbytes / 100;

            try {
                while (received_bytes < totalbytes && (length = input_stream.read(buffer, 0, buffer.length)) > 0) {

                    if (!downloadingfile && server_timed_out==false) {
                        // wait until server gets time out output_writer
                        // a guess of 4 secs
                        android.os.SystemClock.sleep(4000);
                        socket.setSoTimeout(500); // stop cleaning stream if there input_stream nothing to read.
                        server_timed_out = true;
                    }
                    output_stream.write(buffer, 0, length);
                    received_bytes += length;
                    percentdownloaded = (int) (1.0 * received_bytes / unitpercent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(this.getClass().toString(),e.getMessage());
            }
        }

        socket.setSoTimeout(0); // set back to infinite
        if (received_bytes == totalbytes) {
            percentdownloaded = 100;
        }

        downloadingfile = false;

        output_stream.flush();
        output_stream.close();

        cleaning_stream = false;
    }


    public void sendMessage(final String message) {
        if(!check_connection() || cleaning_stream){
         return;
        }

        final Thread send_msg_thread = new Thread() {
            @Override
            public void run() {
                if (output_writer != null && !output_writer.checkError()) {
                    try {
                        output_writer.println(message);
                        Log.e(this.getClass().toString(),"send message: " + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(this.getClass().toString(),e.getMessage());
                    }
                    output_writer.flush();
                } else {
                    Log.e(this.getClass().toString(),"output writer input_stream null");
                }
            }
        };
        send_msg_thread.start();
    }

    public String recieveMessage() {
        String msg_line = null;
        try {
            msg_line = buffer_reader.readLine();
            Log.e(this.getClass().toString(), "recieved message: "+ msg_line);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        return msg_line;
    }

    public boolean check_connection() {
        if (!isconnected()) {
            disconnect();
            Toast.makeText(SocketService.this, "Disconnected", Toast.LENGTH_SHORT).show();
            Intent dialogIntent = new Intent(this, Connect.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(dialogIntent);
            return false;
        }
        return true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void connect(String hostname) {
        if (socket == null) {
            tryhostname = hostname;
            Runnable connect = new connectSocket();
            new Thread(connect).start();
        }
    }

    public boolean isconnected() {
        try {
            if (socket == null || !socket.isConnected() || output_writer == null || output_writer.checkError())
                return false;
            else
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
            return false;
        }
    }


    public void disconnect() {
        try {
            socket.shutdownInput();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        try {
            socket.shutdownOutput();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        socket = null;
    }



    class connectSocket implements Runnable {
        @Override
        public void run() {
            int port = 2055;
            try {
                try {
                    socket = new Socket(tryhostname, port);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().toString(),e.getMessage());
                    tryhostname = null;
                    return;
                }

                InetSocketAddress localSocketAddress = (InetSocketAddress) socket.getLocalSocketAddress();
                clientname = localSocketAddress.getHostName();

                InetSocketAddress serverAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                servername = serverAddress.getHostName();

                try {
                    //send the message to the server
                    output_writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    buffer_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    input_stream = socket.getInputStream();
                    inputreader = new InputStreamReader(socket.getInputStream());

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().toString(),e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(this.getClass().toString(),e.getMessage());
            }
        }
    }


    @Override
    public void onDestroy() {
        try {
            disconnect();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        socket = null;
        try {
            // please DIE DIE DIE.....
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.getClass().toString(),e.getMessage());
        }
        super.onDestroy();
    }
}
