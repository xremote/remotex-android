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

    TableRow controls_layout;
    int touch_x = -1, touch_y = -1, mouse_ops = 9, enter_key = 0;
    int maxpixel = 2000, screenx = 0, screeny = 0;
    SocketService objectservice;
    boolean on_waiting_screen = true, is_paused=false;

    //enter_key = 0 -> not pressed, 1 -> pressed
    //mouse_ops = 9 -> no mouse key pressed, 2 -> right click pressed


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_screen);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenx = size.x;
        screeny = size.y;
        setControlsListener();
    }

    public void setControlsListener() {

        ImageButton return_b = (ImageButton) findViewById(R.id.return_b);
        return_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    enter_key = 1;
                }
                return true;
            }
        });

        ImageButton right_b = (ImageButton) findViewById(R.id.R_click);
        right_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mouse_ops = 2;
                }
                return true;
            }
        });

        ImageView screen_image = (ImageView) findViewById(R.id.Screen);
        screen_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!on_waiting_screen) {
                        touch_x = (int) (event.getX() * maxpixel / screenx);
                        touch_y = (int) (event.getY() * maxpixel / screeny);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    controls_layout.setVisibility(TableRow.VISIBLE);
                    if (on_waiting_screen) {
                        share();
                        on_waiting_screen = false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        super.onResume();
        is_paused = false;
        on_waiting_screen = true;
        ImageView screen_image = (ImageView) findViewById(R.id.Screen);
        screen_image.setImageResource(R.drawable.screenshare);
        screen_image.invalidate();
        controls_layout = (TableRow) findViewById(R.id.controls_row);
        controls_layout.setVisibility(TableRow.GONE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
        is_paused = true;
    }


    public void show() {
        if (objectservice.bmpimage != null) {
            ImageView screen_image = (ImageView) findViewById(R.id.Screen);
            screen_image.setImageBitmap(objectservice.bmpimage);
            screen_image.invalidate();
            objectservice.bmpimage = null;
        }
    }

    public void share() {
        final Thread update_screen_thread = new Thread() {
            @Override
            public void run() {

                while (!is_paused) {
                    try {
                        objectservice.sendMessage("!" + Integer.toString(touch_x) + ";" + Integer.toString(touch_y) + ";" +
                                Integer.toString(mouse_ops) + ";" + Integer.toString(enter_key));
                        touch_x = -1;
                        touch_y = -1;
                        mouse_ops = 9;
                        enter_key = 0;
                        objectservice.recieveimage();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().toString(),e.getMessage());
                    }
                }
            }
        };
        update_screen_thread.start();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            objectservice = binder.getService();
         }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            objectservice = null;
         }
    };
}
