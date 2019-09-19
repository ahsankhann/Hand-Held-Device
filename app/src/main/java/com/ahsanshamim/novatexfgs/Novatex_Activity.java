package com.ahsanshamim.novatexfgs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Novatex_Activity extends AppCompatActivity {
    Button btnScanMolding, btnShowrecords, btnScanLocation, btnScanDC, btnHold, btnRealised, btnDamage;
    AlertDialog alertDialog;
    String username = "";
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatex_);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Novatex Pvt. Ltd.");
        btnScanMolding = (Button) findViewById(R.id.ScanQRbtn);
        btnShowrecords = (Button) findViewById(R.id.showRecbtn);
        btnScanLocation = (Button) findViewById(R.id.Scanlocbtn);
        btnScanDC = (Button) findViewById(R.id.Scandcbtn);
        btnHold = (Button) findViewById(R.id.Holdbtn);
        btnRealised = (Button) findViewById(R.id.Realisedbtn);
        btnDamage = (Button) findViewById(R.id.Damagebtn);

        btnScanMolding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkProduction = AccessGet("PQR1");
                if(checkProduction) {
                    startActivity(new Intent(getApplicationContext(), MoldingScanner_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnShowrecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkRecords = AccessGet("SQR1");
                if(checkRecords) {
                    Intent IT = new Intent(Novatex_Activity.this, ShowRecords_Activity.class);
                    startActivity(IT);
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnScanLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkLocation = AccessGet("LQR1");
                if(checkLocation) {
                    startActivity(new Intent(getApplicationContext(), LocationScan_Actitvity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnScanDC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkDispatch = AccessGet("DQR1");
                if(checkDispatch) {
                    startActivity(new Intent(getApplicationContext(), DiliveryChalan_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkHold = AccessGet("HQR1");
                if(checkHold) {
                    startActivity(new Intent(getApplicationContext(), HoldScanner_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnRealised.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkHold = AccessGet("RQR1");
                if(checkHold) {
                    startActivity(new Intent(getApplicationContext(), Realised_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

        btnDamage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkHold = AccessGet("DQR1");
                if(checkHold) {
                    startActivity(new Intent(getApplicationContext(), Damage_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
            }
        });

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private Boolean AccessGet(String ObjectCode){
        if (username.equals("")){
        db = new DatabaseHelper(this);
        Cursor cursor = db.getDataUser();
        if(cursor.moveToLast()){
            username = cursor.getString(0);
        }
        }

        db = new DatabaseHelper(this);
        Cursor cursor = db.GetAccess(username, ObjectCode);
        if(cursor.moveToFirst()){
            return true;
        }

        return false;

    }
    private void ErrorResponse(String Error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(Error + " Please Contact - IS Department");
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

}
