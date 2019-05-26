package com.fiction.remotex;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;


public class MusicplayActivity extends Activity {

    SocketService objectservice;
    boolean isbound = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplay);
        Intent i = new Intent(this,SocketService.class);
        bindService(i,serviceConnection, Context.BIND_AUTO_CREATE);


    }

    public void openplaylist(View v){
        objectservice.sendMessage("%playlist");
    }


    public void openmedia(View v){
        objectservice.sendMessage("%media");
    }

    public void playpause(View v){
        objectservice.sendMessage("%play");
    }

    public void nexttrack(View v){
        objectservice.sendMessage("%next");
    }

    public void previoustrack(View v){
        objectservice.sendMessage("%previous");
    }

    public void volminus(View v){
        objectservice.sendMessage("%minus");
    }

    public void volplus(View v){
        objectservice.sendMessage("%plus");
    }


    public void volmute(View v){
        objectservice.sendMessage("%mute");
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
