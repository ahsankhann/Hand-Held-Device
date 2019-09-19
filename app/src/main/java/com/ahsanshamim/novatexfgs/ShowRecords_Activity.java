package com.ahsanshamim.novatexfgs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ShowRecords_Activity extends AppCompatActivity {
    String Url = "http://192.168.96.148:8080/api/qrscanlog";
//    ListView LV;
    EditText Statustxt;
    AlertDialog alertDialog;
    private ProgressDialog mProgressDialog;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_records_);
        showProgressDialog(true);
        recyclerView= (RecyclerView) findViewById(R.id.ListRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        LV = (ListView) findViewById(R.id.listVieww);
        Statustxt = (EditText) findViewById(R.id.Statustxtt);
    }


    @Override
    protected void onResume() {
        super.onResume();
        GetData();
    }

    private void GetData(){
        try{
            if(isNetworkConnected() == false){
                Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                LogReport("Internet Not Connect Please Connect the WIFI!");
                return;
            }


        RequestQueue requestQueue = Volley.newRequestQueue(this);

            /*JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    Url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ShowList(response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ErrorResponse();
                }
            });*/
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ShowList(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showProgressDialog(false);
                ErrorResponse("Connecting Server Error.");

                //Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
            }
        }
        );


        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        /*request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));*/

        requestQueue.add(stringRequest);}catch (Exception ex){
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void ErrorResponse(String Error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LogReport(Error);
        builder.setTitle("Server Error");
        builder.setMessage(Error +" Please Contact to IS Department");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();
                onBackPressed();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }


    private void ShowList(String Json){
        //List< BagInfo> JsonBag = (List<BagInfo>) new Gson().fromJson(String.valueOf(Json), BagInfo.class);
        if(Json != "[]"){
            try {
                Gson gson = new Gson();

                Type founderListType = new TypeToken<ArrayList<BagInfo>>() {
                }.getType();

                BagInfo[] BagLists = gson.fromJson(Json, BagInfo[].class);

                recyclerView.setAdapter(new ShowRecords_RecyclerViewAdptr(ShowRecords_Activity.this, BagLists));
                /*List<BagInfo> founderList = gson.fromJson(Json, founderListType);

                List<HashMap<String, String>> ListMap = new ArrayList<>();
                for (BagInfo Bag : founderList) {
                    HashMap<String, String> HMap = new HashMap<>();
                    HMap.put("First Line", Bag.lbl_Package_No);
                    HMap.put("Second Line", Bag.Scan_DT);
                    Toast.makeText(getApplicationContext(), HMap.get("First Line").toString(), Toast.LENGTH_LONG);
                    ListMap.add(HMap);
                }

                SimpleAdapter adapter = new SimpleAdapter(this, ListMap, android.R.layout.simple_list_item_2,
                        new String[]{"First Line", "Second Line"},
                        new int[]{android.R.id.text1, android.R.id.text2});
                LV.setAdapter(adapter);*/
                String Count = String.valueOf(BagLists.length);
                Statustxt.setText("Last 24 Hours Records: " + Count);
                Toast.makeText(getApplicationContext(), "Found " + Count + " Records", Toast.LENGTH_LONG).show();
            }catch (Exception ex){
                Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            showProgressDialog(false);
        }
        else {
            Toast.makeText(getApplicationContext(), "No Records Found Last 24 Hours!", Toast.LENGTH_LONG).show();
            Statustxt.setText("Last 24 Hours Records: No Found ");
        }

    }
    private Timer mTimer;
    private void showProgressDialog(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Loading");
            mProgressDialog.setMessage("Loading Records...");
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
                                    ErrorResponse("Connecting Server TimeOut.");
                                }
                            });
                        }

                    }
                }, 50000);
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
    private void LogReport(String Error){
        DatabaseHelper db = new DatabaseHelper(this);
        boolean result = db.InsertError("Show Records - " + Error);
        if(result)
            Toast.makeText(getApplicationContext(), "LOG Reported", Toast.LENGTH_SHORT).show();
    }
}
