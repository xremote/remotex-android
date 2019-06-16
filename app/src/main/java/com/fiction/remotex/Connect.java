package com.fiction.remotex;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;

public class Connect extends Activity {

    SocketService socketServiceObject;
    private int backpressed = 0;
    private ArrayList<ClientScanResult> DevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        findConnectedDevices();
    }

    @Override
    protected void onResume() {

        //bind this activity to background service
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        getFileStoragePermission();

        if (socketServiceObject != null && socketServiceObject.isconnected()) {
            // if already connected
            showMenu();
        }

        // check once more after 10ms if device input_stream connected....
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (socketServiceObject != null && socketServiceObject.isconnected()) {
                    showMenu();
                } else {
                    //do nothing
                }
            }
        }, 10);

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        // to avoid closing app by mistake
        if (backpressed == 0) {
            backpressed += 1;
            Toast.makeText(Connect.this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onPause() {
        // unbind from background service
        unbindService(serviceConnection);
        super.onPause();
    }

    public void findConnectedDevices() {

        final WifiApManager wifiApManager = new WifiApManager(this);

        // load connected devices list in background thread
        final Thread getConnectedDevices = new Thread() {
            @Override
            public void run() {
                try {
                    DevicesList = wifiApManager.getClientList(false);
                } catch (Exception e) {

                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {

                            if (DevicesList.size() > 0) {
                                NoDeviceError(false);
                                showDevicesList();
                            } else {
                                NoDeviceError(true); // show "no device error"
                                showDevicesList();
                            }
                        } catch (Exception e) {
                            Log.e(this.getClass().toString(), e.getMessage());
                        }
                    }
                });
            }
        };

        // start the thread
        getConnectedDevices.start();
    }


    public void showDevicesList() {

        String DevicesInfo[] = new String[DevicesList.size()];

        int index = 0;
        for (ClientScanResult device : DevicesList) {
            DevicesInfo[index] = device.getIpAddr() + ":";
            index++;
        }

        final ListView devicesListView = (ListView) findViewById(R.id.DeviceList);
        ArrayAdapter<String> adapter = new CustomAdapter(this, DevicesInfo);
        devicesListView.setAdapter(adapter);
        devicesListView.setFocusable(true);

        devicesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String value = (String) devicesListView.getItemAtPosition(i);
                        value = value.substring(0, value.indexOf(':'));
                        TextInputEditText edit_IP = (TextInputEditText) findViewById(R.id.edit_IP);
                        edit_IP.setText(value);
                    }
                }
        );
    }

    public static class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(Context context, String[] DevicesInfo) {
            super(context, R.layout.connect_list_layout, DevicesInfo);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.connect_list_layout, parent, false);

            String ip_host = getItem(position);

            String hostName = ip_host.split(":")[1]; // host name
            String hostIP = ip_host.split(":")[0];

            if (hostName.contains(hostIP)) {
                hostName = "Unknown Host";
            }
            hostIP += ":";

            TextView host_text = (TextView) view.findViewById(R.id.device_host);
            TextView ip_text = (TextView) view.findViewById(R.id.device_IP);
            ImageView device_image = (ImageView) view.findViewById(R.id.drive_image);

            host_text.setText(hostName);
            ip_text.setText(hostIP);
            return view;
        }
    }


    public void getFileStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // create download folder if not present
        File dir = new File(Environment.getExternalStorageDirectory() + "/Download/Remote Devices");
        if (!dir.exists())
            dir.mkdirs();
    }


    public void Refreshlist_onclick(View v) {
        findConnectedDevices();
    }

    public void NoDeviceError(boolean noDeviceFound) {
        RelativeLayout noDeviceLayout = (RelativeLayout) findViewById(R.id.notfound_layout);
        if (noDeviceFound) noDeviceLayout.setVisibility(RelativeLayout.VISIBLE);
        else noDeviceLayout.setVisibility(RelativeLayout.GONE);
    }

    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public void Connect_onclick(View v) {
        String ip = ((TextInputEditText) findViewById(R.id.edit_IP)).getText().toString();
        if (!validIP(ip)) {
            Toast.makeText(this, "Enter Valid IP address", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
        tryConnect();

        // check after some time if device input_stream connected....
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (socketServiceObject.isconnected()) {
                    showMenu();
                } else {
                    //showMenu();
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
        }, 600);
    }


    public void showhelp_onclick(View V) {
        String helpURL = "https://www.google.com";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(helpURL));
        startActivity(browserIntent);
    }


    public void tryConnect() {

        if (!socketServiceObject.isservicerunning) {
            Intent socketIntent = new Intent(this, SocketService.class);
            startService(socketIntent);
            socketServiceObject.isservicerunning = true;
        }
        try {
            socketServiceObject.connect(((TextInputEditText) findViewById(R.id.edit_IP)).getText().toString());
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage());
        }
    }

    public void showMenu() {
        Intent mainIntent = new Intent(this, MainMenu.class);
        this.startActivity(mainIntent);
        this.finish();
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
