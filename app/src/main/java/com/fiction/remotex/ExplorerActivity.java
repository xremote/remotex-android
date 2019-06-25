package com.fiction.remotex;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExplorerActivity extends Activity {
    public String threaddata = "";
    private static ListView Drives_list_view;
    String items_nameArray[];
    Integer items_imagesArray[];
    public static String[] drives_list;
    public static String path = "";
    SocketService socketServiceObject;
    public ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explorer);
        reset(); //reset path and show default my computer screen
        setControlListeners();
    }

    public void setControlListeners() {

        MaterialButton reset_b = (MaterialButton) findViewById(R.id.Home);
        reset_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(!socketServiceObject.cleaning_stream)
                    reset();
                }
                return false;
            }
        });


        MaterialButton back_b = (MaterialButton) findViewById(R.id.Back);
        back_b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(!socketServiceObject.cleaning_stream)
                    goback();
                }
                return false;
            }
        });

        RelativeLayout loading_layout = (RelativeLayout) findViewById(R.id.LoadingLayout);

        pd = new ProgressDialog(ExplorerActivity.this);
        pd.setTitle("Please Wait...");
        pd.setMessage("Downloading to Storage/Download/Remote Devices/");
        pd.setProgressStyle(pd.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setIndeterminate(false);

        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                socketServiceObject.downloadingfile = false;
                if (socketServiceObject.percentdownloaded < 100) {
                    showloadingscreen();
                    socketServiceObject.sendMessage("syncback");
                    final Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                android.os.SystemClock.sleep(500);
                                while (socketServiceObject.cleaning_stream) {
                                    // undetermined waiting for file network stream to get cleared
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        hideloadingscreen();
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(this.getClass().toString(), e.getMessage());
                            }
                        }

                    };
                    t.start();
                }
            }
        });
        pd.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (socketServiceObject.cleaning_stream || socketServiceObject.downloadingfile || pd.isShowing()) {
            return;
        }
        goback();
    }

    public boolean athome() {
        // input_stream the explorer showing root directory
        ListView ItemList = (ListView) findViewById(R.id.DrivesList);

        if (ItemList.getChildCount() == 1 && path == "") {
            return ((String) ItemList.getItemAtPosition(0)).contains("My Computer");
        }
        return false;
    }

    public void split_drives(String recieved_msg) {

        List<String> drives_namearray = new ArrayList<String>();
        List<Integer> drives_imagearray = new ArrayList<Integer>();

        drives_list = recieved_msg.split(";");

        for (int j = 0; j < drives_list.length; j++) {
            String drive = drives_list[j];
            if (drive.charAt(0) == '0') {
                drives_namearray.add(drive.substring(1));
                drives_imagearray.add(R.drawable.pc_material_icon);
            } else if (drive.charAt(0) == '1') {
                drives_namearray.add(drive.substring(1));
                drives_imagearray.add(R.drawable.folder_material_icon);
            } else if (drive.charAt(0) == '2') {
                drives_namearray.add(drive.substring(1));
                drives_imagearray.add(R.drawable.file_material_icon);
            }
        }
        items_nameArray = new String[drives_namearray.size()];
        items_imagesArray = new Integer[drives_imagearray.size()];

        drives_namearray.toArray(items_nameArray);
        drives_imagearray.toArray(items_imagesArray);
    }

    public void create_list(String recieved_msg) {

        Drives_list_view = (ListView) findViewById(R.id.DrivesList);
        RelativeLayout empty_folder_view = (RelativeLayout) findViewById(R.id.empty_folder_layout);
        empty_folder_view.setVisibility(RelativeLayout.GONE);
        items_nameArray = new String[0];
        items_imagesArray = new Integer[0];

        if (recieved_msg == null || recieved_msg.isEmpty()) {
            ArrayAdapter<String> adapter = new CustomAdapter(this, items_nameArray, items_imagesArray);
            Drives_list_view.setAdapter(adapter);
            Drives_list_view.setFocusable(true);
            empty_folder_view.setVisibility(RelativeLayout.VISIBLE);
            return;
        }

        if(garbage_date_recieved(recieved_msg)){
            disconnect();
            return;
        }
        split_drives(recieved_msg);
        ArrayAdapter<String> adapter = new CustomAdapter(this, items_nameArray, items_imagesArray);
        Drives_list_view.setAdapter(adapter);
        Drives_list_view.setFocusable(true);

        Drives_list_view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        RelativeLayout loading_layout = (RelativeLayout) findViewById(R.id.LoadingLayout);
                        if (loading_layout.getVisibility() == RelativeLayout.VISIBLE) {
                            return;
                        }
                        String value = (String) Drives_list_view.getItemAtPosition(i);
                        if (drives_list[i].charAt(0) == '1') {
                            path += value + "\\";
                            send(path, i);
                        } else if (drives_list[i].charAt(0) == '0') {
                            send("My Computer", -1);
                        } else if (drives_list[i].charAt(0) == '2') {
                            receivefile(value, path + value);
                        }
                    }
                }
        );
    }

    public Boolean garbage_date_recieved(String msg){
        // if msg does not start with 0,1 or 2 disconnect
        if(msg.charAt(0)=='0' || msg.charAt(0)=='1' || msg.charAt(0)=='2'){
            return false;
        }
        return true;
    }

    public void goback() {

        if (athome()) {
            super.onBackPressed(); // go back to previous activity
        }
        if (path == "" || path == null || (path.indexOf('\\') == -1))
            reset();
        else {
            String temp = path.substring(0, path.lastIndexOf('\\'));
            // Log.i(TAG,"1st "+temp);
            String temp2 = temp.substring(0, temp.lastIndexOf('\\') + 1);
            //Log.i(TAG, temp2);
            if (temp2.length() == temp.length()) {
                path = "";
                send("My Computer", -1);
            } else {
                path = temp2;
                send(path, 1);
            }
        }

    }

    public void refresh() {
        send(path, 1);
    }

    @Override
    protected void onResume() {
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        hideloadingscreen();
        super.onResume();
    }

    @Override
    protected void onPause() {
        socketServiceObject.downloadingfile = false;
        socketServiceObject.sendMessage("syncback");
        unbindService(serviceConnection);
        pd.dismiss();
        super.onPause();
    }

    public void reset() {
        path = "";
        create_list("0My Computer");
    }


    public void showloadingscreen() {
        RelativeLayout loading_layout = (RelativeLayout) findViewById(R.id.LoadingLayout);
        loading_layout.setVisibility(RelativeLayout.VISIBLE);
    }


    public void hideloadingscreen() {
        RelativeLayout loading_layout = (RelativeLayout) findViewById(R.id.LoadingLayout);
        loading_layout.setVisibility(RelativeLayout.GONE);
    }

    public void send(String v, int p) {
        showloadingscreen();
        if (p == -1) {
            socketServiceObject.sendMessage("&0" + v);
        } else {
            //Toast.makeText(Explorer.this, drives_list[p] + v, Toast.LENGTH_LONG).show();
            socketServiceObject.sendMessage("&1" + v);
        }

        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    final String recieved_message = socketServiceObject.recieveMessage();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            hideloadingscreen();
                            create_list(recieved_message);
                        }
                    });
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), e.getMessage());
                }
            }

        };
        t.start();
    }

    public void receivefile(final String filename, String path) {
        socketServiceObject.sendMessage("&2" + path);
        pd.setTitle("Please Wait...");
        pd.setMessage("Downloading to Storage/Download/Remote Devices/");
        pd.show();
        socketServiceObject.percentdownloaded = 0;

        pd.setProgress(socketServiceObject.percentdownloaded);
        pd.setCancelable(false);


        socketServiceObject.downloadingfile = true;
        final Thread recieve_file_thread = new Thread() {
            @Override
            public void run() {
                try {
                    socketServiceObject.recievefile(filename);
                } catch (IOException e) {
                    Log.e(this.getClass().toString(), e.getMessage());
                }
            }
        };

        final Thread update_pd_thread = new Thread() {
            @Override
            public void run() {
                int time_elapsed = 0;
                while (socketServiceObject.percentdownloaded < 100 && socketServiceObject.downloadingfile) {
                    try {
                        pd.setProgress(socketServiceObject.percentdownloaded);
                        sleep(200);
                        time_elapsed += 2;
                        if (time_elapsed > 100 && socketServiceObject.percentdownloaded < 1) {
                            //something is wrong... :(
                            //break;
                        }
                    } catch (Exception e) {
                        Log.e(this.getClass().toString(), e.getMessage());
                    }
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                pd.setProgress(socketServiceObject.percentdownloaded);
                                if (socketServiceObject.percentdownloaded > 99) {
                                    pd.setTitle("Completed :)");
                                    pd.setMessage("Downloaded to Storage/Download/Remote Devices/");
                                }
                                socketServiceObject.downloadingfile = false;
                            }
                        }
                );
            }
        };

        recieve_file_thread.start();
        update_pd_thread.start();
    }

    public static class CustomAdapter extends ArrayAdapter<String> {
        private final String[] drives_nameArray;
        private final Integer[] drives_imagesArray;

        public CustomAdapter(Context context, String[] drives_nameArray, Integer[] drives_imagesArray) {
            super(context, R.layout.explorer_list_layout, drives_nameArray);
            this.drives_imagesArray = drives_imagesArray;
            this.drives_nameArray = drives_nameArray;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.explorer_list_layout, parent, false);

            String oneItem = getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.drive_name);
            ImageView imageView = (ImageView) view.findViewById(R.id.drive_image);

            textView.setText(oneItem);
            imageView.setImageResource(drives_imagesArray[position]);
            return view;
        }
    }

    public void disconnect(){
        if(socketServiceObject!=null)
            socketServiceObject.disconnect();

        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, Connect.class);
        startActivity(i);
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
