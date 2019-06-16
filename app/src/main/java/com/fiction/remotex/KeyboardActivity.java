package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class KeyboardActivity extends Activity {
    String key_events = "";
    SocketService socketServiceObject;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    public void switchkeys() {
        LinearLayout nlayout1 = (LinearLayout) findViewById(R.id.nlayout);
        if (nlayout1.getVisibility() != LinearLayout.GONE)
            nlayout1.setVisibility(LinearLayout.GONE);
        else
            nlayout1.setVisibility(LinearLayout.VISIBLE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        setKeysOnTouchListener();
        switchkeys();
    }

    public void setKeysOnTouchListener() {
        Button npad_b = (Button) findViewById(R.id.Npad);
        npad_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    switchkeys();
                }
                return true;
            }
        });

        ArrayList<View> allButtons;
        allButtons = ((LinearLayout) findViewById(R.id.Keyboard_Layout)).getTouchables();
        for (int i = 0; i < allButtons.size(); i++) {
            View touchable = allButtons.get(i);
            if (touchable.getTag() != null && !touchable.getTag().toString().contains("Npad")) {
                touchable.setOnTouchListener(listener);
            }
        }
    }


    View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String tag = v.getTag().toString();
            button_func(tag, event);
            return false;
        }
    };


    private boolean button_func(String button_val, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            key_events += "1" + button_val + ";";
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            key_events += "2" + button_val + ";";
        }
        send_key_events();
        return true;
    }


    public void send_key_events() {
        if (key_events != "") {
            socketServiceObject.sendMessage("@" + key_events);
            key_events = "";
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
