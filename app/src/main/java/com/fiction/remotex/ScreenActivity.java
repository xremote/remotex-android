package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow;

import java.io.IOException;


public class ScreenActivity extends Activity {

        TableRow buttons_r;


    int x1 = -1,pause=5, y1 = -1,z1=0,r1=0, maxpixel = 2000, screenx = 0, screeny = 0;
    SocketService objectservice;
    boolean isbound = false,startenable=true;
    int i1 = 3;
    private static final String TAG = "luckymessage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        setContentView(R.layout.activity_screen);
        ImageView screen_image = (ImageView) findViewById(R.id.Screen);
        //     help.setText("Ready");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenx = size.x;
        screeny = size.y;
        Log.i(TAG, screenx + ":x + y: " + screeny);


        ImageButton return_b =(ImageButton)findViewById(R.id.return_b);
        return_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {



                } else if (event.getAction() == MotionEvent.ACTION_UP) {


                    r1=1;

                }

                return true;
            }
        });




        ImageButton right_b =(ImageButton)findViewById(R.id.R_click);
        right_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {



                } else if (event.getAction() == MotionEvent.ACTION_UP) {


                    z1=2;

                }

                return true;
            }
        });





        screen_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    //  socketServiceObject.sendMessage("!done");
                    // specialfunction();
                    //socketServiceObject.sendMessage("!" + x1 + ";" + y1 + ";" + "5;");

                    if(!startenable) {
                        x1 = (int) (event.getX() * maxpixel / screenx);
                        y1 = (int) (event.getY() * maxpixel / screeny);

                        Log.i(TAG, x1 + " co " + y1);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttons_r.setVisibility(TableRow.VISIBLE);
                    if(startenable)
                    { Log.i(TAG, "started");   share();  startenable=false; }
                }

                return true;
            }
        });


    }

    @Override
    protected void onResume() {
        Intent i = new Intent(this,SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        super.onResume();
        pause=5;
        startenable=true;
        ImageView screen_image = (ImageView) findViewById(R.id.Screen);
        screen_image.setImageResource(R.drawable.screenshare);
        screen_image.invalidate();

        buttons_r= (TableRow)findViewById(R.id.buttons);

        buttons_r.setVisibility(TableRow.GONE);
        //share();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
        pause=-1;

    }


    public void show() {
        if(objectservice.bmpimage!=null) {
            ImageView screen_image = (ImageView) findViewById(R.id.Screen);
            screen_image.setImageBitmap(objectservice.bmpimage);
            screen_image.invalidate();
            objectservice.bmpimage = null;
        }
    }





    public void share() {


        final Thread t = new Thread() {
            @Override
            public void run() {
                String msg="";
                while(pause>0) {
                    try {

                        objectservice.sendMessage("!" + Integer.toString(x1) +";" + Integer.toString(y1) + ";" +
                                Integer.toString(z1)+";"  + Integer.toString(r1));


                        x1=-1; y1=-1; z1 =9; r1 =0;
                        objectservice.recieveimage();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                show();
                            }
                        });




                    }

                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    //pause--;
                }
            }
        };



        t.start();



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
