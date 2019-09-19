package com.ahsanshamim.novatexfgs;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    EditText EdtScan;
    String[] SplitQR;
    TextView txtStatus;
    String Url = "http://192.168.96.148:8080/api/qrscanlog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdtScan = (EditText) findViewById(R.id.ScanDataedt);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        EdtScan.setInputType(InputType.TYPE_NULL);
        EdtScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(EdtScan.getText().toString().length() >0)
                    EdtScan.selectAll();
                return false;
            }
        });


        EdtScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EdtScan.getText().toString().length() >0)
                    EdtScan.selectAll();
            }
        });
        EdtScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    EdtScan.selectAll();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(EdtScan.getText().toString().length() > 26){
                    SplitData(EdtScan.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EdtScan.isFocused();
        if(EdtScan.getText().toString().length() >0)
            EdtScan.selectAll();
    }

    private void SplitData(String QRtext){
        final JSONObject json = new JSONObject();
        try{
            SplitQR = QRtext.split(",");

            EdtScan.setText(SplitQR[0]);
            EdtScan.selectAll();
            try {
                json.put("lbl_Package_No",  SplitQR[0]);
                json.put("Grammage",  SplitQR[1]);
                json.put("Color",  SplitQR[2]);
                json.put("Shade",  SplitQR[3]);
                json.put("Productcode",  SplitQR[4]);
                json.put("Grade",  SplitQR[5]);
                json.put("Nacktype",  SplitQR[6]);
                json.put("Batchno",  SplitQR[7]);
                json.put("pieces",  SplitQR[8]);
                json.put("Netweight",  SplitQR[9]);
                json.put("Grossweight",  SplitQR[10]);
                json.put("Mobile_Info", Build.MANUFACTURER + " " + Build.MODEL);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch (Exception ex){

        }

        Bundle bundle = new Bundle();
        bundle.putString("json", json.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Url,
                json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                    }
                },
        new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), String.valueOf(error.getMessage()), Toast.LENGTH_LONG).show();
                        txtStatus.setText("QR Code Problem");
                        txtStatus.setTextColor(Color.RED);
                    }
                }
        );
        requestQueue.add(request);
        EdtScan.isFocused();
    }
}
