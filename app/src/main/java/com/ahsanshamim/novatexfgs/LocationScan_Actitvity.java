package com.ahsanshamim.novatexfgs;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsanshamim.novatexfgs.RecylerAdapter.Location_RecylerAaptr;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationScan_Actitvity extends AppCompatActivity {
    private EditText mResultText, BarcodeResult;
    private String mStatusText;
    private String mSuccessFailText;
    private ProgressDialog mProgressDialog;
    private String BarcodeDevice = "";
    private String username = "";
    RecyclerView recyclerView;
    AlertDialog alertDialog;
    String Url = "http://192.168.96.148:8080/api/WH_Location/";
    String WHurl = "http://192.168.96.148:8080/api/WH_Location/";
    String WID = "";
    Boolean SplitError = false;
    private String mCurrentStatus;
    private String mSavedStatus;
    private boolean mIsRegisterReceiver;
    TextView ScanningOption;
    EditText WHName;
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
    TextView txtStatus, txtTimeView;
    private int mSelectedSetParam = -1;
    private int mSelectedGetParam = -1;
    TableLayout tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_scan__actitvity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
        GetDeviceData();
        SetDevice();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    @Override
    protected void onResume() {
        SetDevice();
        super.onResume();
        if(WHName.getText().toString().equals(""))
            ScanningOption.setText("First You Scan Location QR Code");
        else
            ScanningOption.setText("Now you Scan Cuttons QR Code");
    }

    private void SetDevice(){
        GetDeviceData();
        if(BarcodeDevice.length()> 1){
            if(BarcodeDevice.equals("BlueBird Device")){
                registerReceiver();
                resetCurrentView();
                OpenBarcode();
            }else if(BarcodeDevice.equals("China Device")){
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
        if(BarcodeDevice.equals("BlueBird Device")){
            CloseBarcode();
        }else if(BarcodeDevice.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
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
        setTitle("Set Location");

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

    private void SetBarcode(){

    }

    private void initLayout() {
        ScanningOption = (TextView) findViewById(R.id.ScanningOption);
        WHName = (EditText) findViewById(R.id.WHName);

        recyclerView = (RecyclerView) findViewById(R.id.loclist);
        mResultText = (EditText) findViewById(R.id.BarcodeStatus);
        BarcodeResult = (EditText) findViewById(R.id.resultTxt);
        txtStatus = (TextView) findViewById(R.id.Statustxt);
        txtTimeView = (TextView) findViewById(R.id.DatTimmetxt);
//        txtCutton = (TextView) findViewById(R.id.Cuttontxt);

       /* tb = (TableLayout) findViewById(R.id.tblLayout);
        Grammagelbl = (TextView) findViewById(R.id.Grammagelbl);
        ColorShadelbl = (TextView) findViewById(R.id.ColorShadelbl);
        ProductCodelbl = (TextView) findViewById(R.id.ProductCodelbl);
        Gradelbl = (TextView) findViewById(R.id.Gradelbl);
        NeckTypelbl = (TextView) findViewById(R.id.NeckTypelbl);
        BatchNolbl = (TextView) findViewById(R.id.BatchNolbl);
        TareWeightlbl = (TextView) findViewById(R.id.TareWeightlbl);
        GrossWeightlbl = (TextView) findViewById(R.id.GrossWeightlbl);
        NetWeightlbl = (TextView) findViewById(R.id.NetWeightlbl);
        Pieceslbl = (TextView) findViewById(R.id.Pieceslbl);
        Mobilelbl = (TextView) findViewById(R.id.Mobilelbl);


        Grammagelbl1 = (TextView) findViewById(R.id.Grammagelbl1);
        ColorShadelbl1 = (TextView) findViewById(R.id.ColorShadelbl1);
        ProductCodelbl1 = (TextView) findViewById(R.id.ProductCodelbl1);
        Gradelbl1 = (TextView) findViewById(R.id.Gradelbl1);
        NeckTypelbl1 = (TextView) findViewById(R.id.NeckTypelbl1);
        BatchNolbl1 = (TextView) findViewById(R.id.BatchNolbl1);
        TareWeightlbl1 = (TextView) findViewById(R.id.TareWeightlbl1);
        GrossWeightlbl1 = (TextView) findViewById(R.id.GrossWeightlbl1);
        NetWeightlbl1 = (TextView) findViewById(R.id.NetWeightlbl1);
        Pieceslbl1 = (TextView) findViewById(R.id.Pieceslbl1);
        Mobilelbl1 = (TextView) findViewById(R.id.Mobilelbl1);*/
    }

    private void setResultText(String Json)
    {

        mResultText.setText(Json);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void setResulsText(final String Json){
        if(isNetworkConnected() == false){
            Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
            return;
        }
        if(BarcodeDevice.equals("China Device")){
            unregisterReceiver(ChinabroadcastReceiver);
            setResultText("BARCODE_CLOSE");
        }
        else if(BarcodeDevice.equals("BlueBird Device")){
            CloseBarcode();
        }
        if(username.equals("")){
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.getDataUser();
            if(cursor.moveToLast()){
                username = cursor.getString(0);
            }

        }

        if(Json != null && Json != "") {
        try{
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
                                if(BarcodeDevice.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeDevice.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}
                                if(WHName.getText().toString().equals(""))
                                    ScanningOption.setText("First You Scan Location QR Code");
                                else
                                    ScanningOption.setText("Now you Scan Cuttons QR Code");

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ErrorResponse("Connecting Server Error. Please Contact to IS Department");
                                if(BarcodeDevice.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeDevice.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}
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
                                if(JsonDS.Status_API.toString().equals("WareHouse Location Successfully Updated!")) {
                                    BarcodeResult.setText(JsonDS.lbl_Package_No);
                                    txtStatus.setText(JsonDS.Status_API);
                                    txtStatus.setTextColor(Color.parseColor("#15b21d"));
                                    ShowData(response.toString());
                                }else{
                                    BarcodeResult.setText(JsonDS.lbl_Package_No);
                                    txtStatus.setText(JsonDS.Status_API);
                                    txtStatus.setTextColor(Color.parseColor("#ea0404"));
                                }
                                if(BarcodeDevice.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeDevice.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}
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
                                ErrorResponse("Connecting Server Error. Please Contact to IS Department");
                                if(BarcodeDevice.equals("China Device")){
                                    register();
                                }
                                else if(BarcodeDevice.equals("BlueBird Device")){
                                    OpenBarcode();
                                    registerReceiver();
                                    resetCurrentView();}

                            }
                        }

                );
                requestQueue.add(JsonObjec);}
            }catch (Exception ex){
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            if(BarcodeDevice.equals("China Device")){
                register();
            }
            else if(BarcodeDevice.equals("BlueBird Device")){
                OpenBarcode();
                registerReceiver();
                resetCurrentView();}
        }
        }



    }
    List<BagInfo> BagLists = new ArrayList<>();
    private void ShowData(String JsonData){
        if(JsonData != null && JsonData != "") {
            BagInfo JsonBag = new Gson().fromJson(String.valueOf(JsonData), BagInfo.class);
            boolean p = false;
            p = BagLists.contains(JsonBag);
            if(p == false)
                BagLists.add(JsonBag);

            recyclerView.setAdapter(new Location_RecylerAaptr(LocationScan_Actitvity.this, BagLists));



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

    private void GetDeviceData(){
        try{
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.getData(Build.MANUFACTURER);
            if (cursor.moveToLast()){
                BarcodeDevice = cursor.getString(2);
            }}catch (Exception ex){
            ErrorResponse("Device Error. Please Contact to IS Department");
        }
    }
    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DatabaseHelper db = new DatabaseHelper(LocationScan_Actitvity.this);
        boolean result = db.InsertError("Location Activity - " + Error);
        if(result)
            Toast.makeText(getApplicationContext(), "LOG Reported", Toast.LENGTH_SHORT).show();
        builder.setTitle("Server Error");
        builder.setMessage(Error);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();

            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }
}

