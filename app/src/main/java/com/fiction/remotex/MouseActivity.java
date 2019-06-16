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


public class MouseActivity extends Activity {
    String mouse_events = null;
    SocketService socketServiceObject;
    int new_mouse_x = 0, new_mouse_y = 0, mouse_x = 0, mouse_y = 0, mouse_ops = 9, sensitivity_factor = 50;
    float mouse_sensitivity = (float) 1.5;

    int mouse_down_x = 0, mouse_down_y = 0;
    // mouse operations or mouse ops
    private static int LEFT_CLICK_DOWN = 0;
    private static int LEFT_CLICK_UP = 1;
    private static int RIGHT_CLICK_DOWN = 2;
    private static int RIGHT_CLICK_UP = 3;
    private static int MIDDLE_CLICK_DOWN = 4;
    private static int MIDDLE_CLICK_UP = 5;
    private static int LEFT_SINGLE_CLICK = 6;
    private static int LEFT_DOUBLE_CLICK = 7;
    private static int DEFAULT_VALUE = 9;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_mouse);
        setMouseControls();
    }

    @Override
    protected void onResume() {
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(serviceConnection);
        super.onPause();
    }

    public void setMouseControls() {

        Button Left = (Button) findViewById(R.id.Left_Click);
        Left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mouse_ops = LEFT_CLICK_DOWN;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mouse_ops = LEFT_CLICK_UP;
                }
                send_mouse_events();
                return false;

            }
        });


        Button Right = (Button) findViewById(R.id.Right_Click);
        Right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mouse_ops = RIGHT_CLICK_DOWN;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mouse_ops = RIGHT_CLICK_UP;
                }
                send_mouse_events();
                return false;
            }
        });


        Button Middle = (Button) findViewById(R.id.Middle_Click);
        Middle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mouse_ops = MIDDLE_CLICK_DOWN;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mouse_ops = MIDDLE_CLICK_UP;
                }
                send_mouse_events();
                return false;
            }
        });


        TextView Trackpad = (TextView) findViewById(R.id.TrackPad);
        Trackpad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (!(mouse_x == 0 && mouse_y == 0)) {
                    new_mouse_x = (int) event.getX() - mouse_x;
                    new_mouse_y = (int) event.getY() - mouse_y;
                    mouse_x = (int) event.getX();
                    mouse_y = (int) event.getY();
                    new_mouse_x = new_mouse_x + (int) (new_mouse_x * mouse_sensitivity);
                    new_mouse_y = new_mouse_y + (int) (new_mouse_y * mouse_sensitivity);

                } else {
                    mouse_x = (int) event.getX();
                    mouse_y = (int) event.getY();
                    new_mouse_x = 0;
                    new_mouse_y = 0;

                }


                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mouse_x = (int) event.getX();
                    mouse_y = (int) event.getY();
                    new_mouse_x = 0;
                    new_mouse_y = 0;

                    mouse_down_x = (int) event.getX();
                    mouse_down_y = (int) event.getY();

                    //mouse_events = "down" + event.getX() + " " + event.getY();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    new_mouse_x = 0;
                    new_mouse_y = 0;
                    mouse_x = 0;
                    mouse_y = 0;


                    if (Math.abs(mouse_down_x - (int) event.getX()) < 2 && Math.abs(mouse_down_y - (int) event.getY()) < 2) {
                        mouse_ops = LEFT_SINGLE_CLICK;
                        send_mouse_events();
                    }
                }
                send_mouse_events();
                return true;
            }
        });

        SeekBar bar = (SeekBar) findViewById(R.id.Sensitivity);
        bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        sensitivity_factor = i;
                        mouse_sensitivity = (float) (((2.4 / 50) * sensitivity_factor) + 0.1);

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


    public void send_mouse_events() {
        if (!(new_mouse_x == 0 && new_mouse_y == 0 && mouse_ops == 9)) {
            mouse_events = new_mouse_x + ";" + new_mouse_y + ";" + mouse_ops + ";";
            if (mouse_events != null) {
                socketServiceObject.sendMessage("*" + mouse_events);
                mouse_ops = DEFAULT_VALUE;
            }
        }

    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketServiceObject = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketServiceObject = null;

        }
    };
}
