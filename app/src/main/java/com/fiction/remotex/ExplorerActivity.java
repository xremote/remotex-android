package com.fiction.remotex;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExplorerActivity extends Activity
{
    public String threaddata ="";
    public int totalsize=0,completed=0;
    private static final String TAG = "zedmessage";
    private static ListView listView;
    String namesArray[];
    Integer imagesArray[];
    public static String[] parts;
    public static String path="";
    SocketService  objectservice;
    boolean isbound = false;
    public ProgressDialog pd;
    private Boolean downloading = false;
    private Boolean stopdownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explorer);
        reset(); //reset path and show default my computer screen
        Log.i(TAG, "created");

        MaterialButton reset_b =(MaterialButton)findViewById(R.id.Home);
        reset_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {


                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    reset();
                }

                return false;
            }
        });


        MaterialButton back_b =(MaterialButton)findViewById(R.id.Back);
        back_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {


                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    goback();
                }

                return false;
            }
        });

        pd = new ProgressDialog(ExplorerActivity.this);
        pd.setTitle("Please Wait...");
        pd.setMessage("Downloading to Storage/Download/Remote Devices/");
        pd.setProgressStyle(pd.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setIndeterminate(false);

        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(this.getClass().toString(),"Close");
                objectservice.downloadingfile=false;

            }
        });


        pd.dismiss();


    }


    @Override
    public void onBackPressed() {
        if(objectservice.downloadingfile || pd.isShowing()){
            return;
        }
            goback();
    }

    public boolean athome(){
        ListView viewlist = (ListView) findViewById(R.id.listView);

        if(viewlist.getChildCount() == 1 && path==""){
            return ((String) viewlist.getItemAtPosition(0)).contains("My Computer");
        }

        return false;

    }

    public void nameValue(String s) {

        List<String> names = new ArrayList<String>();
        List<Integer> images=new ArrayList<Integer>();

        int len = s.length();
        int sem=0;

        parts=s.split(";");

        for(int i = 0; i < len; i++){
            if(s.charAt(i)==';')
            {
                sem++;
            }
        }
        //Log.i(TAG,"sem " + sem + " parts " + parts.length);
        String s1 = "";

        for(int j=0;j<=sem;j++)
        {
            s1=parts[j];
            if(s1.charAt(0)=='0')
            {   names.add(s1.substring(1));images.add(R.drawable.pc_material_icon);}
            else if(s1.charAt(0)=='1')
            {   names.add(s1.substring(1));images.add(R.drawable.folder_material_icon);}
            else if(s1.charAt(0)=='2')
            {   names.add(s1.substring(1));images.add(R.drawable.file_material_icon);}
        }
        namesArray = new String[names.size()];
        imagesArray = new Integer[images.size()];
        names.toArray(namesArray);


        images.toArray(imagesArray);
    }

    public void listview(String a) {

        listView = (ListView) findViewById(R.id.listView);
        RelativeLayout empty_l =  (RelativeLayout) findViewById(R.id.empty_folder_layout);
        empty_l.setVisibility(RelativeLayout.GONE);
        namesArray = new String[0];
        imagesArray = new Integer[0];

        if(a==null || a.isEmpty()){
            ArrayAdapter<String> adapter = new CustomAdapter(this,namesArray,imagesArray);
            listView.setAdapter(adapter);
            listView.setFocusable(true);
            empty_l.setVisibility(RelativeLayout.VISIBLE);

            return;
        }

        Log.e(this.getClass().toString(),"listview : " + a);

        nameValue(a);
        ArrayAdapter<String> adapter = new CustomAdapter(this,namesArray,imagesArray);
        listView.setAdapter(adapter);
        listView.setFocusable(true);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 String value = (String) listView.getItemAtPosition(i);

                        if (parts[i].charAt(0) == '1') {

                            path += value + "\\";
                            send(path, i);
                        } else if (parts[i].charAt(0) == '0') {
                            send("My Computer", -1);
                        } else if (parts[i].charAt(0) == '2') {

                            sendfile(value, path + value);
                        }
                    }
                }
        );
        //   if(a.equals("1Empty Folder"))

        //     onBck();
        //Log.i(TAG,"a " + a);

    }




    public void goback(){

        if(athome()){
            super.onBackPressed(); // go back to previous activity
        }

        Log.i(TAG,path);
        if(path=="" || path==null || (path.indexOf('\\')==-1))
            reset();
        else{

            String temp = path.substring(0,path.lastIndexOf('\\'));
            // Log.i(TAG,"1st "+temp);
            String temp2 = temp.substring(0,temp.lastIndexOf('\\')+1);
            //Log.i(TAG, temp2);
            if(temp2.length()==temp.length())
            {  path="";  send("My Computer", -1); }
            else
            {path = temp2; send(path, 1);}
        }

    }

    public void refresh(){
        send(path,1);
    }

    @Override
    protected void onResume() {

        Intent i = new Intent(this,SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        objectservice.downloadingfile  = false;
        unbindService(serviceConnection);
        super.onPause();
    }

    public void reset()
    {
        path="";
        listview("0My Computer");
    }


    public void send(String v,int p) {

        if(p==-1){
            objectservice.sendMessage("&0" + v);
        }
        else {
            //Toast.makeText(Explorer.this, parts[p] + v, Toast.LENGTH_LONG).show();
            objectservice.sendMessage("&1" + v);
        }



        final Thread t = new Thread() {
            @Override
            public void run() {
                    try {
                        final String updatelist = objectservice.recieveMessage();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                listview(updatelist);
                                Log.i(TAG,"Exception list1: ");
                            }
                        });
                    }

                    catch (Exception e) {
                        Log.i(TAG,"; " + e);
                    }
                    //pause--;
                }

        };

        t.start();


    }

    public void sendfile(String filename,String path) {

        Log.i(TAG,"send start1");
        threaddata = filename;
        // Log.i(TAG, "5&2" + path);
        objectservice.sendMessage("&2" + path);
        Log.i(TAG,"send start2");
        pd.setTitle("Please Wait...");
        pd.setMessage("Downloading to Storage/Download/Remote Devices/");
        pd.show();
        objectservice.percentdownloaded = 0;
        Log.i(TAG,"send start3");
        pd.setProgress(objectservice.percentdownloaded);
        pd.setCancelable(false);
        Log.i(TAG,"send start4");

        objectservice.downloadingfile = true;
        final Thread t = new Thread() {
            @Override
            public void run() {

                try {
                    objectservice.recievefile(threaddata);
                    Log.i(TAG,"send start5");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Log.i(TAG,"send start6");
        final Thread t2 = new Thread() {
            @Override
            public void run() {

                int i=0;
                Log.i(TAG,"send start7");
                while(objectservice.percentdownloaded<100 && objectservice.downloadingfile) {
                    try {
                        pd.setProgress(objectservice.percentdownloaded);
                        sleep(200);
                        i+=2;
                        Log.e(this.getClass().toString(),""+objectservice.percentdownloaded);
                            if(i>100 && objectservice.percentdownloaded<1)
                                break;
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.i(TAG,"send start8");
                    }
                }
                //       Log.i(TAG," seconds" + i);
                Log.i(TAG,"send start9");

                Log.e(this.getClass().toString(),"done");

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                pd.setProgress(objectservice.percentdownloaded);
                                pd.setTitle("Completed :)");
                                pd.setMessage("Downloaded to Storage/Download/Remote Devices/");
                                objectservice.downloadingfile = false;
                            }
                        }
                );
                //pd.setCancelable(true);
                //pd.dismiss();
            }
        };


        Log.i(TAG,"send start10");
        t.start();
        Log.i(TAG,"send start11");
        t2.start();
        Log.i(TAG,"send start12");

    }



    public static class CustomAdapter extends ArrayAdapter<String> {
        private final String[] namesArray;
        private final Integer[] imagesArray;
        public CustomAdapter(Context context, String[] namesArray,Integer[] imagesArray) {
            super(context, R.layout.content, namesArray);
            this.imagesArray=imagesArray;
            this.namesArray=namesArray;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater= LayoutInflater.from(getContext());
            View view=layoutInflater.inflate(R.layout.content,parent,false);

            String oneItem=getItem(position);
            TextView textView= (TextView) view.findViewById(R.id.text);
            ImageView imageView=(ImageView) view.findViewById(R.id.imageView);

            textView.setText(oneItem);
            imageView.setImageResource(imagesArray[position]);


            return view;
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            objectservice = binder.getService();
            isbound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            objectservice = null;
            isbound = false;
        }
    };
}
