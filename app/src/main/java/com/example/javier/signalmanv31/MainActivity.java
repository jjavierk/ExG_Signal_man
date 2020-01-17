package com.example.javier.signalmanv31;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    int bits = 28;
    ListView lv;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter BA = null;
    int lineWidth;
    int downsample_value;
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    boolean cancelBits = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv = (ListView) findViewById(R.id.listView);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                expand(extract_addr(adapterView.getItemAtPosition(i).toString()));
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        BA = BluetoothAdapter.getDefaultAdapter();

        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        }

        askExternalStoragePermission();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
/*
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
    }


    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
*/


    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
            {
                Extra_settings();
                break;
            }
        }
        return true;
    }

    public void list(View v) {
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices)
            list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.custom, list);
        lv.setAdapter(adapter);
    }

    String extract_addr(String Name) {
        pairedDevices = BA.getBondedDevices();
        ArrayList list = new ArrayList();

        String rVal = "";

        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().contains(Name)) {
                rVal = bt.getAddress();
                break;
            }
        }

        return rVal;

    }


    private int mResult;

    public int getYesNoWithExecutionStop(String title, String message, Context context) {
        // make a handler that throws a runtime exception when a message is received
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setIcon(R.drawable.head_qs);

        alert.setPositiveButton("16 Bits", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult = 22;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.setNegativeButton("24 Bits", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult = 28;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.show();


        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (RuntimeException e2) {
        }

        return mResult;
    }


    public void expand(String address) {

        //bits = getYesNoWithExecutionStop("Bits of Operation", "Please, choose a number to continue...", this);

        SelectNumberOfBits();

        if(cancelBits==false) {

            Intent i = new Intent(this, RealtimeLineChartActivity.class);
            i.putExtra("address", address);
            i.putExtra("bits", Integer.toString(bits));
            startActivity(i);
        }

    }


    void Extra_settings() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setMessage("     Line Width \n ");

// Set up the input
        final NumberPicker input = new NumberPicker(this);
        final NumberPicker input2 = new NumberPicker(this);

        read_preferences();
        input.setMaxValue(10);
        input.setMinValue(0);
        input.setValue(lineWidth);

        input2.setMaxValue(500);
        input2.setMinValue(1);
        input2.setValue(downsample_value);

        int left = 6;
        int top = 6;
        int right = 6;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams.WRAP_CONTENT, com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);
        input.setLayoutParams(params);
        input2.setLayoutParams(params);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setDividerPadding(1000);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        linearLayout.addView(input);
        linearLayout.addView(input2);

        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                lineWidth = input.getValue();
                downsample_value = input2.getValue();
                save_preferences();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void save_preferences() {

        SharedPreferences preferencias = getSharedPreferences("sman_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("lineWidth", Integer.toString(lineWidth));
        editor.putString("downsample_value", Integer.toString(downsample_value));
        editor.putString("bitsNumber", Integer.toString(bits));


        editor.commit();

    }

    void read_preferences() {

        SharedPreferences prefe = getSharedPreferences("sman_data", Context.MODE_PRIVATE);

        if (!prefe.getString("lineWidth", "").equals(""))
            lineWidth = Integer.parseInt(prefe.getString("lineWidth", ""));
        else lineWidth = 2;

        if (!prefe.getString("downsample_value", "").equals(""))
            downsample_value = Integer.parseInt(prefe.getString("downsample_value", ""));
        else downsample_value = 5;

        if (!prefe.getString("bitsNumber", "").equals(""))
            bits = Integer.parseInt(prefe.getString("bitsNumber", ""));
        else bits = 28;



    }


    void askExternalStoragePermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    public void SelectNumberOfBits() {
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    // Must use message queue to show keyboard
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(v, 0);
                        }
                    });
                }
            }
        };

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams.WRAP_CONTENT, com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);

        read_preferences();
        final NumberPicker input = new NumberPicker(this);
        input.setMaxValue(1000);
        input.setMinValue(1);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setValue(bits);
        input.setLayoutParams(params);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.addView(input);

        DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Done button clicked
                        bits = input.getValue();
                        save_preferences();
                        cancelBits = false;
                        handler.sendMessage(handler.obtainMessage());


                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        cancelBits = true;
                        handler.sendMessage(handler.obtainMessage());
                        // Cancel button clicked
                        //do nothing
                        break;
                }
            }
        };


        final android.support.v7.app.AlertDialog alertDialog = (new android.support.v7.app.AlertDialog.Builder(this)).setCustomTitle(format_textview("Select Data Buffer Length"))
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();




        alertDialog.show();

        try {
            Looper.loop();
        } catch (RuntimeException e2) {
        }
    }

    TextView format_textview(String message) {

        TextView title = new TextView(this);
// You Can Customise your Title here
        title.setText(message);
        title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        return title;


    }
}
