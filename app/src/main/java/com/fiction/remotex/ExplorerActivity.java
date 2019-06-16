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
    SocketService objectservice;
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
                objectservice.downloadingfile = false;
                if (objectservice.percentdownloaded < 100) {
                    showloadingscreen();
                    final Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                android.os.SystemClock.sleep(100);
                                while (objectservice.cleaning_stream) {
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
        if (objectservice.downloadingfile || pd.isShowing()) {
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
                            sendfile(value, path + value);
                        }
                    }
                }
        );
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
        objectservice.downloadingfile = false;
        unbindService(serviceConnection);
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
            objectservice.sendMessage("&0" + v);
        } else {
            //Toast.makeText(Explorer.this, drives_list[p] + v, Toast.LENGTH_LONG).show();
            objectservice.sendMessage("&1" + v);
        }

        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    final String recieved_message = objectservice.recieveMessage();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            hideloadingscreen();
                            create_list(recieved_message);
                        }
                    });
                } catch (Exception e) {
                    Log.e(this.getClass().toString(),e.getMessage());
                }
            }

        };
        t.start();
    }

    public void sendfile(final String filename, String path) {
        objectservice.sendMessage("&2" + path);
        pd.setTitle("Please Wait...");
        pd.setMessage("Downloading to Storage/Download/Remote Devices/");
        pd.show();
        objectservice.percentdownloaded = 0;

        pd.setProgress(objectservice.percentdownloaded);
        pd.setCancelable(false);


        objectservice.downloadingfile = true;
        final Thread send_file_path_thread = new Thread() {
            @Override
            public void run() {
                try {
                    objectservice.recievefile(filename);
                } catch (IOException e) {
                    Log.e(this.getClass().toString(),e.getMessage());
                }
            }
        };

        final Thread update_pd_thread = new Thread() {
            @Override
            public void run() {
                int progress = 0;
                while (objectservice.percentdownloaded < 100 && objectservice.downloadingfile) {
                    try {
                        pd.setProgress(objectservice.percentdownloaded);
                        sleep(200);
                        progress += 2;
                        if (progress > 100 && objectservice.percentdownloaded < 1)
                            break;
                    } catch (Exception e) {
                        Log.e(this.getClass().toString(),e.getMessage());
                    }
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                pd.setProgress(objectservice.percentdownloaded);
                                pd.setTitle("Completed :)");
                                pd.setMessage("Downloaded to Storage/Download/Remote Devices/");
                                objectservice.downloadingfile = false;
                            }
                        }
                );
            }
        };

        send_file_path_thread.start();
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
