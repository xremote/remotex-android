package com.fiction.remotex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.fiction.remotex.Encryption;

import java.nio.charset.StandardCharsets;


public class SplashScreen extends Activity {

    private final int SPLASH_DISPLAY_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String plainmsg = "hello";
//        String pwd = "1234568";
//        String emsg = Encryption.encrypt(plainmsg, pwd);
//
//        Log.e(this.getClass().toString(), emsg);
//
//        try{
//            String dmsg = Encryption.decrypt(emsg,pwd);
//
//            Log.e(this.getClass().toString(), dmsg );
//
//        }catch (Exception e){
//
//        }
        setContentView(R.layout.activity_splash_screen);
        TextView splashText = (TextView) findViewById(R.id.splashText);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashText.startAnimation(myFadeInAnimation);
        // set timer for end of splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openConnectActivity();
            }
        }, SPLASH_DISPLAY_TIME);
    }

    public void openConnectActivity() {
        Intent connectIntent = new Intent(this, Connect.class);
        this.startActivity(connectIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        this.finish();
    }
}
