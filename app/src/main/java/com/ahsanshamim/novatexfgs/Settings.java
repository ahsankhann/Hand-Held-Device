package com.ahsanshamim.novatexfgs;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    LinearLayout linearLayoutMobile, linearLayoutDevice;
    AlertDialog alertDialog;
    TextView txtMobile, txtDevice;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        linearLayoutMobile = (LinearLayout) findViewById(R.id.MobileNamebtn);
        linearLayoutDevice = (LinearLayout) findViewById(R.id.DeviceNamebtn);
        txtMobile = (TextView) findViewById(R.id.txtmobile);
        txtDevice = (TextView) findViewById(R.id.txtdevice);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GetSettings();
        linearLayoutMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle("Enter Your Mobile Name");

                final EditText input = new EditText(Settings.this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        txtMobile.setText(m_Text);
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        alertDialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        alertDialog.dismiss();
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        linearLayoutDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                final CharSequence[] charSequenceItems = {"BlueBird Device", "China Device"};
                builder.setTitle("Select Scanning Device");
                builder.setSingleChoiceItems(charSequenceItems, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtDevice.setText(charSequenceItems[which]);
                        alertDialog.dismiss();
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        databaseHelper = new DatabaseHelper(this);
        if(txtMobile.getText().toString().equals("None") || txtMobile.getText().toString().equals("")) {
            return true;
        }
        else if(txtDevice.getText().toString().equals("None") || txtDevice.getText().toString().equals("")){
            return true;
        }
        else {
            if (id == R.id.savesetting) {
                // do something here
                switch (id) {
                    case R.id.savesetting:
                        boolean result = databaseHelper.InsertData(Build.MANUFACTURER, txtMobile.getText().toString(), txtDevice.getText().toString());
                        if(result){
                            Toast.makeText(getApplicationContext(), "Setting Saved Successfully", Toast.LENGTH_LONG).show();
                            GetSettings();
                            onBackPressed();
                        }
                        // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
                        break;

                }
            }
        }

        if(id== android.R.id.home){
            onBackPressed();
            return true;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void GetSettings(){
        databaseHelper = new DatabaseHelper(Settings.this);
        try{
        Cursor cursor = databaseHelper.getData(Build.MANUFACTURER);
        if (cursor.moveToLast()){
            txtMobile.setText(cursor.getString(1));
            txtDevice.setText(cursor.getString(2));
        }
        }catch (Exception ex){

        }
    }
}
