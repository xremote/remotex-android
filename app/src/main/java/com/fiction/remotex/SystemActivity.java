package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class SystemActivity extends Activity {

    SocketService objectservice;
    boolean isbound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        Intent i = new Intent(this,SocketService.class);
        bindService(i,serviceConnection, Context.BIND_AUTO_CREATE);
    }



    public void lockUser(View v){
        objectservice.sendMessage("^lockuser");
    }

    public void logOff(View v){objectservice.sendMessage("^logoff"); }

    public void screenShot(View v){
        objectservice.sendMessage("^screenshot");
    }

    public void taskManager(View v){objectservice.sendMessage("^taskmanager"); }

    public void magPlus(View v){
        objectservice.sendMessage("^magnify+");
    }

    public void magMinus(View v){objectservice.sendMessage("^magnify-"); }

    public void shutDown(View v){objectservice.sendMessage("^shutdown"); }

    public void restart(View v){objectservice.sendMessage("^restart"); }

    public void previousSlide(View v){objectservice.sendMessage("^previousslide"); }

    public void playpauseSlide(View v){objectservice.sendMessage("^pauseplay"); }

    public void nextSlide(View v){objectservice.sendMessage("^nextslide"); }

    public void clearAll(View v){objectservice.sendMessage("^clearsins"); }





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
