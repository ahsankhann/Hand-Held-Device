package com.ahsanshamim.novatexfgs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsanshamim.novatexfgs.Model.DefectsInfo;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HoldScanner_Activity extends AppCompatActivity {
    private EditText mResultText, BarcodeResult, txtHold, edtdefect;
    private String mStatusText;
    private String mSuccessFailText;
    private ProgressDialog mProgressDialog;
    String Url = "http://192.168.96.148:8080/api/HoldCorton";
    private String mCurrentStatus;
    private String mSavedStatus;
    private boolean mIsRegisterReceiver;
    private String BarcodeSetting;
    AlertDialog alertDialog;
    String username = "";

    private static final String STATUS_CLOSE = "STATUS_CLOSE";
    private static final String STATUS_OPEN = "STATUS_OPEN";
    private static final String STATUS_TRIGGER_ON = "STATUS_TRIGGER_ON";

    private static final int SEQ_BARCODE_OPEN = 100;
    private static final int SEQ_BARCODE_CLOSE = 200;
    private static final int SEQ_BARCODE_GET_STATUS = 300;
    private static final int SEQ_BARCODE_SET_TRIGGER_ON = 400;
    private static final int SEQ_BARCODE_SET_TRIGGER_OFF = 500;
    private static final int SEQ_BARCODE_SET_PARAMETER = 600;
    private static final int SEQ_BARCODE_GET_PARAMETER = 700;
    private final static String SCAN_ACTION = "scan.rcv.message";
    private static final String LOG_TAG = "LaserScannerPlugin";

    TextView txtStatus, txtTimeView, txtCutton;
    TextView Grammagelbl, ColorShadelbl,ProductCodelbl, NeckTypelbl, BatchNolbl, TareWeightlbl, GrossWeightlbl, NetWeightlbl, Pieceslbl, Mobilelbl, Holdlbl;
    TextView Grammagelbl1, ColorShadelbl1,ProductCodelbl1, NeckTypelbl1, BatchNolbl1, TareWeightlbl1, GrossWeightlbl1, NetWeightlbl1, Pieceslbl1, Mobilelbl1, Holdlbl1;
    private int mSelectedSetParam = -1;
    private int mSelectedGetParam = -1;
    TableLayout tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_scanner_);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
        tb.setVisibility(View.GONE);
        txtCutton.setVisibility(View.GONE);
        GetDeviceData();
        txtHold.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    SelectType();
                }
                else{
                    if(txtHold.getText().toString().equals(""))
                        txtHold.hasFocus();
                }
            }
        });

        txtHold.setInputType(InputType.TYPE_NULL);

        txtHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectType();
                    if(txtHold.getText().toString().equals(""))
                        txtHold.hasFocus();
            }
        });

        edtdefect.setInputType(InputType.TYPE_NULL);

        edtdefect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProgressDialogDefects(true);
                GetDataDefect();
            }
        });

        edtdefect.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    ProgressDialogDefects(true);

                    GetDataDefect();
                }
                else {
                    if (edtdefect.getText().toString().equals(""))
                        edtdefect.hasFocus();
                }
            }
        });

    }

    private void SetBarcode(){
        GetDeviceData();
        if(BarcodeSetting.length()> 1){
            if(BarcodeSetting.equals("BlueBird Device")){
                registerReceiver();
                resetCurrentView();
                OpenBarcode();
            }else if(BarcodeSetting.equals("China Device")){
                showProgressDialog(true);
                register();
            }
        }
    }

    private void register(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_PARAMETER);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_GET_STATUS);
        filter.addAction(SCAN_ACTION);
        filter.addAction(LOG_TAG);


        registerReceiver(ChinabroadcastReceiver, filter);
        setResultText("BARCODE_OPEN");
        showProgressDialog(false);
    }

    private void SelectType(){
        final String[] Values = {"Hold", "PVS", "Manual Sorting"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Hold Or Realised");
        builder.setSingleChoiceItems(Values, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtHold.setText(Values[which]);
                if((txtHold.getText().toString().equals("Hold") || txtHold.getText().toString().equals("PVS")|| txtHold.getText().toString().equals("Manual Sorting")) && !edtdefect.getText().toString().equals("")) {
                    SetBarcode();
                }
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    BroadcastReceiver ChinabroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //ast.makeText(getApplicationContext(), "Scanner", Toast.LENGTH_SHORT).show();
            Log.v("Amar", "received info");
            byte[] barocode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            byte temp = intent.getByteExtra("barcodeType", (byte) 0);
            Log.v("Amar", "----codetype--" + temp);
            String barcodeStr = new String(barocode, 0, barocodelen);
            //       showScanResult.setText(barcodeStr);
            Log.v("Amar", barcodeStr);
            setResulsText(barcodeStr);
        }
    };

    @Override
    protected void onResume() {
        if(txtHold.getText().toString().equals("Hold") || txtHold.getText().toString().equals("PVS")|| txtHold.getText().toString().equals("Manual Sorting")) {
            SetBarcode();
        }
        super.onResume();
    }

    private void OpenBarcode(){
        Intent intent = new Intent();
        mCount = 0;
        String action = Constants.ACTION_BARCODE_CLOSE;
        int seq = SEQ_BARCODE_CLOSE;
        if(mCurrentStatus.equals(STATUS_CLOSE)) action = Constants.ACTION_BARCODE_OPEN;
        intent.setAction(action);
        if(mIsOpened) intent.putExtra(Constants.EXTRA_HANDLE, mBarcodeHandle);
        if(mCurrentStatus.equals(STATUS_CLOSE)) seq = SEQ_BARCODE_OPEN;
        intent.putExtra(Constants.EXTRA_INT_DATA3, seq);
        sendBroadcast(intent);
        if(mCurrentStatus.equals(STATUS_CLOSE))
        {
            mIsOpened = true;
            setResultText("BARCODE_OPEN");
            showProgressDialog(true);
        }
        else
        {
            mIsOpened = false;
            setResultText("BARCODE_CLOSE");
        }
    }

    private void resetCurrentView()
    {
        if(!mSavedStatus.equals(mCurrentStatus))
        {
            if(!mSavedStatus.equals(STATUS_CLOSE))
            {
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_BARCODE_OPEN);
                intent.putExtra(Constants.EXTRA_HANDLE, mBarcodeHandle);
                intent.putExtra(Constants.EXTRA_INT_DATA3, SEQ_BARCODE_OPEN);
                sendBroadcast(intent);
                showProgressDialog(true);
                return;
            }
        }
        refreshCurrentStatus();
    }

    @Override
    protected void onPause() {
        if(txtHold.getText().toString().equals("Hold") || txtHold.getText().toString().equals("Realised")) {
        if(BarcodeSetting.equals("BlueBird Device")){
            CloseBarcode();
        }else if(BarcodeSetting.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
        }
        }
        super.onPause();
    }

    private void CloseBarcode(){
        mSavedStatus = mCurrentStatus;
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BARCODE_CLOSE);
        intent.putExtra(Constants.EXTRA_HANDLE, mBarcodeHandle);
        intent.putExtra(Constants.EXTRA_INT_DATA3, SEQ_BARCODE_CLOSE);
        sendBroadcast(intent);
        unregisterReceiver();
        mCurrentStatus = STATUS_CLOSE;
    }

    private void initialize()
    {
        setTitle("Hold Carton Scanning");

        mSavedStatus = mCurrentStatus = STATUS_CLOSE;
        initLayout();
        mIsRegisterReceiver = false;

    }

    private String getVersion() {
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    private boolean mIsOpened = false;



    private Timer mTimer;

    private void showProgressDialog(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Notice");
            mProgressDialog.setMessage("Barcode Initializing...");
            mProgressDialog.setCancelable(false);
        }

        stopProgressTimer();
        if(isShow)
        {
            mProgressDialog.show();
            startProgressTimer();
        }
        else mProgressDialog.dismiss();
    }

    private void startProgressTimer()
    {
        mTimer = new Timer();
        mTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if(mProgressDialog!=null && mProgressDialog.isShowing())
                        {
                            mProgressDialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setSuccessFailText("Failed result : "+"Try open timeout");
                                }
                            });
                        }

                    }
                }, 5000);
    }

    private void stopProgressTimer()
    {
        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = null;
    }

    private void registerReceiver()
    {
        if(mIsRegisterReceiver) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_PARAMETER);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_GET_STATUS);

        registerReceiver(mReceiver, filter);
        mIsRegisterReceiver = true;
    }

    private void unregisterReceiver()
    {
        if(!mIsRegisterReceiver) return;
        unregisterReceiver(mReceiver);
        mIsRegisterReceiver = false;
    }

    private int mBarcodeHandle = -1;
    private int mCount = 0;
    private String[] STATUS_ARR = {STATUS_CLOSE, STATUS_OPEN, STATUS_TRIGGER_ON};

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int handle = intent.getIntExtra(Constants.EXTRA_HANDLE, 0);
            int seq = intent.getIntExtra(Constants.EXTRA_INT_DATA3, 0);

            if(action.equals(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA))
            {
                tb.setVisibility(View.GONE);
                mCount++;
                byte[] data = intent.getByteArrayExtra(Constants.EXTRA_BARCODE_DECODING_DATA);
                int symbology = intent.getIntExtra(Constants.EXTRA_INT_DATA2, -1);
                String result = "[BarcodeDecodingData handle : "+handle+" / count : "+mCount+" / seq : "+seq+"]\n";
                result += ("[Symbology] : " + symbology + "\n");
                String dataResult = "";
                if(data!=null)
                {
                    dataResult = new String(data);
                    if(dataResult.contains("�"))
                    {
                        try {
                            dataResult = new String(data, "Shift-JIS");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                result += "[Data] : "+dataResult;
                setResulsText(dataResult);
            }
            else if(action.equals(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS))
            {
                setSuccessFailText("Success : "+seq);
                if(seq == SEQ_BARCODE_OPEN)
                {
                    mBarcodeHandle = intent.getIntExtra(Constants.EXTRA_HANDLE, 0);
                    mCurrentStatus = STATUS_OPEN;
                    showProgressDialog(false);
                }
                else if(seq == SEQ_BARCODE_CLOSE)
                {
                    mCurrentStatus = STATUS_CLOSE;
                    showProgressDialog(false);
                }
                else if(seq == SEQ_BARCODE_GET_STATUS)
                {
                    mCurrentStatus = STATUS_CLOSE;
                    showProgressDialog(false);
                }
                else if(seq == SEQ_BARCODE_SET_TRIGGER_ON) mCurrentStatus = STATUS_TRIGGER_ON;
                else if(seq == SEQ_BARCODE_SET_TRIGGER_OFF) mCurrentStatus = STATUS_OPEN;
                else if(seq == SEQ_BARCODE_SET_PARAMETER)
                {
                    setResultText("SET_PARAMETER success");
                }
                else
                {
                    showProgressDialog(false);
                }

                refreshCurrentStatus();
//                refreshButton();
            }
            else if(action.equals(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED))
            {
                int result = intent.getIntExtra(Constants.EXTRA_INT_DATA2, 0);
                showProgressDialog(false);
                if(result == Constants.ERROR_BARCODE_DECODING_TIMEOUT)
                {
                    setSuccessFailText("Failed result : "+"Decode Timeout"+" / seq : "+seq);
                }
                else if(result == Constants.ERROR_NOT_SUPPORTED)
                {
                    setSuccessFailText("Failed result : "+"Not Supoorted"+" / seq : "+seq);
                }
                else if(result == Constants.ERROR_BARCODE_ERROR_USE_TIMEOUT)
                {
                    mCurrentStatus = STATUS_CLOSE;
                    setSuccessFailText("Failed result : "+"Use Timeout"+" / seq : "+seq);
                }
                else if(result == Constants.ERROR_BARCODE_ERROR_ALREADY_OPENED)
                {
                    mCurrentStatus = STATUS_OPEN;
                    setSuccessFailText("Failed result : "+"Already opened"+" / seq : "+seq);
                }
                else if(result == Constants.ERROR_BATTERY_LOW)
                {
                    mCurrentStatus = STATUS_CLOSE;
                    setSuccessFailText("Failed result : "+"Battery low"+" / seq : "+seq);
                }
                else if(result == Constants.ERROR_NO_RESPONSE)
                {
                    int notiCode = intent.getIntExtra(Constants.EXTRA_INT_DATA3, 0);
                    setSuccessFailText("Failed result : "+ notiCode+"/ ### ERROR_NO_RESPONSE ###");
                    mCurrentStatus = STATUS_CLOSE;
                    setSuccessFailText("Failed result : "+result+" / seq : "+seq);
                }
                else
                {
                    setSuccessFailText("Failed result : "+result+" / seq : "+seq);
                }
                if(seq == SEQ_BARCODE_SET_PARAMETER)
                {
                    if(result == Constants.ERROR_BARCODE_EXCEED_ASCII_CODE) setResultText("SET_PARAMETER failed:exceed range of ascii code");
                }
                refreshCurrentStatus();
//                refreshButton();
            }
            else if(action.equals(Constants.ACTION_BARCODE_CALLBACK_PARAMETER))
            {
                int parameter = intent.getIntExtra(Constants.EXTRA_INT_DATA2, -1);
                String value = intent.getStringExtra(Constants.EXTRA_STR_DATA1);

                setResultText("Get parameter result\nparameter : "+parameter+" / value : "+value);
            }
            else if(action.equals(Constants.ACTION_BARCODE_CALLBACK_GET_STATUS))
            {
                int status = intent.getIntExtra(Constants.EXTRA_INT_DATA2, 0);
                mCurrentStatus = STATUS_ARR[status];
                setResultText("Current Status : "+mCurrentStatus+" / id : "+status);
                refreshCurrentStatus();
            }
        }
    };



    private void initLayout() {
        mResultText = (EditText) findViewById(R.id.BarcodeStatus);
        BarcodeResult = (EditText) findViewById(R.id.resultTxt);
        edtdefect = (EditText) findViewById(R.id.defectedt);
        txtStatus = (TextView) findViewById(R.id.Statustxt);
        txtTimeView = (TextView) findViewById(R.id.DatTimmetxt);
        txtCutton = (TextView) findViewById(R.id.Cuttontxt);
        txtHold = (EditText) findViewById(R.id.holdtxt);

        tb = (TableLayout) findViewById(R.id.tblLayout);
        Grammagelbl = (TextView) findViewById(R.id.Grammagelbl);
        ColorShadelbl = (TextView) findViewById(R.id.ColorShadelbl);
        ProductCodelbl = (TextView) findViewById(R.id.ProductCodelbl);

        NeckTypelbl = (TextView) findViewById(R.id.NeckTypelbl);
        BatchNolbl = (TextView) findViewById(R.id.BatchNolbl);
        TareWeightlbl = (TextView) findViewById(R.id.TareWeightlbl);
        GrossWeightlbl = (TextView) findViewById(R.id.GrossWeightlbl);
        NetWeightlbl = (TextView) findViewById(R.id.NetWeightlbl);
        Pieceslbl = (TextView) findViewById(R.id.Pieceslbl);
        Mobilelbl = (TextView) findViewById(R.id.Mobilelbl);
        Holdlbl = (TextView) findViewById(R.id.Holdlbl);


        Grammagelbl1 = (TextView) findViewById(R.id.Grammagelbl1);
        ColorShadelbl1 = (TextView) findViewById(R.id.ColorShadelbl1);
        ProductCodelbl1 = (TextView) findViewById(R.id.ProductCodelbl1);

        NeckTypelbl1 = (TextView) findViewById(R.id.NeckTypelbl1);
        BatchNolbl1 = (TextView) findViewById(R.id.BatchNolbl1);
        TareWeightlbl1 = (TextView) findViewById(R.id.TareWeightlbl1);
        GrossWeightlbl1 = (TextView) findViewById(R.id.GrossWeightlbl1);
        NetWeightlbl1 = (TextView) findViewById(R.id.NetWeightlbl1);
        Pieceslbl1 = (TextView) findViewById(R.id.Pieceslbl1);
        Mobilelbl1 = (TextView) findViewById(R.id.Mobilelbl1);
        Holdlbl1 = (TextView) findViewById(R.id.Holdlbl1);
    }

    private void setResultText(String Json)
    {

        mResultText.setText(Json);
    }

    private void setResulsText(String Json){
        if(isNetworkConnected() == false){
            Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
            return;
        }

        if(BarcodeSetting.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
        }
        else if(BarcodeSetting.equals("BlueBird Device")){
            CloseBarcode();
        }

        if(username.equals("")){
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.getDataUser();
            if(cursor.moveToLast()){
                username = cursor.getString(0);
            }

        }

        if((txtHold.getText().toString().equals("Hold")|| txtHold.getText().toString().equals("PVS")|| txtHold.getText().toString().equals("Manual Sorting")) && !edtdefect.getText().toString().equals("")){
        try{
            if(Json != null && Json != "") {
                //BagInfo JsonBag = new Gson().fromJson(String.valueOf(Json), BagInfo.class);

                final String[] Split = Json.split(",");
                String[] SplitDefects = edtdefect.getText().toString().split("-");

                final JSONObject json = new JSONObject();
                if (Split.length == 11) {
                    try {
                        json.put("lbl_Package_No",  Split[0]);
                        json.put("Grammage",  Split[1]);
                        json.put("Color",  Split[2]);
                        json.put("Shade",  Split[3]);
                        json.put("Productcode",  Split[4]);
                        json.put("Grade",  Split[5]);
                        json.put("Nacktype",  Split[6]);
                        json.put("Batchno",  Split[7]);
                        json.put("pieces",  Split[8]);
                        json.put("Netweight",  Split[9]);
                        json.put("Grossweight",  Split[10]);
                        json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);
                        if(txtHold.getText().toString().equals("Hold"))
                            json.put("hold", "H");
                        else if(txtHold.getText().toString().equals("PVS"))
                            json.put("hold", "P");
                        else if(txtHold.getText().toString().equals("Manual Sorting"))
                            json.put("hold", "M");
                        json.put("username", username);
                        json.put("def_cd", SplitDefects[0]);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(Split.length == 12){
                    try {
                        json.put("lbl_Package_No",  Split[0]);
                        json.put("Grammage",  Split[1]);
                        json.put("Color",  Split[2]);
                        json.put("Shade",  Split[3]);
                        json.put("Productcode",  Split[4]);
                        json.put("Grade",  Split[5]);
                        json.put("Nacktype",  Split[6]);
                        json.put("Batchno",  Split[7]);
                        json.put("pieces",  Split[8]);
                        json.put("Netweight",  Split[9]);
                        json.put("Grossweight",  Split[10]);
                        json.put("Stem_cd", Split[11]);
                        json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);
                        if(txtHold.getText().toString().equals("Hold"))
                            json.put("hold", "H");
                        else if(txtHold.getText().toString().equals("PVS"))
                            json.put("hold", "P");
                        else if(txtHold.getText().toString().equals("Manual Sorting"))
                            json.put("hold", "M");
                        json.put("username", username);
                        json.put("def_cd", SplitDefects[0]);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    ErrorInResponse("Connecting Error", "Invalid QR Code");
                }

                Bundle bundle = new Bundle();
                bundle.putString("json", json.toString());
                BarcodeResult.setText(Split[0].toString());
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        Url,
                        json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                ShowDataHold(response.toString());
                                tb.setVisibility(View.VISIBLE);
                                txtCutton.setVisibility(View.VISIBLE);
                                if(BarcodeSetting.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeSetting.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                showToast("Server Error Please Contact IS Department!");
                                txtStatus.setText("Server Error Please Contact IS Department");
                                txtTimeView.setText("");
                                txtStatus.setTextColor(Color.RED);
                                BarcodeResult.setText("");
                                tb.setVisibility(View.GONE);
                                txtCutton.setVisibility(View.GONE);
                                if(BarcodeSetting.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeSetting.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}
                            }
                        }
                );
                requestQueue.add(request);
            }}catch (Exception ex){
            Toast.makeText(getApplicationContext(), "QR Code Invalid", Toast.LENGTH_LONG).show();
            txtStatus.setText("QR Code Invalid");
            txtStatus.setTextColor(Color.RED);
            txtTimeView.setText("");
            tb.setVisibility(View.GONE);
            txtCutton.setVisibility(View.GONE);
            BarcodeResult.setText("");
            if(BarcodeSetting.equals("China Device")){
                register();
            }
            else if(BarcodeSetting.equals("BlueBird Device")){
                OpenBarcode();
                registerReceiver();
                resetCurrentView();}
        }}
        else if(txtHold.getText().toString().equals("Realised")){
            try{
                if(Json != null && Json != "") {
                    //BagInfo JsonBag = new Gson().fromJson(String.valueOf(Json), BagInfo.class);

                    final String[] Split = Json.split(",");



                    final JSONObject json = new JSONObject();
                    if (Split.length == 11) {
                        try {
                            json.put("lbl_Package_No", Split[0]);
                            json.put("Grammage", Split[1]);
                            json.put("Color", Split[2]);
                            json.put("Shade", Split[3]);
                            json.put("Productcode", Split[4]);
                            json.put("Grade", Split[5]);
                            json.put("Nacktype", Split[6]);
                            json.put("Batchno", Split[7]);
                            json.put("pieces", Split[8]);
                            json.put("Netweight", Split[9]);
                            json.put("Grossweight", Split[10]);
                            json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);
                            json.put("username", username);
                            if (txtHold.getText().toString().equals("Hold"))
                                json.put("hold", "H");
                            else if (txtHold.getText().toString().equals("PVS"))
                                json.put("hold", "P");
                            else if (txtHold.getText().toString().equals("Manual Sorting"))
                                json.put("hold", "M");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(Split.length == 12){
                        try {
                            json.put("lbl_Package_No", Split[0]);
                            json.put("Grammage", Split[1]);
                            json.put("Color", Split[2]);
                            json.put("Shade", Split[3]);
                            json.put("Productcode", Split[4]);
                            json.put("Grade", Split[5]);
                            json.put("Nacktype", Split[6]);
                            json.put("Batchno", Split[7]);
                            json.put("pieces", Split[8]);
                            json.put("Netweight", Split[9]);
                            json.put("Grossweight", Split[10]);
                            json.put("Stem_cd", Split[11]);
                            json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);
                            json.put("username", username);
                            if (txtHold.getText().toString().equals("Hold"))
                                json.put("hold", "H");
                            else if (txtHold.getText().toString().equals("PVS"))
                                json.put("hold", "P");
                            else if (txtHold.getText().toString().equals("Manual Sorting"))
                                json.put("hold", "M");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorInResponse("Connecting Error", "Invalid QR Code");
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("json", json.toString());
                    BarcodeResult.setText(Split[0].toString());
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    String URLPUT = Url + "/" + Split[0];
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.PUT,
                            URLPUT,
                            json,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    ShowDataRelised(response.toString());
                                    tb.setVisibility(View.VISIBLE);
                                    txtCutton.setVisibility(View.VISIBLE);
                                    if(BarcodeSetting.equals("China Device")){
                                        register();
                                    }
                                    else if(BarcodeSetting.equals("BlueBird Device")){
                                        OpenBarcode();
                                        registerReceiver();
                                        resetCurrentView();}
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    ErrorInResponse("Connecting Error", "Server TimeOut");
                                }
                            }
                    );
                    requestQueue.add(request);
                }}catch (Exception ex){
                ErrorInResponse("Application Error", "Some Text Error");
            }

        }

    }

    private void ErrorInResponse(String ErrorToast, String ErrorAlert){
        showToast(ErrorToast);
        txtStatus.setText("Please Contact to IS Department");
        ErrorResponse(ErrorAlert);
        txtTimeView.setText("");
        txtStatus.setTextColor(Color.RED);
        BarcodeResult.setText("");
        tb.setVisibility(View.GONE);
        txtCutton.setVisibility(View.GONE);
        if(BarcodeSetting.equals("China Device")){
            register();
        }
        else if(BarcodeSetting.equals("BlueBird Device")){
            OpenBarcode();
            registerReceiver();
            resetCurrentView();}
    }

    private void ShowDataHold(String JsonData){
        if(JsonData != null && JsonData != "") {
            BagInfo JsonBag = new Gson().fromJson(String.valueOf(JsonData), BagInfo.class);


            Grammagelbl.setText(" " + JsonBag.Grammage);
            ColorShadelbl.setText(JsonBag.Color + "/" + JsonBag.Shade);
            ProductCodelbl.setText(JsonBag.Productcode);
//            Gradelbl.setText(JsonBag.Grade);
            NeckTypelbl.setText(JsonBag.Nacktype);
            BatchNolbl.setText(JsonBag.Batchno);
            TareWeightlbl.setText(" " + JsonBag.Tareweight);
            GrossWeightlbl.setText(JsonBag.Grossweight);
            NetWeightlbl.setText(JsonBag.Netweight);
            Pieceslbl.setText(JsonBag.pieces);
            Mobilelbl.setText(" " + JsonBag.Mobile_Info);
            txtStatus.setText(JsonBag.Status_API);
            txtTimeView.setText(JsonBag.Scan_DT);
            Holdlbl.setText(JsonBag.hold);
            if(txtStatus.getText().toString().equals("QR Label Successfully Scanned Hold")){
                txtStatus.setTextColor(Color.parseColor("#15b21d"));
                txtTimeView.setTextColor(Color.parseColor("#15b21d"));
                Toast.makeText(getApplicationContext(), txtStatus.getText().toString(), Toast.LENGTH_LONG).show();
            }else  {
                txtStatus.setTextColor(Color.RED);
                txtTimeView.setTextColor(Color.RED);
            }

            Toast.makeText(getApplicationContext(), String.valueOf(JsonBag.Status_API), Toast.LENGTH_LONG).show();
        }

    }

    private void ShowDataRelised(String JsonData){
        if(JsonData != null && JsonData != "") {
            BagInfo JsonBag = new Gson().fromJson(String.valueOf(JsonData), BagInfo.class);


            Grammagelbl.setText(" " + JsonBag.Grammage);
            ColorShadelbl.setText(JsonBag.Color + "/" + JsonBag.Shade);
            ProductCodelbl.setText(JsonBag.Productcode);
//            Gradelbl.setText(JsonBag.Grade);
            NeckTypelbl.setText(JsonBag.Nacktype);
            BatchNolbl.setText(JsonBag.Batchno);
            TareWeightlbl.setText(" " + JsonBag.Tareweight);
            GrossWeightlbl.setText(JsonBag.Grossweight);
            NetWeightlbl.setText(JsonBag.Netweight);
            Pieceslbl.setText(JsonBag.pieces);
            Mobilelbl.setText(" " + JsonBag.Mobile_Info);
            txtStatus.setText(JsonBag.Status_API);
            txtTimeView.setText(JsonBag.Scan_DT);
            Holdlbl.setText(JsonBag.hold);
            if(txtStatus.getText().toString().equals("Now Carton Released")){
                txtStatus.setTextColor(Color.parseColor("#15b21d"));
                txtTimeView.setTextColor(Color.parseColor("#15b21d"));
                Toast.makeText(getApplicationContext(), txtStatus.getText().toString(), Toast.LENGTH_LONG).show();
            }else  {
                txtStatus.setTextColor(Color.RED);
                txtTimeView.setTextColor(Color.RED);
            }

            Toast.makeText(getApplicationContext(), String.valueOf(JsonBag.Status_API), Toast.LENGTH_LONG).show();
        }

    }



    private void refreshCurrentStatus()
    {
        mStatusText ="Status : "+mCurrentStatus;
    }

    private void setSuccessFailText(String text)
    {
        mSuccessFailText = text.toString();
    }

    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //must have to close when app destroyed
    private void destroyEvent()
    {
        if(!mCurrentStatus.equals(STATUS_CLOSE))
        {
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_BARCODE_CLOSE);
            intent.putExtra(Constants.EXTRA_HANDLE, mBarcodeHandle);
            intent.putExtra(Constants.EXTRA_INT_DATA3, SEQ_BARCODE_CLOSE);
            sendBroadcast(intent);
        }
        if(mProgressDialog!=null) mProgressDialog.dismiss();
        mProgressDialog = null;
        unregisterReceiver();
        stopProgressTimer();
    }

    @Override
    protected void onDestroy() {
        destroyEvent();
        super.onDestroy();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void GetDeviceData(){
        try{
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.getData(Build.MANUFACTURER);
            if (cursor.moveToLast()){
                BarcodeSetting = cursor.getString(2);
            }}catch (Exception ex){
            ErrorResponse("Internal Setting Error.");
        }
    }

    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Error");
        builder.setMessage(Error+" Please Contact to IS Department");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void GetDataDefect(){
        try{
            if(isNetworkConnected() == false){
                Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                return;
            }
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            DefectLov(response.toString());

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ProgressDialogDefects(false);
                    ErrorResponse("Connecting Server Error Or Rqquest TimeOut.");
                }
            }
            );

            requestQueue.add(stringRequest);}catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void DefectLov(String jsonresponse) {
        List<String> Defectitems = new ArrayList<>();
        Gson gson = new Gson();

        Type founderListType = new TypeToken<ArrayList<DefectsInfo>>() {
        }.getType();

        List<DefectsInfo> founderList = gson.fromJson(jsonresponse, founderListType);
        for(DefectsInfo  DI : founderList){
            Defectitems.add(DI.Defects_CD + "- " + DI.Defects_DES);
        }

        final CharSequence[] charSequences = Defectitems.toArray(new CharSequence[Defectitems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Defect Type");
        builder.setSingleChoiceItems(charSequences, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                edtdefect.setText(charSequences[i]);
                if((txtHold.getText().toString().equals("Hold") || txtHold.getText().toString().equals("PVS")|| txtHold.getText().toString().equals("Manual Sorting")) && !edtdefect.getText().toString().equals("")) {
                    SetBarcode();
                }
                alertDialog.dismiss();
            }
        });
        ProgressDialogDefects(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void ProgressDialogDefects(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Searching");
            mProgressDialog.setMessage("Searching Defects...");
            mProgressDialog.setCancelable(false);
        }

        stopProgressTimer();
        if(isShow)
        {
            mProgressDialog.show();
            startProgressTimer();
        }
        else mProgressDialog.dismiss();
    }

}

