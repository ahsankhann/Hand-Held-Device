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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class DiliveryChalan_Activity extends AppCompatActivity {
    private EditText mResultText, BarcodeResult;
    private String mStatusText;
    private String mSuccessFailText;
    private ProgressDialog mProgressDialog;
    private String username = "";
    String Url = "http://192.168.96.148:8080/api/DiliveryChalan";
    //String WHurl = "http://192.168.96.148:8080/api/WH_Location//";
    String WID = "";
    Boolean SplitError = false;
    private String mCurrentStatus;
    private String mSavedStatus;
    private boolean mIsRegisterReceiver;
    private String BarcodeSetting;

    EditText edtDC, edtVehicle;
    AlertDialog alertDialog;
    TextView ScanningOption;
    String Url_Vehicle = "http://192.168.96.148:8080/api/transports";
    RecyclerView recyclerView;
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
    Boolean DeviceOpenClose = false;

    private final static String SCAN_ACTION = "scan.rcv.message";
    private static final String LOG_TAG = "LaserScannerPlugin";

    TextView txtStatus, txttotal;
    private int mSelectedSetParam = -1;
    private int mSelectedGetParam = -1;

    TableLayout tb;
    Button btnmanualentry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dilivery_chalan_);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
        CloseBarcode();
        btnmanualentry.setEnabled(false);

        edtDC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    DocumentTypeSelect();
                    //edtVehicle.requestFocus();
                }
            }
        });
        edtDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentTypeSelect();
                //edtVehicle.requestFocus();

            }
        });

        edtVehicle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ProgressDialogVehicle(true);
                    GetData();
                }
            }
        });

        edtVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialogVehicle(true);
                GetData();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        btnmanualentry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiliveryChalan_Activity.this);
                builder.setTitle("Manual Enter Lbl Number");

                // Set up the input
                final EditText input = new EditText(DiliveryChalan_Activity.this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_NUMBER_FLAG_SIGNED);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        ManualEntry(m_Text);
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        alertDialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        alertDialog.dismiss();
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();

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
                DeviceOpenClose = true;
            }else if(BarcodeSetting.equals("China Device")){
                showProgressDialog(true);
                register();
                DeviceOpenClose = true;
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

    @Override
    protected void onResume() {

        super.onResume();

        if(!edtDC.getText().toString().equals("") && !edtVehicle.getText().toString().equals("")){
            SetBarcode();
            ScanningOption.setText("Now you Scan QR Code for " + edtDC.getText().toString());
            DCRecords();
        }
        else if(edtDC.getText().toString().equals(""))
            ScanningOption.setText("First You Select Document Type");
        else if(edtVehicle.getText().toString().equals(""))
            ScanningOption.setText("Now you Select the Vehicle No.");
        else
            ScanningOption.setText("Now you Scan Cuttons QR Code");
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
        if(!edtDC.getText().toString().equals("") && !edtVehicle.getText().toString().equals("")){
        if(BarcodeSetting.equals("BlueBird Device")){
            CloseBarcode();
            DeviceOpenClose = false;
        }else if(BarcodeSetting.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
            DeviceOpenClose = false;
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
        setResultText("BARCODE_CLOSE");
    }

    private void initialize()
    {
        setTitle("Carton Dispatch");

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

    private void ProgressDialogVehicle(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Searching");
            mProgressDialog.setMessage("Searching Vehicles...");
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
//                tb.setVisibility(View.GONE);

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
        ScanningOption = (TextView) findViewById(R.id.ScanningOption);
        edtDC = (EditText) findViewById(R.id.DocumentTypeEdt);
        edtVehicle = (EditText) findViewById(R.id.VehicleEdt);
        ScanningOption = (TextView) findViewById(R.id.ScanningOption);
        ScanningOption.setText("First You Select Document Type");
        edtDC.setInputType(InputType.TYPE_NULL);;
        edtVehicle.setInputType(InputType.TYPE_NULL);
        recyclerView = (RecyclerView) findViewById(R.id.DCList);
        txttotal = (TextView) findViewById(R.id.txttotal);
        btnmanualentry = (Button) findViewById(R.id.manualentrybtn);


        mResultText = (EditText) findViewById(R.id.BarcodeStatus);
        BarcodeResult = (EditText) findViewById(R.id.resultTxt);
        txtStatus = (TextView) findViewById(R.id.Statustxt);
//        txtTimeView = (TextView) findViewById(R.id.DatTimmetxt);

    }

    private void setResultText(String Json)
    {

        mResultText.setText(Json);
    }

    private void setResulsText(final String Json) {

        if(isNetworkConnected() == false){
            Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
            LogReport("Internet Not Connect Please Connect the WIFI!");
            return;
        }
        if(BarcodeSetting.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
            DeviceOpenClose = false;
        }
        else if(BarcodeSetting.equals("BlueBird Device")){
            CloseBarcode();
            DeviceOpenClose = false;
        }
        if(username.equals("")){
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.getDataUser();
            if(cursor.moveToLast()){
                username = cursor.getString(0);
            }

        }
        if (Json != null && Json != "") {
            try {
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
                        json.put("dct_cd", edtDC.getText().toString());
                        json.put("veh_no", edtVehicle.getText().toString());
                        json.put("username", username);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if(Split.length == 12){
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
                        json.put("dct_cd", edtDC.getText().toString());
                        json.put("veh_no", edtVehicle.getText().toString());
                        json.put("username", username);


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
                                BagInfo JsonDS = new Gson().fromJson(String.valueOf(response), BagInfo.class);
                                BarcodeResult.setText(JsonDS.lbl_Package_No);
                                txtStatus.setText(JsonDS.Status_API);
                                txtStatus.setTextColor(Color.parseColor("#15b21d"));
//                                txtTimeView.setText(JsonDS.Scan_DT);
                                if(txtStatus.getText().toString().equals("QR Label Successfully Scanned")){
                                    txtStatus.setTextColor(Color.parseColor("#15b21d"));
//                                    txtTimeView.setTextColor(Color.parseColor("#15b21d"));
                                    edtDC.setEnabled(false);
                                    edtVehicle.setEnabled(false);
                                    Toast.makeText(getApplicationContext(), txtStatus.getText().toString(), Toast.LENGTH_SHORT).show();
                                }else  {
                                    txtStatus.setTextColor(Color.RED);
//                                    txtTimeView.setTextColor(Color.RED);
                                }

                                Toast.makeText(getApplicationContext(), String.valueOf(JsonDS.Status_API), Toast.LENGTH_SHORT).show();
                                if(BarcodeSetting.equals("China Device")){
                                    register();
                                    DeviceOpenClose = true;
                                }
                                else if(BarcodeSetting.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();
                                    DeviceOpenClose = true;}

                                if(!edtVehicle.getText().toString().equals("")){
                                    DCRecords();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (SplitError.equals(true)) {
                                    txtStatus.setText("Invalid QR Code");
                                    txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                } else {
                                    txtStatus.setText("Server Error Please Contact IS Department");
                                    txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                    ErrorResponse("Connecting Server Error Or Request TimeOut.");
                                }
                                SplitError = false;
                                //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                                if(BarcodeSetting.equals("China Device")){
                                    register();
                                    DeviceOpenClose = true;
                                }
                                else if(BarcodeSetting.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();
                                    DeviceOpenClose = true;}
                            }
                        }
                );
                requestQueue.add(request);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                txtStatus.setText("QR Code Invalid");
                txtStatus.setTextColor(Color.RED);
                if(BarcodeSetting.equals("China Device")){
                    register();
                    DeviceOpenClose = true;
                }
                else if(BarcodeSetting.equals("BlueBird Device")){
                    OpenBarcode();
                    registerReceiver();
                    resetCurrentView();
                    DeviceOpenClose = true;}
            }
            /*try{
                if(WHName.getText().toString().equals("")){
                    String[] Split = Json.toString().split(",");
                    String ComplUrl = WHurl + Split[0];
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.GET,
                            ComplUrl,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    WareHouseInfo JsonDS = new Gson().fromJson(String.valueOf(response), WareHouseInfo.class);
                                    WHName.setText(JsonDS.WLOC_FULL_DESC);
//                                WHName.setTextColor(Color.BLUE);
                                    WID = JsonDS.WLOC_CD;
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();
                                    if(WHName.getText().toString().equals(""))
                                        ScanningOption.setText("First You Scan Location QR Code");
                                    else
                                        ScanningOption.setText("Now you Scan Cuttons QR Code");

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    showToast("Ware House QR Code Invalid!");
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();
                                }
                            }
                    );
                    requestQueue.add(request);
                }else {

                    //BagInfo JsonBag = new Gson().fromJson(String.valueOf(Json), BagInfo.class);
                    String[] Split = Json.toString().split(",");
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    String ComplUrl = Url + Split[0];
                    BarcodeResult.setText(Split[0]);
                    WareHouseInfo WHS = new WareHouseInfo();
                    WHS.WLOC_CD = WID;
                    WHS.WLOC_FULL_DESC = WHName.getText().toString();
                    //JsonObject Jobj  = new Gson().toJson(WHS);
                    JSONObject json = new JSONObject();
                    try {
                        json.put("WLOC_CD", WID.toString());
                        json.put("WLOC_FULL_DESC", WHName.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SplitError = true;
                    }
                    // ...
                    // "serialize"
                    Bundle bundle = new Bundle();
                    bundle.putString("json", json.toString());

                    JsonObjectRequest JsonObjec = new JsonObjectRequest(
                            Request.Method.PUT,
                            ComplUrl,
                            json,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    BagInfo JsonDS = new Gson().fromJson(String.valueOf(response), BagInfo.class);
                                    BarcodeResult.setText(JsonDS.lbl_Package_No);
                                    txtStatus.setText(JsonDS.Status_API);
                                    txtStatus.setTextColor(Color.parseColor("#15b21d"));
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();
                                    //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if(SplitError.equals(true)){
                                        txtStatus.setText("Invalid QR Code");
                                        txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                    }
                                    else {
                                        txtStatus.setText("Server Error Please Contact IS Department");
                                        txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                    }
                                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();

                                }
                            }

                    );
                    requestQueue.add(JsonObjec);}
            }catch (Exception ex){
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                OpenBarcode();
                registerReceiver();
                resetCurrentView();
            }*/
        }

    }
    private void ErrorInResponse(String ErrorToast, String ErrorAlert){
        Toast.makeText(getApplicationContext(), ErrorToast, Toast.LENGTH_LONG).show();
        txtStatus.setText("QR Code Invalid");
        txtStatus.setTextColor(Color.RED);
        ErrorResponse(ErrorAlert);
        if(BarcodeSetting.equals("China Device")){
            register();
            DeviceOpenClose = true;
        }
        else if(BarcodeSetting.equals("BlueBird Device")){
            OpenBarcode();
            registerReceiver();
            resetCurrentView();
            DeviceOpenClose = true;}
    }

    private void ShowData(String JsonData){
        if(JsonData != null && JsonData != "") {
            BagInfo JsonBag = new Gson().fromJson(String.valueOf(JsonData), BagInfo.class);
            txtStatus.setText(JsonBag.Status_API);
//            txtTimeView.setText(JsonBag.Scan_DT);
            if(txtStatus.getText().toString().equals("QR Label Successfully Scanned")){
                txtStatus.setTextColor(Color.GREEN);
//                txtTimeView.setTextColor(Color.GREEN);
                Toast.makeText(getApplicationContext(), txtStatus.getText().toString(), Toast.LENGTH_SHORT).show();
            }else  {
                txtStatus.setTextColor(Color.RED);
//                txtTimeView.setTextColor(Color.RED);
            }

            Toast.makeText(getApplicationContext(), String.valueOf(JsonBag.Status_API), Toast.LENGTH_SHORT).show();
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

    private void DocumentTypeSelect(){
        final CharSequence[] values = {"DC", "ET","IT","SR","GR"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Document Type");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), values[which], Toast.LENGTH_SHORT).show();
                CloseBarcode();
                edtDC.setText(values[which]);
                if(!edtDC.getText().toString().equals(""))
                    ScanningOption.setText("Now you Select Vehicle No.");

                if(!edtDC.getText().toString().equals("") && !edtVehicle.getText().toString().equals("")){
                    SetBarcode();
                    ScanningOption.setText("Now you Scan QR Code for " + edtDC.getText().toString());
                    if(!edtVehicle.getText().toString().equals("")){
                        DCRecords();
                    }
                }
                if(edtVehicle.getText().toString().equals(""))
                    ScanningOption.setText("Now you Select the Vehicle No.");
                alertDialog.dismiss();

            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void VehicleSelect(String Json){
//        CharSequence[] values = new CharSequence[];

        List<String> listItems = new ArrayList<String>();
        Gson gson = new Gson();

        Type founderListType = new TypeToken<ArrayList<VehicleDetails>>() {
        }.getType();

        List<VehicleDetails> founderList = gson.fromJson(Json, founderListType);
        for(VehicleDetails VD : founderList){
            listItems.add(VD.Vehical_No);
        }
        final CharSequence[] charSequenceItems = listItems.toArray(new CharSequence[listItems.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Vehicle No.");
        builder.setSingleChoiceItems(charSequenceItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), charSequenceItems[which], Toast.LENGTH_SHORT).show();
                CloseBarcode();
                edtVehicle.setText(charSequenceItems[which]);
                if(!edtDC.getText().toString().equals("") && !edtVehicle.getText().toString().equals("")){
                    SetBarcode();
                    ScanningOption.setText("Now you Scan QR Code for " + edtDC.getText().toString());
                    if(!edtVehicle.getText().toString().equals("")){
                        DCRecords();
                    }
                }
                if(edtDC.getText().toString().equals(""))
                    ScanningOption.setText("Now you Select the Document Type");
                alertDialog.dismiss();

            }
        });
        ProgressDialogVehicle(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void GetData(){
        try{
            if(isNetworkConnected() == false){
                Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                LogReport("Internet Not Connect Please Connect the WIFI!");
                return;
            }
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Url_Vehicle,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            VehicleSelect(response.toString());

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ProgressDialogVehicle(false);
                    ErrorResponse("Connecting Server Error Or Rqquest TimeOut.");
                }
            }
            );

            requestQueue.add(stringRequest);}catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LogReport(Error);
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


    public  void DCRecords(){
        try{
            if(isNetworkConnected() == false){
                Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                LogReport("Internet Not Connect Please Connect the WIFI!");
                return;
            }
            btnmanualentry.setEnabled(true);
            String CompUrl = Url + "/" + edtVehicle.getText();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, CompUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //VehicleSelect(response.toString());
                            if(response != "[]") {
                                Gson gson = new Gson();
                                Type founderListType = new TypeToken<ArrayList<BagInfo>>() {
                                }.getType();

                                List<BagInfo> BagLists = gson.fromJson(response, founderListType);

                                recyclerView.setAdapter(new DC_RecycleListAdptr(DiliveryChalan_Activity.this, BagLists));

                                txttotal.setText("Total Cartons Scanned: " + BagLists.size());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ErrorResponse("Connecting Server Error.");
                }
            }
            );

            requestQueue.add(stringRequest);}catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void ManualEntry(String Json){
        try {
            if(isNetworkConnected() == false){
                Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                LogReport("Internet Not Connect Please Connect the WIFI!");
                return;
            }
            if(BarcodeSetting.equals("China Device")){
                unregisterReceiver(ChinabroadcastReceiver);
                setResultText("BARCODE_CLOSE");
            }
            else if(BarcodeSetting.equals("BlueBird Device")){
                CloseBarcode();
            }
            //BagInfo JsonBag = new Gson().fromJson(String.valueOf(Json), BagInfo.class);

//            final String[] Split = Json.split(",");


            final JSONObject json = new JSONObject();
            try {
                json.put("lbl_Package_No", Json);

                json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);
                json.put("dct_cd", edtDC.getText().toString());
                json.put("veh_no", edtVehicle.getText().toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String PutUrl = Url + "/" + Json;
            Bundle bundle = new Bundle();
            bundle.putString("json", json.toString());
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    PutUrl,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            BagInfo JsonDS = new Gson().fromJson(String.valueOf(response), BagInfo.class);
                            BarcodeResult.setText(JsonDS.lbl_Package_No);
                            txtStatus.setText(JsonDS.Status_API);
                            txtStatus.setTextColor(Color.parseColor("#15b21d"));
//                                txtTimeView.setText(JsonDS.Scan_DT);
                            if(txtStatus.getText().toString().equals("QR Label Successfully Scanned")){
                                txtStatus.setTextColor(Color.parseColor("#15b21d"));
//                                    txtTimeView.setTextColor(Color.parseColor("#15b21d"));
                                edtDC.setEnabled(false);
                                edtVehicle.setEnabled(false);
                                Toast.makeText(getApplicationContext(), txtStatus.getText().toString(), Toast.LENGTH_SHORT).show();
                            }else  {
                                txtStatus.setTextColor(Color.RED);
//                                    txtTimeView.setTextColor(Color.RED);
                            }

                            Toast.makeText(getApplicationContext(), String.valueOf(JsonDS.Status_API), Toast.LENGTH_SHORT).show();
                            if(BarcodeSetting.equals("China Device")){
                                register();
                            }
                            else if(BarcodeSetting.equals("BlueBird Device")){
                                OpenBarcode();
                                registerReceiver();
                                resetCurrentView();}

                            if(!edtVehicle.getText().toString().equals("")){
                                DCRecords();
                            }

//                            InputMethodManager inputManager =
//                                    (InputMethodManager) context.
//                                            getSystemService(Context.INPUT_METHOD_SERVICE);
//                            inputManager.hideSoftInputFromWindow(
//                                    this.getCurrentFocus().getWindowToken(),
//                                    InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (SplitError.equals(true)) {
                                txtStatus.setText("Invalid QR Code");
                                txtStatus.setTextColor(Color.parseColor("#ea0404"));
                            } else {
                                txtStatus.setText("Not Found Please Contact to IS Department");
                                txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                ErrorResponse("Not Found.");
                            }
                            SplitError = false;
                            //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
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
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            txtStatus.setText("QR Code Invalid");
            txtStatus.setTextColor(Color.RED);
            if(BarcodeSetting.equals("China Device")){
                register();
            }
            else if(BarcodeSetting.equals("BlueBird Device")){
                OpenBarcode();
                registerReceiver();
                resetCurrentView();}
        }
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

        }
    }

    private void LogReport(String Error){
        DatabaseHelper db = new DatabaseHelper(this);
        boolean result = db.InsertError("Dispatch Cartons - " + Error);
        if(result)
            Toast.makeText(getApplicationContext(), "LOG Reported", Toast.LENGTH_SHORT).show();
    }


}

