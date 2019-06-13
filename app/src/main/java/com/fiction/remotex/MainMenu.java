package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.Gravity;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.warkiz.widget.Indicator;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import static com.fiction.remotex.MainMenu.Constant.FIRST_COLUMN;
import static com.fiction.remotex.MainMenu.Constant.SECOND_COLUMN;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "zedmessage";
    SocketService socketServiceObject;
    boolean isSocketServiceBounded = false;
    private int backpressed=0;
    String sendoutput=null;
    boolean isbound = false;
    int x=0,y=0,x1=0,y1=0,z=9,factor=50;
    float sensitivity = (float)2.5;

    int xc=0,yc=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Exception list1: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Log.i(TAG, "Exception list2: ");
        Log.e(this.getClass().toString(),"binded");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        IndicatorSeekBar seekBar = (IndicatorSeekBar) findViewById(R.id.vol_slider);

        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                Log.e(this.getClass().toString(),"Seeked");
                try{
                    socketServiceObject.sendMessage("^setvolume;"+seekParams.progress);
                }catch (Exception e){

                }

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });




        Button Left =(Button)findViewById(R.id.Left_Click);
        Left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    z = 0;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    z = 1;
                }
                send1();
                return false;

            }
        });





        Button Right =(Button)findViewById(R.id.Right_Click);
        Right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    z=2;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    z=3;
                }
                send1();
                return false;
            }
        });




        Button Middle =(Button)findViewById(R.id.Middle_Click);
        Middle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    z=4;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    z=5;
                }
                send1();
                return false;
            }
        });


        TextView Trackpad =(TextView)findViewById(R.id.TrackPad);
        Trackpad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if(!(x1==0 && y1==0)) {
                    x = (int)event.getX()- x1;
                    y = (int)event.getY()-y1;
                    x1=(int)event.getX();
                    y1=(int)event.getY();
                    x= x + (int)(x*sensitivity);
                    y= y + (int)(y*sensitivity);

                }
                else{
                    x1 = (int)event.getX();
                    y1=(int)event.getY();
                    x=0; y=0;

                }


                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    x1 = (int)event.getX();
                    y1=(int)event.getY();
                    x=0; y=0;

                    xc = (int)event.getX();
                    yc = (int)event.getY();

                    //sendoutput = "down" + event.getX() + " " + event.getY();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    x=0; y=0;                   x1=0; y1=0;


                    if( Math.abs(xc-(int)event.getX())<2 && Math.abs(yc-(int)event.getY())<2 )
                    { z=0; send1(); z=1;send1();}
                    //sendoutput = "Up" + event.getX() + " " + event.getY();

                }


                send1();
                return true;
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                getSystemInfo();
            }
        }, 100);
    }


    public void toggle_nav_bar(View V){
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(backpressed==0){
                backpressed+=1;
                Toast.makeText(MainMenu.this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
            }else{
                super.onBackPressed();
            }
        }
    }


    @Override
    protected void onResume() {
        Intent i = new Intent(this,SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(this.getClass().toString(),"mainpause");
        unbindService(serviceConnection);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_screen) {
            Intent i = new Intent(this, ScreenActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_mouse) {
            Intent i = new Intent(this, MouseActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_keyboard) {
            Intent i = new Intent(this, KeyboardActivity.class);
            startActivity(i);

        }  else if (id == R.id.nav_explorer) {
            Intent i = new Intent(this, ExplorerActivity.class);
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disconnect(View V){

        Log.e(this.getClass().toString(),"Disconnecting2");

        socketServiceObject.disconnect();

        Toast.makeText(MainMenu.this, "Disconnected", Toast.LENGTH_SHORT).show();
        //Intent i1 = new Intent(this, SocketService.class);
        //stopService(i1);


        Intent i = new Intent(this, Connect.class);
        startActivity(i);
        this.finish();
    }



    public void send1()
    {

        if(!(x==0 && y==0 && z==9)) {
            sendoutput = x + ";" + y + ";" + z + ";";
            if (sendoutput != null) {
                socketServiceObject.sendMessage("*" + sendoutput);

            }
        }

    }

    public void playpause(View v){
        socketServiceObject.sendMessage("%play");
    }

    public void nexttrack(View v){
        socketServiceObject.sendMessage("%next");
    }

    public void previoustrack(View v){
        socketServiceObject.sendMessage("%previous");
    }


    public void volmute(View v){
        socketServiceObject.sendMessage("%mute");
    }


    public void showSystemInfo(String jsonStringInfo) throws JSONException {

        //show details on ui after parsing jsonstring info
        //Toast.makeText(MainMenu.this, jsonStringInfo, Toast.LENGTH_LONG).show();


        JSONObject json = new JSONObject(jsonStringInfo);
        String computerName = json.getString("computername");
        String userName =  json.getString("username");
        int system_vol =   Integer.parseInt(json.getString("volume"));

        IndicatorSeekBar seekBar = (IndicatorSeekBar) findViewById(R.id.vol_slider);
        seekBar.setProgress(system_vol);

        TextView PCname = (TextView) findViewById(R.id.pc_name);
        PCname.setText(computerName + "/" + userName);

//
//        HashMap item1 = new HashMap();
//        item1.put(FIRST_COLUMN,"Computer Name");
//        String computerName = json.getString("computername");
//        item1.put(SECOND_COLUMN,computerName);
//        list.add(item1);
//
//        HashMap item2 = new HashMap();
//        item2.put(FIRST_COLUMN,"User Name");
//        String userName =  json.getString("username");
//        item2.put(SECOND_COLUMN,userName);
//        list.add(item2);
//
//        HashMap item3 = new HashMap();
//        item3.put(FIRST_COLUMN,"Monitor Count");
//        String monitorCount =  json.getString("monitorcount");
//        item3.put(SECOND_COLUMN,monitorCount);
//        list.add(item3);
//
//        HashMap item4 = new HashMap();
//        item4.put(FIRST_COLUMN,"Monitor Size");
//        String monitorSize = json.getString("monitorwidth")+" X "+json.getString("monitorheight");
//        item4.put(SECOND_COLUMN,monitorSize);
//        list.add(item4);
//
//        HashMap item5 = new HashMap();
//        item5.put(FIRST_COLUMN,"Battery Life");
//        String batteryLife = json.getString("batterylife");
//        item5.put(SECOND_COLUMN,batteryLife);
//        list.add(item5);
//
//        HashMap item6 = new HashMap();
//        item6.put(FIRST_COLUMN,"Battery Status" );
//        String batteryStatus = json.getString("batterystatus");
//        item6.put(SECOND_COLUMN,batteryStatus);
//        list.add(item6);
//
//
//        HashMap item7 = new HashMap();
//        item7.put(FIRST_COLUMN,"System Type");
//        String osbit = json.getString("osbit") + " Operating System";
//        item7.put(SECOND_COLUMN,osbit);
//        list.add(item7);
//
//        HashMap item8 = new HashMap();
//        item8.put(FIRST_COLUMN, "OS Version");
//        String osVersion = json.getString("osversion");
//        item8.put(SECOND_COLUMN,osVersion);
//        list.add(item8);
//
//        HashMap item9 = new HashMap();
//        item9.put(FIRST_COLUMN,"OS Name");
//        String osName =  json.getString("osname");
//        item9.put(SECOND_COLUMN,osName);
//        list.add(item9);
//
//        HashMap item10 = new HashMap();
//        item10.put(FIRST_COLUMN,"No. of Processors");
//        String processorCount = json.getString("processorcount");
//        item10.put(SECOND_COLUMN,processorCount);
//        list.add(item10);
//
//        HashMap item11 = new HashMap();
//        item11.put(FIRST_COLUMN,"Ram");
//        String ram = json.getString("totalmemory");
//        item11.put(SECOND_COLUMN,ram);
//        list.add(item11);
//
//        HashMap item12 = new HashMap();
//        item12.put(FIRST_COLUMN,"Secondary Storage");
//        String hddSize = json.getString("HDD-SIZE");
//        int hddSizeInt=new Double(hddSize.substring(0,hddSize.length()-2)).intValue();
//        hddSize=hddSizeInt+" GB";
//        item12.put(SECOND_COLUMN,hddSize);
//        list.add(item12);
//
//        HashMap item13 = new HashMap();
//        item13.put(FIRST_COLUMN,"IP Address");
//        String ip = json.getString("ipaddress");
//        item13.put(SECOND_COLUMN,ip);
//        list.add(item13);
//
//
//        system_values_array = new String[system_values.size()];
//        system_values.toArray(system_values_array);
//        system_values_head_array = new String[system_values_head.size()];
//        system_values_head.toArray(system_values_head_array);

    }




    private void getSystemInfo() {
        try {
            socketServiceObject.sendMessage("$getinfo");
        } catch (Exception e) {
            Log.i(TAG, "Exception list1: " + e.getMessage());
        }


        final Thread getInfo_thread = new Thread() {
            @Override
            public void run() {
                try {

                    final String sysInfo = socketServiceObject.recieveMessage();
                    Log.i(TAG, "Exception list2: " + sysInfo);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                showSystemInfo(sysInfo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, "Exception list3: ");
                        }
                    });
                } catch (Exception e) {
                    Log.i(TAG, "mainmenu; " + e);
                }
            }

        };
        getInfo_thread.start();
    }

    //service connection to network
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketServiceObject = binder.getService();
            Log.i(TAG, "service connection obtained  ");
            isSocketServiceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketServiceObject = null;
            isSocketServiceBounded = false;
        }
    };


    public class Constant {
        public static final String FIRST_COLUMN = "First";
        public static final String SECOND_COLUMN = "Second";
    }
}



