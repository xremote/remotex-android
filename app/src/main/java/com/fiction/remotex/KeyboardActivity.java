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


public class KeyboardActivity extends Activity
{
    private static final String TAG = "zedmessage";

    String sendoutput="";

    SocketService socketServiceObject;
    boolean isbound = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        Log.e(this.getClass().toString(),"keyresumes");
        Intent i = new Intent(this,SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(serviceConnection);
        super.onPause();
    }

    public void switchkeys(){

            LinearLayout nlayout1 = (LinearLayout)findViewById(R.id.nlayout);

            if(nlayout1.getVisibility()!= LinearLayout.GONE )
                 nlayout1.setVisibility(LinearLayout.GONE);
            else
                nlayout1.setVisibility(LinearLayout.VISIBLE);

        }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_keyboard);



            switchkeys();




        Button LShiftKey_b =(Button)findViewById(R.id.LShiftKey);
        LShiftKey_b.setOnTouchListener(listener);

        Button Back_b =(Button)findViewById(R.id.Back);
        Back_b.setOnTouchListener(listener);

        Button CapsLock_b =(Button)findViewById(R.id.CapsLock);
        CapsLock_b.setOnTouchListener(listener);

        Button Enter_b =(Button)findViewById(R.id.Enter);
        Enter_b.setOnTouchListener(listener);


        Button Space_b =(Button)findViewById(R.id.Space);
        Space_b.setOnTouchListener(listener);

        Button LWin_b =(Button)findViewById(R.id.LWin);
        LWin_b.setOnTouchListener(listener);

        Button Escape_b =(Button)findViewById(R.id.Escape);
        Escape_b.setOnTouchListener(listener);

        Button Insert_b =(Button)findViewById(R.id.Insert);
        Insert_b.setOnTouchListener(listener);

        Button LcontrolKey_b =(Button)findViewById(R.id.LControlKey);
        LcontrolKey_b.setOnTouchListener(listener);

        Button Left_b =(Button)findViewById(R.id.Left);
        Left_b.setOnTouchListener(listener);

        Button Up_b =(Button)findViewById(R.id.Up);
        Up_b.setOnTouchListener(listener);

        Button Down_b =(Button)findViewById(R.id.Down);
        Down_b.setOnTouchListener(listener);

        Button Right_b =(Button)findViewById(R.id.Right);
        Right_b.setOnTouchListener(listener);

        Button LMenu_b =(Button)findViewById(R.id.LMenu);
        LMenu_b.setOnTouchListener(listener);

        Button PrintScreen_b =(Button)findViewById(R.id.PrintScreen);
        PrintScreen_b.setOnTouchListener(listener);

        Button NumLock_b =(Button)findViewById(R.id.NumLock);
        NumLock_b.setOnTouchListener(listener);

        Button Scroll_b =(Button)findViewById(R.id.Scroll);
        Scroll_b.setOnTouchListener(listener);

        Button Delete_b =(Button)findViewById(R.id.Delete);
        Delete_b.setOnTouchListener(listener);

        Button Tab_b =(Button)findViewById(R.id.Tab);
        Tab_b.setOnTouchListener(listener);

        Button npad_b =(Button)findViewById(R.id.Npad);
        npad_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_UP){
                    Log.i(TAG,"clicked");
                    switchkeys();
                }
                return true;
            }
        });

        Button OemSemicolon_b =(Button)findViewById(R.id.OemSemicolon);
        OemSemicolon_b.setOnTouchListener(listener);

        Button OemComma_b =(Button)findViewById(R.id.OemComma);
        OemComma_b.setOnTouchListener(listener);

        Button Oemquestion_b =(Button)findViewById(R.id.Oemquestion);
        Oemquestion_b.setOnTouchListener(listener);

        Button Oemperiod_b =(Button)findViewById(R.id.oemperiod);
        Oemperiod_b.setOnTouchListener(listener);

        Button Q_b =(Button)findViewById(R.id.Q);
        Q_b.setOnTouchListener(listener);

        Button W_b =(Button)findViewById(R.id.W);
        W_b.setOnTouchListener(listener);

        Button E_b =(Button)findViewById(R.id.E);
        E_b.setOnTouchListener(listener);

        Button R_b =(Button)findViewById(R.id.R);
        R_b.setOnTouchListener(listener);

        Button T_b =(Button)findViewById(R.id.T);
        T_b.setOnTouchListener(listener);

        Button Y_b =(Button)findViewById(R.id.Y);
        Y_b.setOnTouchListener(listener);

        Button U_b =(Button)findViewById(R.id.U);
        U_b.setOnTouchListener(listener);

        Button I_b =(Button)findViewById(R.id.I);
        I_b.setOnTouchListener(listener);

        Button O_b =(Button)findViewById(R.id.O);
        O_b.setOnTouchListener(listener);

        Button P_b =(Button)findViewById(R.id.P);
        P_b.setOnTouchListener(listener);

        Button A_b =(Button)findViewById(R.id.A);
        A_b.setOnTouchListener(listener);

        Button S_b =(Button)findViewById(R.id.S);
        S_b.setOnTouchListener(listener);

        Button D_b =(Button)findViewById(R.id.D);
        D_b.setOnTouchListener(listener);

        Button F_b =(Button)findViewById(R.id.F);
        F_b.setOnTouchListener(listener);

        Button G_b =(Button)findViewById(R.id.G);
        G_b.setOnTouchListener(listener);

        Button H_b =(Button)findViewById(R.id.H);
        H_b.setOnTouchListener(listener);

        Button J_b =(Button)findViewById(R.id.J);
        J_b.setOnTouchListener(listener);

        Button K_b =(Button)findViewById(R.id.K);
        K_b.setOnTouchListener(listener);

        Button L_b =(Button)findViewById(R.id.L);
        L_b.setOnTouchListener(listener);

        Button Z_b =(Button)findViewById(R.id.Z);
        Z_b.setOnTouchListener(listener);

        Button X_b =(Button)findViewById(R.id.X);
        X_b.setOnTouchListener(listener);

        Button C_b =(Button)findViewById(R.id.C);
        C_b.setOnTouchListener(listener);

        Button V_b =(Button)findViewById(R.id.V);
        V_b.setOnTouchListener(listener);

        Button B_b =(Button)findViewById(R.id.B);
        B_b.setOnTouchListener(listener);

        Button N_b =(Button)findViewById(R.id.N);
        N_b.setOnTouchListener(listener);

        Button M_b =(Button)findViewById(R.id.M);
        M_b.setOnTouchListener(listener);

        Button F1_b =(Button)findViewById(R.id.F1);
        F1_b.setOnTouchListener(listener);

        Button F2_b =(Button)findViewById(R.id.F2);
        F2_b.setOnTouchListener(listener);

        Button F3_b =(Button)findViewById(R.id.F3);
        F3_b.setOnTouchListener(listener);

        Button F4_b =(Button)findViewById(R.id.F4);
        F4_b.setOnTouchListener(listener);

        Button F5_b =(Button)findViewById(R.id.F5);
        F5_b.setOnTouchListener(listener);

        Button F6_b =(Button)findViewById(R.id.F6);
        F6_b.setOnTouchListener(listener);

        Button F7_b =(Button)findViewById(R.id.F7);
        F7_b.setOnTouchListener(listener);

        Button F8_b =(Button)findViewById(R.id.F8);
        F8_b.setOnTouchListener(listener);

        Button F9_b =(Button)findViewById(R.id.F9);
        F9_b.setOnTouchListener(listener);

        Button F10_b =(Button)findViewById(R.id.F10);
        F10_b.setOnTouchListener(listener);

        Button F11_b =(Button)findViewById(R.id.F11);
        F11_b.setOnTouchListener(listener);

        Button F12_b =(Button)findViewById(R.id.F12);
        F12_b.setOnTouchListener(listener);


        //NPAD

        Button D1_b =(Button)findViewById(R.id.D1);
        D1_b.setOnTouchListener(listener);

        Button D2_b =(Button)findViewById(R.id.D2);
        D2_b.setOnTouchListener(listener);

        Button D3_b =(Button)findViewById(R.id.D3);
        D3_b.setOnTouchListener(listener);

        Button D4_b =(Button)findViewById(R.id.D4);
        D4_b.setOnTouchListener(listener);

        Button D5_b =(Button)findViewById(R.id.D5);
        D5_b.setOnTouchListener(listener);

        Button D6_b =(Button)findViewById(R.id.D6);
        D6_b.setOnTouchListener(listener);

        Button D7_b =(Button)findViewById(R.id.D7);
        D7_b.setOnTouchListener(listener);

        Button D8_b =(Button)findViewById(R.id.D8);
        D8_b.setOnTouchListener(listener);

        Button D9_b =(Button)findViewById(R.id.D9);
        D9_b.setOnTouchListener(listener);

        Button D0_b =(Button)findViewById(R.id.D0);
        D0_b.setOnTouchListener(listener);

        Button Minus_b =(Button)findViewById(R.id.oemMinus);
        Minus_b.setOnTouchListener(listener);

        Button plus_b =(Button)findViewById(R.id.oemplus);
        plus_b.setOnTouchListener(listener);

        final Button oem5_b =(Button)findViewById(R.id.oem5);
        oem5_b.setOnTouchListener(listener);



    }

    View.OnTouchListener listener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String tag=v.getTag().toString();
            button_func(tag,event);
            return false;
        }
    };


    private boolean button_func(String button_val,MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {


            sendoutput += "1"+button_val+";";

        } else if (event.getAction() == MotionEvent.ACTION_UP) {


            sendoutput += "2"+button_val+";";

        }
        send1();
        return true;
    }


    public void send1() {


        if(sendoutput!="")
        {

            socketServiceObject.sendMessage("@"+sendoutput);

            sendoutput="";

        }

    }



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            Log.e(this.getClass().toString(),"keybinded");
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
