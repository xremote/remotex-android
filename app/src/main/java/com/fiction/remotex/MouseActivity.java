package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class MouseActivity extends Activity
{
    String sendoutput=null;
    SocketService  socketServiceObject;
    boolean isbound = false;
    int x=0,y=0,x1=0,y1=0,z=9,factor=50;
    float sensitivity = (float)2.5;

    int xc=0,yc=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //ActionBar actionBar = getSupportActionBar();

        //if(actionBar!=null)
        //  actionBar.hide();
        setContentView(R.layout.activity_mouse);

        Intent i = new Intent(this,SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        setMouseControls();
    }

    public void setMouseControls(){

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
                return true;

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
                return true;
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
                return true;
            }
        });


        seekbar();

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

    }


    public void seekbar() {
        SeekBar bar = (SeekBar) findViewById(R.id.Sensitivity);
        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        factor = i;
                        sensitivity = (float)(((2.4/50)*factor) + 0.1);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
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



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketServiceObject = binder.getService();
            isbound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            socketServiceObject = null;
            isbound = false;
        }
    };
}
