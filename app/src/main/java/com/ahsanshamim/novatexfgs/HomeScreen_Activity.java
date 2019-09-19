package com.ahsanshamim.novatexfgs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class HomeScreen_Activity extends AppCompatActivity {
    ImageButton btnGandT, btnNovatex, btnGatron, btnKrystalite;
    AlertDialog alertDialog;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    TextView username, fullname;
    NavigationView navigationView;
    String Url = "http://192.168.96.148:8080/api/LoginQRCode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_);

        setTitle("Home");
        navigationView = (NavigationView) findViewById(R.id.navigationViwer);
        SetupDrawer();
        AccessCheck();
        btnGandT = (ImageButton) findViewById(R.id.gandtbtn);
        btnNovatex = (ImageButton) findViewById(R.id.novatexbtn);
        btnGatron = (ImageButton) findViewById(R.id.gatronbtn);
        btnKrystalite = (ImageButton) findViewById(R.id.krystalitebtn);


        btnNovatex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Novatexbtton();

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.homeitem:

                        break;

                    case R.id.novatexitem:
                        Novatexbtton();
                        break;

                    case R.id.settingitem:
                        Intent IT = new Intent(HomeScreen_Activity.this, Settings.class);
                        startActivity(IT);
                        break;

                    case R.id.logout:
                        DatabaseHelper db = new DatabaseHelper(HomeScreen_Activity.this);
                        db.deleteTitle();
                        finish();
                        System.exit(0);
                        break;
                }
                return false;
            }
        });

    }

    private void Novatexbtton(){
        try{
            DatabaseHelper db = new DatabaseHelper(HomeScreen_Activity.this);
            Cursor cursor = db.getData(Build.MANUFACTURER);
            if (cursor.moveToLast()){
                String Device = cursor.getString(2);
                Intent IT = new Intent(HomeScreen_Activity.this, Novatex_Grid.class);
                startActivity(IT);
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen_Activity.this);
                builder.setTitle("Information");
                builder.setMessage("You're not Select your Device Goto Setting and Select your Device first. Press Setting to go Settings menu.");
                builder.setPositiveButton("Goto Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent IT = new Intent(HomeScreen_Activity.this, Settings.class);
                        alertDialog.dismiss();
                        startActivity(IT);
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        }catch (Exception ex){
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen_Activity.this);
            builder.setTitle("Information");
            builder.setMessage("You're not Select your Device Goto Setting and Select your Device first. Press Setting to go Settings menu.");
            builder.setPositiveButton("Goto Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent IT = new Intent(HomeScreen_Activity.this, Settings.class);
                    alertDialog.dismiss();
                    startActivity(IT);
                }
            });

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void SetupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        username = (TextView) headerView.findViewById(R.id.username_navi);
        fullname = (TextView) headerView.findViewById(R.id.fullname_navi);
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor cursor = db.getDataUser();
        if(cursor.moveToLast()){
            username.setText(cursor.getString(0));
            fullname.setText(cursor.getString(2));
        }

            navigationView.getMenu().getItem(0).setChecked(true);


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settingsbtn) {
            Intent IT = new Intent(HomeScreen_Activity.this, Settings.class);
            startActivity(IT);
        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void AccessCheck() {
        if(isNetworkConnected() == false){
            Toast.makeText(getApplicationContext(),"Internet Not Connect Please Connect the WIFI!", Toast.LENGTH_LONG).show();
            LogReport("Internet Not Connect Please Connect the WIFI!");
            return;
        }
        String username = "";
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor cursor = db.getDataUser();
        if(cursor.moveToLast()){
            username = cursor.getString(0);
        }
        String UrlGet = Url + "/" + username;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, UrlGet,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(!response.equals("")){


                                Gson gson = new Gson();

                                Type founderListType = new TypeToken<ArrayList<LoginUsers>>() {
                                }.getType();

                                LoginUsers[] AccessList = gson.fromJson(response, LoginUsers[].class);
                                DatabaseHelper dbs = new DatabaseHelper(HomeScreen_Activity.this);
                                dbs.deleteAccess();
                                for(LoginUsers LU : AccessList){
                                    DatabaseHelper db = new DatabaseHelper(HomeScreen_Activity.this);
                                    boolean result = db.InsertAccess(LU.Username, LU.Object_Code, LU.Object_Name);

                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ErrorResponse();
                    //Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
                }
            }
            );

            requestQueue.add(stringRequest);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void ErrorResponse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LogReport("Server Error");
        builder.setTitle("Server Error");
        builder.setMessage("Connecting Server Error. Please Contact to IS Department");
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

    private void LogReport(String Error){
        DatabaseHelper db = new DatabaseHelper(this);
        boolean result = db.InsertError("Auto Login - Connecting Server Error. Please Contact to IS Department");
        if(result)
            Toast.makeText(getApplicationContext(), "LOG Reported", Toast.LENGTH_SHORT).show();
    }
}
