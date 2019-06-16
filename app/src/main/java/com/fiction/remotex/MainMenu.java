package com.fiction.remotex;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONException;
import org.json.JSONObject;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SocketService socketServiceObject;
    private int backpressed = 0;
    String mouse_events = null;

    float mouse_sensitivity = (float) 1.5;
    int new_mouse_x = 0, new_mouse_y = 0, mouse_x = 0, mouse_y = 0, mouse_ops = 9;
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
        setContentView(R.layout.activity_main_menu);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setControlListeners();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // get info about pc after some time
                getSystemInfo();
            }
        }, 100);
    }


    public void setControlListeners() {
        IndicatorSeekBar seekBar = (IndicatorSeekBar) findViewById(R.id.vol_slider);

        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                try {
                    socketServiceObject.sendMessage("^setvolume;" + seekParams.progress);
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), e.getMessage());
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });


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
                    //get initial mouse cordinates
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
    }

    public void toggle_nav_bar_onclick(View V) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backpressed == 0) {
                backpressed += 1;
                Toast.makeText(MainMenu.this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
            } else {
                super.onBackPressed();
            }
        }
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

        } else if (id == R.id.nav_explorer) {
            Intent i = new Intent(this, ExplorerActivity.class);
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disconnect(View V) {
        socketServiceObject.disconnect();
        Toast.makeText(MainMenu.this, "Disconnected", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, Connect.class);
        startActivity(i);
        this.finish();
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

    public void playpause(View v) {
        socketServiceObject.sendMessage("%play");
    }

    public void nexttrack(View v) {
        socketServiceObject.sendMessage("%next");
    }

    public void previoustrack(View v) {
        socketServiceObject.sendMessage("%previous");
    }

    public void volmute(View v) {
        socketServiceObject.sendMessage("%mute");
    }


    public void showSystemInfo(String jsonStringInfo) throws JSONException {

        JSONObject json = new JSONObject(jsonStringInfo);
        String computerName = json.getString("computername");
        String userName = json.getString("username");
        int system_vol = Integer.parseInt(json.getString("volume"));

        IndicatorSeekBar seekBar = (IndicatorSeekBar) findViewById(R.id.vol_slider);
        seekBar.setProgress(system_vol);

        TextView PCname = (TextView) findViewById(R.id.pc_name);
        PCname.setText(computerName + "/" + userName);
    }


    private void getSystemInfo() {
        try {
            socketServiceObject.sendMessage("$getinfo");
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage());
        }


        final Thread getInfo_thread = new Thread() {
            @Override
            public void run() {
                try {

                    final String sysInfo = socketServiceObject.recieveMessage();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                showSystemInfo(sysInfo);
                            } catch (JSONException e) {
                                Log.e(this.getClass().toString(), e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), e.getMessage());
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketServiceObject = null;
        }
    };

}



