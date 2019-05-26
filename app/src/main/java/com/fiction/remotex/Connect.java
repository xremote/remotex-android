package com.fiction.remotex;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

public class Connect extends Activity {

    SocketService socketServiceObject;
    boolean isSocketServiceBounded = false;
    private ArrayList<ClientScanResult> devicesArray;
    private static final String TAG = "zedmessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect);
        Log.i(TAG, "Exception list1: ");
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        findConnectedDevices();
        getFileStoragePermission();
    }

    public void findConnectedDevices() {

        final WifiApManager wifiApManager =  new WifiApManager(this);

        // load connected devices list in background thread
        final Thread getConnectedDevices = new Thread() {
            @Override
            public void run() {
                try {
                    devicesArray = wifiApManager.getClientList(false);
                } catch (Exception e) {
                    Log.i(TAG, "get wifi device list" + e);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            if (devicesArray.size()>0) {
                                NoDeviceError(false);
                                showDevicesList();
                            } else{
                                NoDeviceError(true); // show "no device error"
                                showDevicesList();
                            }
                        }catch (Exception e){
                            Log.i(TAG, "get wifi device list ui" + e);
                        }
                    }
                });
            }
        };
        getConnectedDevices.start();
    }

    private String getNameByIP(String IPAddr){
        String hostName = "";
        try{
            hostName = InetAddress.getByName(IPAddr).getHostName();
            if(hostName.contains(IPAddr)){
                hostName = "";
            }
        }catch(Exception e){

        }
        return hostName;
    }

    public void showDevicesList() {

        String namesArray[] = new String[devicesArray.size()] ;

        int index =0;
        for(ClientScanResult device : devicesArray){
            namesArray[index] = device.getIpAddr() + ":\n" + getNameByIP(device.getIpAddr());
            index++;
        }

        final ListView devicesListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new CustomAdapter(this, namesArray);
        devicesListView.setAdapter(adapter);
        devicesListView.setFocusable(true);

        devicesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String value = (String) devicesListView.getItemAtPosition(i);
                        value = value.substring(0, value.indexOf(':'));
                        TextInputEditText et = (TextInputEditText) findViewById(R.id.edit_IP);
//                        EditText et = (EditText) findViewById(R.id.edit_IP);

                        et.setText(value);
                    }
                }
        );
    }

    public static class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(Context context, String[] namesArray) {
            super(context, R.layout.ip_list, namesArray);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.ip_list, parent, false);

            String oneItem = getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.text);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            textView.setText(oneItem);
            return view;
        }
    }



    public void getFileStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        File dir = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices");
        if (!dir.exists())
            dir.mkdirs();
    }


    public void Refreshlist(View v) {
        findConnectedDevices();
    }

    public void NoDeviceError(boolean noDeviceFound) {
        RelativeLayout noDeviceLayout = (RelativeLayout) findViewById(R.id.notfound_layout);

        if(noDeviceFound) noDeviceLayout.setVisibility(RelativeLayout.VISIBLE);
        else noDeviceLayout.setVisibility(RelativeLayout.GONE);
    }

    public void onConnect(View v) {
        Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
        tryConnect();

        // check after 500ms is device connected....
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (socketServiceObject.isconnected()) {
                    showMenu();
                }
                else{
                    showMenu();
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (!socketServiceObject.isconnected())
                            Toast.makeText(Connect.this, "Unable to Connect", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Connect.this, "Connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 500);
    }


    public void showhelp(View V){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(browserIntent);
    }


    public void tryConnect() {
        if (!socketServiceObject.isservicerunning) {
            Intent socketIntent = new Intent(this, SocketService.class);
            startService(socketIntent);
            socketServiceObject.isservicerunning = true;
        }
        try {
            socketServiceObject.connect( ((TextInputEditText) findViewById(R.id.edit_IP)).getText().toString() );
        } catch (Exception e) {       }
    }

    public void showMenu() {
//        Intent mainIntent = new Intent(this, MainMenu.class);
        Intent mainIntent = new Intent(this, MainMenu.class);
        this.startActivity(mainIntent);
        this.finish();
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            SocketService.LocalBinder binder = (SocketService.LocalBinder) iBinder;
            socketServiceObject = binder.getService();
            isSocketServiceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketServiceObject = null;
            isSocketServiceBounded = false;
        }
    };


    private void showInfo() {
        try {
            View layout = ( (LayoutInflater) this.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                    inflate(R.layout.popup_view,(ViewGroup) findViewById(R.id.popup_element));

            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);

            final PopupWindow infoWindow = new PopupWindow(layout,displaySize.x-10,displaySize.y-10,true);
            infoWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

             ((Button) layout.findViewById(R.id.ok_button)).           // ok button on info window layout
                 setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    infoWindow.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
