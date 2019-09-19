package com.ahsanshamim.novatexfgs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import android.widget.Toast;
import android.support.v7.widget.GridLayout;

public class Novatex_Grid extends AppCompatActivity {
    AlertDialog alertDialog;
    String username = "";
    DatabaseHelper db;
    GridLayout gridLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatex__grid);
        gridLayout = (GridLayout)findViewById(R.id.mainGrid);

        setsingleClick(gridLayout);
    }

    private void setsingleClick(GridLayout gridLayout) {
        for (int i=0; i< gridLayout.getChildCount(); i++){
            CardView cardView = (CardView)gridLayout.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentActivity(finalI);
                }
            });
        }
    }


    private void IntentActivity(int i){
        switch (i){
            case 0:
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
                break;

            case 1:
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
                break;

            case 2:
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
                break;

            case 3:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkRecords = AccessGet("SQR1");
                if(checkRecords) {
                    Intent IT = new Intent(Novatex_Grid.this, ShowRecords_Activity.class);
                    startActivity(IT);
                }
                else {
                    ErrorResponse("Access Denied");
                }
                break;

            case 4:
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
                break;

            case 5:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkHold1 = AccessGet("RQR1");
                if(checkHold1) {
                    startActivity(new Intent(getApplicationContext(), Realised_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
                break;

            case 6:
                if (isNetworkConnected() == false) {
                    Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                    return;
                }
                Boolean checkHold2 = AccessGet("DQR1");
                if(checkHold2) {
                    startActivity(new Intent(getApplicationContext(), Damage_Activity.class));
                }
                else {
                    ErrorResponse("Access Denied");
                }
                break;

                default:
                    if (isNetworkConnected() == false) {
                        Toast.makeText(getApplicationContext(), "Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(getApplicationContext(), "No Action Found", Toast.LENGTH_SHORT).show();
                    break;

        }
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
        builder.setMessage(Error + " Please Contact to IS Department");
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
