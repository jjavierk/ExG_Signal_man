
package com.example.javier.signalmanv31;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
//import com.xxmassdeveloper.mpchartexample.notimportant.DemoBase;

public class RealtimeLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener, OnChartGestureListener {

    private LineChart mChart;

    private LineChart mChart_psd;
    int readBufferPosition;
    int lineWidth = 2;
    Boolean alredy_running = false;

    Timer T = new Timer();

    int displayValue = 200;
    int yMaxValue = 300;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    YAxis leftAxis;
    String nameFile;
    OutputStream mmOutputStream;
    private BluetoothReadThread readThread;
    boolean playing = false;
    boolean psd_time_bol = false;
    int ChannelsToShow = 0;
    int ChannelTOComputePSD;
    int N = 1024;
    double[] fft_mag;
    int count;

    boolean wait;
    boolean ft = true;
    int num_rec_samples;
    int scaling_psd = 100000;
    TextView textView2;
    TextView txtCOutput;

    boolean keep_transmitting = false;



    double[] circular_buffer;

    int PK_Counter_int = 0;
    int downsample;
    TextView Header;
    TextView PK_ID;

    int PK_ID_int;
    int Header_int;


    boolean write_open;
    boolean captureStateShowValues = false;
    boolean sound_enable = false;
    OutputStreamWriter osw = null;
    boolean saving = false;
    int centerCh1;
    int centerCh2;
    int centerCh3;
    int centerCh4;
    int centerCh5;
    int centerCh6;
    int centerCh7;
    int centerCh8;
    int ch1;
    int ch2;
    int ch3;
    int ch4;
    int ch5;
    int ch6;
    int ch7;
    int ch8;

    boolean[] activateChannels = {false, false, false, false, false, false, false, false};
    int scaling = 100;
    int prevSelection;
    int downsample_value = 10;
    boolean stream_enable = false;
    int scaCh1, scaCh2, scaCh3, scaCh4, scaCh5, scaCh6, scaCh7, scaCh8;
    ImageView rlIcon1;
    ImageView rlIcon2;
    ImageView rlIcon3;
    ImageView rlIcon4;
    ImageView rlIcon5;
    private static String address = "00:13:12:23:56:18";
    int bitsExpected;
    static int[][] readbuffer_copy;
    private static final String TAG = "bluetooth2";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    static int[] readBuffer;
    String startTring = "j";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_realtime_linechart);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        readBuffer = new int[10240];
        textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setVisibility(View.INVISIBLE);
        textView2.setTextColor(Color.WHITE);

        txtCOutput = (TextView) findViewById(R.id.txtCOutput);
        txtCOutput.setTextColor(Color.WHITE);



        circular_buffer = new double[N];
        readbuffer_copy = new int[1024][14];
        fft_mag = new double[N];


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        read_preferences();
        ChannelsToShow = yesNoStop();
        iniChannels(ChannelsToShow);
        ini_psd_graphic();
        buttons_ini_and_handlers();
        bt_init();


    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }


    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAdd: {
                addEntry();
                break;
            }
            case R.id.actionClear: {
                mChart.clearValues();
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.actionFeedMultiple: {
                feedMultiple();
                break;
            }
        }
        return true;
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh1), 0);

            if (activateChannels[1]) {
                set = data.getDataSetByIndex(1);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh2), 1);
            }

            if (activateChannels[2]) {
                set = data.getDataSetByIndex(2);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh3), 2);
            }

            if (activateChannels[3]) {
                set = data.getDataSetByIndex(3);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh4), 3);
            }

            if (activateChannels[4]) {
                set = data.getDataSetByIndex(4);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh5), 4);
            }

            if (activateChannels[5]) {
                set = data.getDataSetByIndex(5);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh6), 5);
            }

            if (activateChannels[6]) {
                set = data.getDataSetByIndex(6);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh7), 6);
            }

            if (activateChannels[7]) {
                set = data.getDataSetByIndex(7);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f + centerCh8), 7);
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(displayValue);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private void addEntry(int chn1, int chn2, int chn3, int chn4, int chn5, int chn6, int chn7, int chn8) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            data.addEntry(new Entry(set.getEntryCount(), (float) chn1 + centerCh1), 0);

            if (activateChannels[1]) {
                set = data.getDataSetByIndex(1);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn2 + centerCh2), 1);
            }

            if (activateChannels[2]) {
                set = data.getDataSetByIndex(2);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn3 + centerCh3), 2);
            }

            if (activateChannels[3]) {
                set = data.getDataSetByIndex(3);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn4 + centerCh4), 3);
            }

            if (activateChannels[4]) {
                set = data.getDataSetByIndex(4);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn5 + centerCh5), 4);
            }

            if (activateChannels[5]) {
                set = data.getDataSetByIndex(5);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn6 + centerCh6), 5);
            }

            if (activateChannels[6]) {
                set = data.getDataSetByIndex(6);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn7 + centerCh7), 6);
            }

            if (activateChannels[7]) {
                set = data.getDataSetByIndex(7);
                data.addEntry(new Entry(set.getEntryCount(), (float) chn8 + centerCh8), 7);
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(displayValue);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Channel 1");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet2() {

        LineDataSet set = new LineDataSet(null, "Channel 2");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#8BC34A"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet3() {

        LineDataSet set = new LineDataSet(null, "Channel 3");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#EF6C00"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#EF6C00"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet4() {

        LineDataSet set = new LineDataSet(null, "Channel 4");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#607D8B"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#607D8B"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet5() {

        LineDataSet set = new LineDataSet(null, "Channel 5");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#E91E63"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#E91E63"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet6() {

        LineDataSet set = new LineDataSet(null, "Channel 6");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#F44336"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#F44336"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet7() {

        LineDataSet set = new LineDataSet(null, "Channel 7");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#D500F9"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#D500F9"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet8() {

        LineDataSet set = new LineDataSet(null, "Channel 8");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb("#2196F3"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(lineWidth);
        set.setCircleRadius(4f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb("#2196F3"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    void iniChannels(int Channels) {

        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        Typeface mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // modify the legend ...
        //l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setForm(LegendForm.LINE);
        l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);
        l.setDrawInside(true);
        l.setWordWrapEnabled(true);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(yMaxValue);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        data = mChart.getData();
        ILineDataSet set;

        for (int i = 0; i < 8; i++)

        {
            boolean setter;
            if (i < Channels) setter = true;
            else setter = false;

            activateChannels[i] = setter;

        }


        if (activateChannels[0] == true) {


            set = createSet();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh1), 0);
            }
        }

        if (activateChannels[1] == true) {
            set = createSet2();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh2), 1);
            }
        }


        if (activateChannels[2] == true) {
            set = createSet3();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh3), 2);
            }
        }

        if (activateChannels[3] == true) {
            set = createSet4();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh4), 3);
            }
        }

        if (activateChannels[4] == true) {
            set = createSet5();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh5), 4);
            }
        }

        if (activateChannels[5] == true) {
            set = createSet6();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh6), 5);
            }
        }

        if (activateChannels[6] == true) {
            set = createSet7();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh7), 6);
            }
        }

        if (activateChannels[7] == true) {
            set = createSet8();
            data.addDataSet(set);

            for (int i = 0; i <= displayValue; i++) {
                data.addEntry(new Entry(set.getEntryCount(), (float) centerCh8), 7);
            }
        }
    }

    void read_preferences() {

        SharedPreferences prefe = getSharedPreferences("sman_data", Context.MODE_PRIVATE);

        if (!prefe.getString("posCH1", "").equals(""))
            centerCh1 = Integer.parseInt(prefe.getString("posCH1", ""));
        else centerCh1 = 30;

        if (!prefe.getString("posCH2", "").equals(""))
            centerCh2 = Integer.parseInt(prefe.getString("posCH2", ""));
        else centerCh2 = 60;

        if (!prefe.getString("posCH3", "").equals(""))
            centerCh3 = Integer.parseInt(prefe.getString("posCH3", ""));
        else centerCh3 = 90;

        if (!prefe.getString("posCH4", "").equals(""))
            centerCh4 = Integer.parseInt(prefe.getString("posCH4", ""));
        else centerCh4 = 120;

        if (!prefe.getString("posCH5", "").equals(""))
            centerCh5 = Integer.parseInt(prefe.getString("posCH5", ""));
        else centerCh5 = 150;

        if (!prefe.getString("posCH6", "").equals(""))
            centerCh6 = Integer.parseInt(prefe.getString("posCH6", ""));
        else centerCh6 = 180;

        if (!prefe.getString("posCH7", "").equals(""))
            centerCh7 = Integer.parseInt(prefe.getString("posCH7", ""));
        else centerCh7 = 210;

        if (!prefe.getString("posCH8", "").equals(""))
            centerCh8 = Integer.parseInt(prefe.getString("posCH8", ""));
        else centerCh8 = 240;


        if (!prefe.getString("Scaling", "").equals(""))
            scaling = Integer.parseInt(prefe.getString("Scaling", ""));
        else scaling = 800;

        if (!prefe.getString("xMaxValue", "").equals(""))
            displayValue = Integer.parseInt(prefe.getString("xMaxValue", ""));
        else displayValue = 200;

        if (!prefe.getString("yMaxValue", "").equals(""))
            yMaxValue = Integer.parseInt(prefe.getString("yMaxValue", ""));
        else yMaxValue = 300;

        if (!prefe.getString("numberOfChannel", "").equals(""))
            prevSelection = Integer.parseInt(prefe.getString("numberOfChannel", ""));
        else prevSelection = 2;

        if (!prefe.getString("lineWidth", "").equals(""))
            lineWidth = Integer.parseInt(prefe.getString("lineWidth", ""));
        else lineWidth = 2;

        if (!prefe.getString("downsample_value", "").equals(""))
            downsample_value = Integer.parseInt(prefe.getString("downsample_value", ""));
        else downsample_value = 10;

        if (!prefe.getString("scaCh1", "").equals(""))
            scaCh1 = Integer.parseInt(prefe.getString("scaCh1", ""));
        else scaCh1 = 100;

        if (!prefe.getString("scaCh2", "").equals(""))
            scaCh2 = Integer.parseInt(prefe.getString("scaCh2", ""));
        else scaCh2 = 100;

        if (!prefe.getString("scaCh3", "").equals(""))
            scaCh3 = Integer.parseInt(prefe.getString("scaCh3", ""));
        else scaCh3 = 100;

        if (!prefe.getString("scaCh4", "").equals(""))
            scaCh4 = Integer.parseInt(prefe.getString("scaCh4", ""));
        else scaCh4 = 100;

        if (!prefe.getString("scaCh5", "").equals(""))
            scaCh5 = Integer.parseInt(prefe.getString("scaCh5", ""));
        else scaCh5 = 100;

        if (!prefe.getString("scaCh6", "").equals(""))
            scaCh6 = Integer.parseInt(prefe.getString("scaCh6", ""));
        else scaCh6 = 100;

        if (!prefe.getString("scaCh7", "").equals(""))
            scaCh7 = Integer.parseInt(prefe.getString("scaCh7", ""));
        else scaCh7 = 100;

        if (!prefe.getString("scaCh8", "").equals(""))
            scaCh8 = Integer.parseInt(prefe.getString("scaCh8", ""));
        else scaCh8 = 100;

        if (!prefe.getString("scaPSD", "").equals(""))
            scaling_psd = Integer.parseInt(prefe.getString("scaPSD", ""));
        else scaling_psd = 100000;



    }

    public void save_preferences() {

        SharedPreferences preferencias = getSharedPreferences("sman_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("posCH1", Integer.toString(centerCh1));
        editor.putString("posCH2", Integer.toString(centerCh2));
        editor.putString("posCH3", Integer.toString(centerCh3));
        editor.putString("posCH4", Integer.toString(centerCh4));
        editor.putString("posCH5", Integer.toString(centerCh5));
        editor.putString("posCH6", Integer.toString(centerCh6));
        editor.putString("posCH7", Integer.toString(centerCh7));
        editor.putString("posCH8", Integer.toString(centerCh8));
        editor.putString("Scaling", Integer.toString(scaling));
        editor.putString("xMaxValue", Integer.toString(displayValue));
        editor.putString("yMaxValue", Integer.toString(yMaxValue));
        editor.putString("numberOfChannel", Integer.toString(prevSelection));

        editor.putString("scaCh1", Integer.toString(scaCh1));
        editor.putString("scaCh2", Integer.toString(scaCh2));
        editor.putString("scaCh3", Integer.toString(scaCh3));
        editor.putString("scaCh4", Integer.toString(scaCh4));
        editor.putString("scaCh5", Integer.toString(scaCh5));
        editor.putString("scaCh6", Integer.toString(scaCh6));
        editor.putString("scaCh7", Integer.toString(scaCh7));
        editor.putString("scaCh8", Integer.toString(scaCh8));
        editor.putString("scaPSD", Integer.toString(scaling_psd));


        editor.commit();

    }

    private int mResult;

    public int yesNoStop() {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final NumberPicker input = new NumberPicker(this);
        input.setMaxValue(8);
        input.setMinValue(1);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setValue(prevSelection);
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

                        mResult = input.getValue();
                        handler.sendMessage(handler.obtainMessage());
                        prevSelection = input.getValue();
                        save_preferences();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mResult = 2;
                        Toast.makeText(RealtimeLineChartActivity.this, "Default Value has been selected", Toast.LENGTH_SHORT).show();
                        handler.sendMessage(handler.obtainMessage());
                        // Cancel button clicked
                        break;
                }
            }
        };


        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Channels")).setMessage("Select Number of channels")
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();


        alertDialog.show();

        try {
            Looper.loop();
        } catch (RuntimeException e2) {
        }

        return mResult;

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

    void buttons_ini_and_handlers() {


        // Set up the white button on the lower right corner
        // more or less with default parameter
        final ImageView fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.gear_128));
        final FloatingActionButton rightLowerButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .setPosition(4)
                .build();

        final ImageView play_stop_ico = new ImageView(this);
        play_stop_ico.setImageDrawable(getResources().getDrawable(R.drawable.play_128_v2));
        final FloatingActionButton play_stop = new FloatingActionButton.Builder(this)
                .setContentView(play_stop_ico)
                .setPosition(2)
                .build();

        //final ImageView kaka_ico = new ImageView(this);
        //kaka_ico.setImageDrawable(getResources().getDrawable(R.drawable.kaka));
        //final FloatingActionButton kaka_send_str = new FloatingActionButton.Builder(this)
        //          .setContentView(kaka_ico)
        //          .setPosition(8)
        //          .build();


        final ImageView save_ico = new ImageView(this);
        save_ico.setImageDrawable(getResources().getDrawable(R.drawable.floppy_disk_128));
        final FloatingActionButton save_btn = new FloatingActionButton.Builder(this)
                .setContentView(save_ico)
                .setPosition(6)
                .build();

        /*
        final ImageView maker_ico = new ImageView(this);
        maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.eyes_closed));
        final FloatingActionButton marker_btn = new FloatingActionButton.Builder(this)
                .setContentView(maker_ico)
                .setPosition(5)
                .build();


        final ImageView Time_PSD_Show_img = new ImageView(this);
        save_ico.setImageDrawable(getResources().getDrawable(R.drawable.signal));
        final FloatingActionButton PSD_Time_btn = new FloatingActionButton.Builder(this)
                .setContentView(save_ico)
                .setPosition(6)
                .build();

        marker_btn.setEnabled(false);
        marker_btn.setVisibility(View.INVISIBLE);

        marker_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bolOpenClose = !bolOpenClose;

                if (bolOpenClose)
                    maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.eyes_marker_open));
                else maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.eyes_closed));

            }
        });
*/
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saving = !saving;

                if (saving) {
                    Toast.makeText(RealtimeLineChartActivity.this, "Click here again to save the file after you finish capturing data", Toast.LENGTH_SHORT).show();
                    save_ico.setImageDrawable((getResources().getDrawable(R.drawable.save_ico_saved)));
                    onClickCheckToSave();
                    write_open = true;

                    textView2.setVisibility(View.VISIBLE);
                    count = 0;

                    if (!alredy_running) {
                        T.scheduleAtFixedRate(new TimerTask() {


                            @Override
                            public void run() {
                                alredy_running = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (count <= 9)
                                            textView2.setText("T=00:0" + count);
                                        else
                                            textView2.setText("T=00:" + count);


                                    }

                                });

                                count++;


                            }

                        }, 1000, 1000);
                    }


                    //marker_btn.setEnabled(true);
                    //marker_btn.setVisibility(View.VISIBLE);

                } else {
                    save_ico.setImageDrawable((getResources().getDrawable(R.drawable.floppy_disk_128)));
                    try {
                        write_open = false;
                        textView2.setVisibility(View.INVISIBLE);
                        //T.purge();

                        saveFile();
                        askOpenFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //marker_btn.setEnabled(false);
                    //marker_btn.setVisibility(View.INVISIBLE);
                }
            }
        });

        final ImageView maker_ico = new ImageView(this);
        maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.psd));
        final FloatingActionButton psd_signal_btn = new FloatingActionButton.Builder(this)
                .setContentView(maker_ico)
                .setPosition(8)
                .build();


        psd_signal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                psd_time_bol = !psd_time_bol;

                if (psd_time_bol) {
                    maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.signal));
                    mChart_psd.setVisibility(View.VISIBLE);
                    ChannelSelectDialog();
                } else {
                    maker_ico.setImageDrawable(getResources().getDrawable(R.drawable.psd));
                    mChart_psd.setVisibility(View.INVISIBLE);
                }

            }
        });


        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(this);


        rlIcon1 = new ImageView(this);
        rlIcon2 = new ImageView(this);
        rlIcon3 = new ImageView(this);
        rlIcon4 = new ImageView(this);
        rlIcon5 = new ImageView(this);


        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.test));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.num_mag));
        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.line_128));
        rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.values_ico_gray));
        rlIcon5.setImageDrawable(getResources().getDrawable(R.drawable.easel));

        // Build the menu with default options: light theme, 90 degrees, 72dp radius.
        // Set 4 default SubActionButtons
        final FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon4).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon5).build())
                .attachTo(rightLowerButton)
                .build();
        rightLowerMenu.updateItemPositions();

        final FloatingActionMenu play_stop_menu = new FloatingActionMenu.Builder(this)
                .attachTo(play_stop)
                .build();
        play_stop_menu.updateItemPositions();


        // Listen menu open and close events to animate the button content view
        rightLowerMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                fabIconNew.setRotation(90);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                fabIconNew.setRotation(120);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }

        });


        /*kaka_ico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            if (keep_transmitting == false) { keep_transmitting = true;  run_write_data(100);}
                else keep_transmitting = false;


            }

        });
*/

        play_stop_ico.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                playing = !playing;
                if (playing) {
                    play_stop_ico.setImageDrawable((getResources().getDrawable(R.drawable.stop_ico_v2)));


                    String startTring = "=";
                    try {
                        mmOutputStream.write(startTring.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stream_enable = true;


                } else {
                    play_stop_ico.setImageDrawable((getResources().getDrawable(R.drawable.play_128_v2)));


                    String startTring = ":";
                    try {
                        mmOutputStream.write(startTring.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    stream_enable = false;

                }

            }

        });


        rlIcon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (psd_time_bol) shw_wel_msg();

            }
        });


        rlIcon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scalingMsgbox();
                save_preferences();


            }
        });

        rlIcon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showInput();
                save_preferences();


            }
        });


        rlIcon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                captureStateShowValues = !captureStateShowValues;


                int x;

                if (captureStateShowValues) {
                    x = View.VISIBLE;
                    rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.values_ico));
                } else {
                    x = View.INVISIBLE;
                    rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.values_ico_gray));
                }

/*
                lv1.setVisibility(x);
                value1.setVisibility(x);

                lv2.setVisibility(x);
                value2.setVisibility(x);

                lv3.setVisibility(x);
                value3.setVisibility(x);

                lv4.setVisibility(x);
                value4.setVisibility(x);

                lhe.setVisibility(x);
                Header.setVisibility(x);

                lid.setVisibility(x);
                PK_ID.setVisibility(x);

                lPK_C.setVisibility(x);
                Counter.setVisibility(x);
                */

            }
        });


        rlIcon5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                xYlimits();
                save_preferences();


            }
        });

    }

    public void xYlimits() {
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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final NumberPicker input = new NumberPicker(this);
        input.setMaxValue(1000);
        input.setMinValue(0);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setValue(displayValue);
        input.setLayoutParams(params);

        final NumberPicker input2 = new NumberPicker(this);
        input2.setMaxValue(1000);
        input2.setMinValue(0);
        input2.setGravity(Gravity.CENTER_HORIZONTAL);
        input2.setValue(yMaxValue);
        input2.setLayoutParams(params);

        final NumberPicker input3 = new NumberPicker(this);
        input2.setMaxValue(1000);
        input2.setMinValue(0);
        input2.setGravity(Gravity.CENTER_HORIZONTAL);
        input2.setValue(yMaxValue);
        input2.setLayoutParams(params);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.addView(input);
        linearLayout.addView(input2);

        DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Done button clicked
                        displayValue = input.getValue();
                        yMaxValue = input2.getValue();
                        leftAxis.setAxisMaxValue(yMaxValue);

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked
                        break;
                }
            }
        };


        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Plot Limits (x,y)"))
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();


        alertDialog.show();
    }

    public void ChannelSelectDialog() {
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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final NumberPicker input = new NumberPicker(this);
        input.setMaxValue(ChannelsToShow);
        input.setMinValue(1);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setValue(displayValue);
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
                        ChannelTOComputePSD = input.getValue();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked
                        //do nothing
                        break;
                }
            }
        };


        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Select Input Channel"))
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();


        alertDialog.show();
    }

    public void showInput() {
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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final NumberPicker input = new NumberPicker(this);
        final NumberPicker input2 = new NumberPicker(this);
        final NumberPicker input3 = new NumberPicker(this);
        final NumberPicker input4 = new NumberPicker(this);
        final NumberPicker input5 = new NumberPicker(this);
        final NumberPicker input6 = new NumberPicker(this);
        final NumberPicker input7 = new NumberPicker(this);
        final NumberPicker input8 = new NumberPicker(this);

        if (activateChannels[0]) {
            input.setMaxValue(yMaxValue);
            input.setMinValue(0);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setValue(centerCh1);
            input.setLayoutParams(params);
            linearLayout.addView(input);

        }

        if (activateChannels[1]) {
            input2.setMaxValue(yMaxValue);
            input2.setMinValue(0);
            input2.setGravity(Gravity.CENTER_HORIZONTAL);
            input2.setValue(centerCh2);
            input2.setLayoutParams(params);
            linearLayout.addView(input2);
        }

        if (activateChannels[2]) {
            input3.setMaxValue(yMaxValue);
            input3.setMinValue(0);
            input3.setGravity(Gravity.CENTER_HORIZONTAL);
            input3.setValue(centerCh3);
            input4.setLayoutParams(params);
            linearLayout.addView(input3);
        }

        if (activateChannels[3]) {
            input4.setMaxValue(yMaxValue);
            input4.setMinValue(0);
            input4.setGravity(Gravity.CENTER_HORIZONTAL);
            input4.setValue(centerCh4);
            input4.setLayoutParams(params);
            linearLayout.addView(input4);
        }

        if (activateChannels[4]) {
            input5.setMaxValue(yMaxValue);
            input5.setMinValue(0);
            input5.setGravity(Gravity.CENTER_HORIZONTAL);
            input5.setValue(centerCh5);
            input5.setLayoutParams(params);
            linearLayout.addView(input5);
        }

        if (activateChannels[5]) {
            input6.setMaxValue(yMaxValue);
            input6.setMinValue(0);
            input6.setGravity(Gravity.CENTER_HORIZONTAL);
            input6.setValue(centerCh6);
            input6.setLayoutParams(params);
            linearLayout.addView(input6);
        }

        if (activateChannels[6]) {
            input7.setMaxValue(yMaxValue);
            input7.setMinValue(0);
            input7.setGravity(Gravity.CENTER_HORIZONTAL);
            input7.setValue(centerCh7);
            input7.setLayoutParams(params);
            linearLayout.addView(input7);
        }

        if (activateChannels[7]) {
            input8.setMaxValue(yMaxValue);
            input8.setMinValue(0);
            input8.setGravity(Gravity.CENTER_HORIZONTAL);
            input8.setValue(centerCh8);
            input8.setLayoutParams(params);
            linearLayout.addView(input8);
        }

        DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Done button clicked

                        if (activateChannels[0]) centerCh1 = input.getValue();
                        if (activateChannels[1]) centerCh2 = input2.getValue();
                        if (activateChannels[2]) centerCh3 = input3.getValue();
                        if (activateChannels[3]) centerCh4 = input4.getValue();
                        if (activateChannels[4]) centerCh5 = input5.getValue();
                        if (activateChannels[5]) centerCh6 = input6.getValue();
                        if (activateChannels[6]) centerCh8 = input7.getValue();
                        if (activateChannels[7]) centerCh8 = input8.getValue();
                        save_preferences();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:

                        int separation = yMaxValue / prevSelection;

                        switch (prevSelection) {
                            case 1:
                                centerCh1 = separation;
                            case 2:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;

                            case 3:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;

                            case 4:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;
                                centerCh4 = separation + separation + separation + separation / 2;

                            case 5:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;
                                centerCh4 = separation + separation + separation + separation / 2;
                                centerCh5 = separation + separation + separation + separation + separation / 2;

                            case 6:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;
                                centerCh4 = separation + separation + separation + separation / 2;
                                centerCh5 = separation + separation + separation + separation + separation / 2;
                                centerCh6 = separation + separation + separation + separation + separation + separation / 2;


                            case 7:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;
                                centerCh4 = separation + separation + separation + separation / 2;
                                centerCh5 = separation + separation + separation + separation + separation / 2;
                                centerCh6 = separation + separation + separation + separation + separation + separation / 2;
                                centerCh7 = separation + separation + separation + separation + separation + separation + separation / 2;


                            case 8:

                                centerCh1 = separation / 2;
                                centerCh2 = separation + separation / 2;
                                centerCh3 = separation + separation + separation / 2;
                                centerCh4 = separation + separation + separation + separation / 2;
                                centerCh5 = separation + separation + separation + separation + separation / 2;
                                centerCh6 = separation + separation + separation + separation + separation + separation / 2;
                                centerCh7 = separation + separation + separation + separation + separation + separation + separation / 2;
                                centerCh8 = separation + separation + separation + separation + separation + separation + separation + separation / 2;


                        }
                        save_preferences();
                        break;

                }
            }
        };
        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Centre of Plots (Ch1, Chx, Chn)"))
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .setNeutralButton("ReOrder", alertDialogClickListener)
                .create();


        alertDialog.show();
    }

    public void scalingMsgbox() {
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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final NumberPicker input = new NumberPicker(this);
        final NumberPicker input2 = new NumberPicker(this);
        final NumberPicker input3 = new NumberPicker(this);
        final NumberPicker input4 = new NumberPicker(this);
        final NumberPicker input5 = new NumberPicker(this);
        final NumberPicker input6 = new NumberPicker(this);
        final NumberPicker input7 = new NumberPicker(this);
        final NumberPicker input8 = new NumberPicker(this);

        if (activateChannels[0]) {
            input.setMaxValue(1000000);
            input.setMinValue(0);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setValue(scaCh1);
            input.setLayoutParams(params);
            linearLayout.addView(input);

        }

        if (activateChannels[1]) {
            input2.setMaxValue(1000000);
            input2.setMinValue(0);
            input2.setGravity(Gravity.CENTER_HORIZONTAL);
            input2.setValue(scaCh2);
            input2.setLayoutParams(params);
            linearLayout.addView(input2);
        }

        if (activateChannels[2]) {
            input3.setMaxValue(1000000);
            input3.setMinValue(0);
            input3.setGravity(Gravity.CENTER_HORIZONTAL);
            input3.setValue(scaCh3);
            input4.setLayoutParams(params);
            linearLayout.addView(input3);
        }

        if (activateChannels[3]) {
            input4.setMaxValue(1000000);
            input4.setMinValue(0);
            input4.setGravity(Gravity.CENTER_HORIZONTAL);
            input4.setValue(scaCh4);
            input4.setLayoutParams(params);
            linearLayout.addView(input4);
        }

        if (activateChannels[4]) {
            input5.setMaxValue(1000000);
            input5.setMinValue(0);
            input5.setGravity(Gravity.CENTER_HORIZONTAL);
            input5.setValue(scaCh5);
            input5.setLayoutParams(params);
            linearLayout.addView(input5);
        }

        if (activateChannels[5]) {
            input6.setMaxValue(1000000);
            input6.setMinValue(0);
            input6.setGravity(Gravity.CENTER_HORIZONTAL);
            input6.setValue(scaCh6);
            input6.setLayoutParams(params);
            linearLayout.addView(input6);
        }

        if (activateChannels[6]) {
            input7.setMaxValue(1000000);
            input7.setMinValue(0);
            input7.setGravity(Gravity.CENTER_HORIZONTAL);
            input7.setValue(scaCh7);
            input7.setLayoutParams(params);
            linearLayout.addView(input7);
        }

        if (activateChannels[7]) {
            input8.setMaxValue(1000000);
            input8.setMinValue(0);
            input8.setGravity(Gravity.CENTER_HORIZONTAL);
            input8.setValue(scaCh8);
            input8.setLayoutParams(params);
            linearLayout.addView(input8);
        }

        DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Done button clicked

                        if (activateChannels[0]) scaCh1 = input.getValue();
                        if (activateChannels[1]) scaCh2 = input2.getValue();
                        if (activateChannels[2]) scaCh3 = input3.getValue();
                        if (activateChannels[3]) scaCh4 = input4.getValue();
                        if (activateChannels[4]) scaCh5 = input5.getValue();
                        if (activateChannels[5]) scaCh6 = input6.getValue();
                        if (activateChannels[6]) scaCh7 = input7.getValue();
                        if (activateChannels[7]) scaCh8 = input8.getValue();
                        save_preferences();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked
                        break;

                }
            }
        };
        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Scaling (Ch1, Chx, Chn)"))
                .setView(linearLayout)
                .setPositiveButton("Done", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();


        alertDialog.show();
    }

    public void shw_wel_msg() {

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


    int left = 12;
    int top = 12;
    int right = 12;
    int bottom = 6;


    LinearLayout linearLayout = new LinearLayout(this);
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout.setGravity(Gravity.CENTER);
    linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

    TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
    params.setMargins(left,top,right,bottom);


    final NumberPicker input = new NumberPicker(this);

        input.setMaxValue(100000000);
        input.setMinValue(0);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setValue(scaling_psd);
        input.setLayoutParams(params);
        linearLayout.addView(input);





    DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Done button clicked

                    if (activateChannels[0]) scaling_psd = input.getValue();
                    save_preferences();

                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Cancel button clicked
                    break;

            }
        }
    };
    final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Scaling PSD"))
            .setView(linearLayout)
            .setPositiveButton("Done", alertDialogClickListener)
            .setNegativeButton("Cancel", alertDialogClickListener)
            .create();


    alertDialog.show();
}
    /*
    public void shw_wel_msg() {
        new AlertDialog.Builder(this)
                .setTitle("Initialize Experiment")
                .setMessage("Once you listen the beep close your eyes until the next beep." + "\n\r"
                        + "After this remain with the open eyes until the next period starts" + "\n\r"
                        + "This will be repeated 3 times (15s each open/close)")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Timer_stf();

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
*/
    public void Timer_stf () {
/*
        txtCounterDetails.setVisibility(View.VISIBLE);
        txtMTimer.setVisibility(View.VISIBLE);

        send_command_stream(START_CMD);


        new CountDownTimer(5000, 1000) {


            public void onTick(long millisUntilFinished) {
                txtMTimer.setText("" + millisUntilFinished / 1000);

            }

            public void onFinish() {
                txtCounterDetails.setText("Close - Remainding . . . ");
                beep();
                new CountDownTimer(testTime, 1000) {

                    public void onTick(long millisUntilFinished) {
                        txtMTimer.setText("" + millisUntilFinished / 1000);
                        if (millisUntilFinished <= testTime - shift_value) activate_close();
                    }

                    public void onFinish() {
                        beep();
                        txtCounterDetails.setText("Open - Remainding . . . ");
                        deactivate_all();
                        new CountDownTimer(testTime, 1000) {

                            public void onTick(long millisUntilFinished) {
                                txtMTimer.setText("" + millisUntilFinished / 1000);
                                if (millisUntilFinished <= testTime - shift_value) activate_open();
                            }

                            public void onFinish() {
                                beep();
                                txtCounterDetails.setText("Close - Remainding . . . ");
                                deactivate_all();
                                new CountDownTimer(testTime, 1000) {

                                    public void onTick(long millisUntilFinished) {
                                        txtMTimer.setText("" + millisUntilFinished / 1000);
                                        if (millisUntilFinished <= testTime - shift_value)
                                            activate_close();
                                    }

                                    public void onFinish() {
                                        beep();
                                        txtCounterDetails.setText("Open - Remainding . . . ");
                                        deactivate_all();
                                        new CountDownTimer(testTime, 1000) {

                                            public void onTick(long millisUntilFinished) {
                                                txtMTimer.setText("" + millisUntilFinished / 1000);
                                                if (millisUntilFinished <= testTime - shift_value)
                                                    activate_open();
                                            }

                                            public void onFinish() {
                                                beep();
                                                txtCounterDetails.setText("Close - Remainding . . . ");
                                                deactivate_all();
                                                new CountDownTimer(testTime, 1000) {

                                                    public void onTick(long millisUntilFinished) {
                                                        txtMTimer.setText("" + millisUntilFinished / 1000);
                                                        if (millisUntilFinished <= testTime - shift_value)
                                                            activate_close();
                                                    }

                                                    public void onFinish() {
                                                        beep();
                                                        txtCounterDetails.setText("Open - Remainding . . . ");
                                                        deactivate_all();
                                                        new CountDownTimer(testTime, 1000) {

                                                            public void onTick(long millisUntilFinished) {
                                                                txtMTimer.setText("" + millisUntilFinished / 1000);
                                                                if (millisUntilFinished <= testTime - shift_value)
                                                                    activate_open();
                                                            }

                                                            public void onFinish() {

                                                                deactivate_all();
                                                                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                                                toneGen1.startTone(ToneGenerator.TONE_CDMA_INTERCEPT, 150);
                                                                txtCounterDetails.setVisibility(View.INVISIBLE);
                                                                txtMTimer.setVisibility(View.INVISIBLE);
                                                                shw_results();
                                                                send_command_stream(STOP_CMD);
                                                            }
                                                        }.start();
                                                    }
                                                }.start();
                                            }
                                        }.start();
                                    }
                                }.start();
                            }

                        }.start();
                    }
                }.start();
            }
        }.start();
        */
    }

    public void  beep() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_L,150);
    }

    public void  beep_alarm() {

        if(sound_enable) {
            for (int i = 0; i < 5; i++) {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 150);
                ToneGenerator toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 0);
                toneGen2.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 150);
            }
        }
    }

    public void saveFile() throws IOException {

        //osw.write("end");
        osw.close();
        //Toast.makeText(this, "Saved at EEG_adq folder (internal mem)", Toast.LENGTH_SHORT ).show();
        write_open = false;


    }

    public void askOpenFile() {
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


        int left = 12;
        int top = 12;
        int right = 12;
        int bottom = 6;


        TableRow.LayoutParams params = new TableRow.LayoutParams(FloatingActionButton.LayoutParams.WRAP_CONTENT, FloatingActionButton.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);


        final TextView text = new TextView(this);
        text.setText("\n Saved at EEG_adq folder (internal mem)" +
                "\n Do you want to open the file now? \n");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        linearLayout.addView(text);


        DialogInterface.OnClickListener alertDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Done button clicked
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        Uri uri = Uri.parse("file:///sdcard/EEG_adq/" + "SF " + nameFile.toString() + ".txt");

                        intent.setDataAndType(uri, "text/plain");
                        try {
                            startActivity(intent);
                        } catch (Exception io) {
                            Toast.makeText(RealtimeLineChartActivity.this, "No Editor APP detected!", Toast.LENGTH_SHORT).show();
                        }


                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked
                        break;
                }
            }
        };


        final AlertDialog alertDialog = (new AlertDialog.Builder(this)).setCustomTitle(format_textview("Saved file"))
                .setView(linearLayout)
                .setPositiveButton("YES", alertDialogClickListener)
                .setNegativeButton("Cancel", alertDialogClickListener)
                .create();


        alertDialog.show();
    }

    public void onClickCheckToSave() {

        File folder = new File(Environment.getExternalStorageDirectory() + "/EEG_adq");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
        } else {
        }
        SimpleDateFormat s = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
        nameFile = s.format(new Date());

        File SdCard = Environment.getExternalStorageDirectory();
        String Convert = SdCard.getAbsolutePath().toString() + "/EEG_adq";

        final File file = new File(Convert, "SF " + nameFile.toString() + ".txt");


        try {
            osw = new OutputStreamWriter(new FileOutputStream(file));
            osw.write("ProgrNum,PacketType,Ch1,Ch2,Ch3,Ch4,Ch5,Ch6,Ch7,Ch8, AccX, AccY, AccZ \n");
        } catch (FileNotFoundException e) {
            success = false;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if (success){ CreateMarker.setVisibility(View.VISIBLE); btnSave.setVisibility(View.VISIBLE); write_open=true; CheckSave.setEnabled(false); btnSave.setEnabled(true);}
        //Toast.makeText(this, "value", Toast.LENGTH_SHORT).show();

    }

    void bt_init(){


        //SingleFeedRunThread();

        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString("address");
            bitsExpected = Integer.parseInt(extras.getString("bits"));
            //Toast.makeText(this, "" + bitsExpected , Toast.LENGTH_SHORT).show();

            if (Connect()) {

                //plot_results_thread();
            } else finish();


        } else {
            Toast.makeText(this, "Invalid BT", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    boolean Connect() {

        boolean toreturn = false;

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

    /*try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }*/

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            mmOutputStream = btSocket.getOutputStream();
            readThread = new BluetoothReadThread();

            readThread.checkAccess();
            readThread.setPriority(Thread.MAX_PRIORITY);

            readThread.start();
            toreturn = true;
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        //mConnectedThread = new ConnectedThread(btSocket);
        //mConnectedThread.start();

        return toreturn;


    }

    class BluetoothReadThread extends Thread {


        private final InputStream iStream;
        private final OutputStream mmOutputStream;

        private boolean continueReading = true;

        public BluetoothReadThread() {
            InputStream tmp = null;
            OutputStream tmp2 = null;

            try {
                tmp = btSocket.getInputStream();
                tmp2 = btSocket.getOutputStream();

            } catch (IOException e) {
            }
            iStream = tmp;
            mmOutputStream = tmp2;


        }

        @Override
        public void run() {

            int c;
            int waitCount = 0;
            while (continueReading) {
                try {
                    // Read integer values from Bluetooth stream.
                    // Assemble them manually into doubles, split on newline (\n) character


                    if (iStream.available() > 0) {
                        waitCount = 0;
                        c = iStream.read();
                        readBuffer[readBufferPosition++] = c;


                        if (readBufferPosition == bitsExpected) {

                            process_data();

                        }


                    } else { // No input stream available, wait
                        if (waitCount >= 500000) {
                            //if desconnected try to reconnect sending the start command.
                            if(stream_enable)
                            {
                                String startTring = "=";
                                try {
                                    mmOutputStream.write(startTring.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Disconnected while streaming enable, Trying to reconnect . . .");


                            }
                            // No data ready in 500000 loop cycles, ECG has probably been disconnected. Close self.
                            waitCount = 0;
                            System.out.println("----wait count expired " + iStream.available());
                            //continueReading = false;
                            //this.stopAndSendIntent();
                        } else {
                            waitCount++;
                        }
                    }

                } catch (IOException e) {
                    System.out.println(e + "\nError sending data + :" + e);
                    // Bluetooth error! Stop reading.
                    //this.stopAndSendIntent();
                }
            }
        }

        /*
                public void stopAndSendIntent() {

                    this.cancel();

                    Intent intent = new Intent();
                    intent.setAction(BLUETOOTH_ACTION_DONE_READING);
                    sendBroadcast(intent);
                }
        */
        public void cancel() {
            System.out.println("-----Cancelling readThread!!");
            try {
                iStream.close();
            } catch (IOException e) {
            } catch (NullPointerException e) {
            }
            ;

            continueReading = false;
        }

    }

    FFT FFT_rt = new FFT(N); // create psd instance;


    double[] re = new double[N];
    double[] im = new double[N];


    public void process_data() throws IOException {
        if (bitsExpected == 22) {
            ch1 = MultiplicationCombine(readBuffer[4], readBuffer[3]);
            ch2 = MultiplicationCombine(readBuffer[6], readBuffer[5]);




        } else {


            ch1 = MultiplicationCombine(readBuffer[5],  readBuffer[4],  readBuffer[3]);
            ch2 = MultiplicationCombine(readBuffer[8],  readBuffer[7],  readBuffer[6]);
            ch3 = MultiplicationCombine(readBuffer[11], readBuffer[10], readBuffer[9]);
            ch4 = MultiplicationCombine(readBuffer[14], readBuffer[13], readBuffer[12]);
            ch5 = MultiplicationCombine(readBuffer[17], readBuffer[16], readBuffer[15]);
            ch6 = MultiplicationCombine(readBuffer[20], readBuffer[19], readBuffer[18]);
            ch7 = MultiplicationCombine(readBuffer[23], readBuffer[22], readBuffer[21]);
            ch8 = MultiplicationCombine(readBuffer[26], readBuffer[25], readBuffer[24]);

        }

        Header_int = readBuffer[0];
        PK_ID_int = readBuffer[1];
        PK_Counter_int = readBuffer[2];

        if (PK_Counter_int % 100 == 0) mmOutputStream.write(startTring.getBytes());

        downsample++;

            if (psd_time_bol)
            {

                double[] re_t = new double[N];
                double[] im_t = new double[N];
                double [] tempCircular;


                switch (ChannelTOComputePSD) {
                    case 1:

                                tempCircular = circular_buffer;
                                for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                                circular_buffer[N-1]  =  ch1;
                                num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for(int i = 0 ; i<N ; i++)
                            {
                                re_t[i] = circular_buffer[i];
                            }

                            for(int i = 0 ; i<N ; i++)
                            {
                                im_t [i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0 ;


                        }


                        break;
                    case 2:
                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch2;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }

                        break;
                    case 3:

                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch3;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }

                        break;
                    case 4:

                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch4;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }

                        break;
                    case 5:
                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch5;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }
                        break;
                    case 6:
                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch6;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }
                        break;
                    case 7:
                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch7;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }
                        break;
                    case 8:
                        tempCircular = circular_buffer;
                        for (int i = 1 ; i<N ; i++)circular_buffer[i-1] = tempCircular[i];
                        circular_buffer[N-1]  =  ch8;
                        num_rec_samples++;

                        if(num_rec_samples > 31 ) {

                            /*for(int i = 0 ; i<N ; i++)
                            {
                                im[i] = 0;
                            }
                            */

                            for (int i = 0; i < N; i++) {
                                re_t[i] = circular_buffer[i];
                            }

                            for (int i = 0; i < N; i++) {
                                im_t[i] = im[i];
                            }


                            FFT_rt.fft(re_t, im_t);
                            fft_mag = fftCalculator(re_t, im_t);
                            plot_psd();
                            num_rec_samples = 0;
                        }
                        break;
                    default:
                        break;
                }

            }

            else if (downsample == downsample_value) {
                    addEntry(ch1 / scaCh1, ch2 / scaCh2, ch3 / scaCh3, ch4 / scaCh4, ch5 / scaCh5, ch6 / scaCh6, ch7 / scaCh7, ch8 / scaCh8);
                    downsample = 0;


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtCOutput.setText( ""+ PK_ID_int);
                    }
                });


                        }



        //ProgrNum,PacketType,Ch1,Ch2,Ch3,Ch4,Ch5,Ch6,Ch7,Ch8,MRK

        if (write_open) {
            osw.write(PK_Counter_int + "," + PK_ID_int + "," + ch1 + "," + ch2 + "," + ch3 + "," + ch4 + "," + ch5 + "," + ch6 + "," + ch7 + "," + ch8  + "\n");
        }

        System.out.println(PK_Counter_int + "," + PK_ID_int + "," + ch1 + "," + ch2 + "," + ch3 + "," + ch4 + "," + ch5 + "," + ch6 + "," + ch7 + "," + ch8);

        readBufferPosition = 0;

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        //return device.createRfcommSocketToServiceRecord(MY_UUID);
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    }

    int MultiplicationCombine(int x_high, int x_low) {
        int combined;
        combined = x_high;
        combined = combined * 256;
        combined |= x_low;
        //combined = ~ combined;
        //combined = combined + 1;
        //combined = two2dec(combined);

        return combined;

    }

    int MultiplicationCombine(int x_high, int x_medium, int x_low) {
        // int VL = 2400/2^24;
        int combined;
        combined = x_high;
        combined = combined * 256 * 256 * 256;
        combined |= x_medium * 256 * 256;
        combined |= x_low * 256;
        //combined = ~ combined;
        //combined = combined + 1;
        //combined = two2dec(combined)/256;
        combined = combined / 256; //* VL;

        return combined;
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try {

                            String startTring = ":";
                            mmOutputStream.write(startTring.getBytes());
                            readThread.cancel();
                            btSocket.close();
                            //stopWorker2 = true;


                        } catch (IOException ex) {
                        }

                        //readThread.stop();
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disconnect");
        builder.setMessage("Are you sure?, BT will be disconnected...");
        builder.setIcon(R.drawable.alert_triangle_red_128);
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);
        builder.show();

        return;
    }

    void ini_psd_graphic() {

        mChart_psd = (LineChart) findViewById(R.id.chart2);
        mChart_psd.setOnChartGestureListener(this);
        mChart_psd.setOnChartValueSelectedListener(this);
        mChart_psd.setDrawGridBackground(false);

        // no description text
        mChart_psd.setDescription("");
        mChart_psd.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart_psd.setTouchEnabled(true);

        // enable scaling and dragging
        mChart_psd.setDragEnabled(true);
        mChart_psd.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart_psd.setPinchZoom(true);

        // set an alternative background color
        mChart_psd.setBackgroundColor(Color.WHITE);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // set the marker to the chart
        mChart_psd.setMarkerView(mv);

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart_psd.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
/*
        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(tf);
*/

        YAxis leftAxis = mChart_psd.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis.addLimitLine(ll1);
        //leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(500f);
        leftAxis.setAxisMinValue(-10f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart_psd.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setData(45, 100);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart_psd.animateY(100);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart_psd.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();


        configure_graphic();





    }

    private void setData(int count, double[] rec_values) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float val = (float) rec_values[i]/scaling_psd;

            values.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (mChart_psd.getData() != null &&
                mChart_psd.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart_psd.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart_psd.getData().notifyDataChanged();
            mChart_psd.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setDrawCircles(false);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart_psd.setData(data);
        }
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * range) + 3;
            values.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (mChart_psd.getData() != null &&
                mChart_psd.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart_psd.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart_psd.getData().notifyDataChanged();
            mChart_psd.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart_psd.setData(data);
        }
    }

    public static double[] fftCalculator(double[] re, double[] im) {

        double[] fftMag = new double[re.length];
        for (int i = 0; i < re.length; i++) {
            fftMag[i] = (Math.pow(re[i], 2) + Math.pow(im[i], 2))/100000;
        }
        return fftMag;
    }

    void configure_graphic() {


        List<ILineDataSet> sets = mChart_psd.getData()
                .getDataSets();

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            if (set.isDrawCirclesEnabled())
                set.setDrawCircles(false);
            else
                set.setDrawCircles(true);
        }
        mChart_psd.invalidate();

        sets = mChart_psd.getData()
                .getDataSets();

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                    ? LineDataSet.Mode.LINEAR
                    :  LineDataSet.Mode.CUBIC_BEZIER);
        }
        mChart_psd.invalidate();

        sets = mChart_psd.getData()
                .getDataSets();

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            set.setDrawValues(!set.isDrawValuesEnabled());
        }

        mChart_psd.invalidate();




    }

    void plot_psd() {



        wait = true;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (wait) {

                    setData(N/2, fft_mag);
                    //setData(20,90);
                    mChart_psd.invalidate();
                    downsample = 0;
                    wait = false;

                }
            }
        });
    }



    private Thread thread2;

    private void run_write_data(final int sample_rate)  {



        if (thread2 != null)
            thread2.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                //addEntry();
            }
        };

        thread2 = new Thread(new Runnable()  {


            boolean keep_transmitting_local = keep_transmitting;
            int downsample_transmit;


            @Override
            public void run() {
                int delay_ms = 5;

                while(keep_transmitting_local)
                {


                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    downsample_transmit++;
                    if (downsample_transmit == downsample_value) {
                        addEntry();
                        downsample_transmit = 0;
                    }


                    if (write_open) {
                        try {
                            osw.write(PK_Counter_int + "," + PK_ID_int + "," + ch1 + "," + ch2 + "," + ch3 + "," + ch4 + "," + ch5 + "," + ch6 + "," + ch7 + "," + ch8  + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("delay = " + (int)((1/sample_rate)*1000) + "," + PK_ID_int + "," + ch1 + "," + ch2 + "," + ch3 + "," + ch4 + "," + ch5 + "," + ch6 + "," + ch7 + "," + ch8);





                    try {
                        Thread.sleep((int)delay_ms);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                   keep_transmitting_local = keep_transmitting;

            }

            }
        });

        thread2.start();
    }


}

