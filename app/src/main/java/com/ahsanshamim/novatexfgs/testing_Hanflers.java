package com.ahsanshamim.novatexfgs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class testing_Hanflers {
    Context context;

    private EditText mResultText, BarcodeResult;
    private String mStatusText;
    private String mSuccessFailText;
    private ProgressDialog mProgressDialog;
    String Url = "http://192.168.96.148:8080/api/HoldCorton";
    private String mCurrentStatus;
    private String mSavedStatus;
    private boolean mIsRegisterReceiver;

    private static final String STATUS_CLOSE = "STATUS_CLOSE";
    private static final String STATUS_OPEN = "STATUS_OPEN";
    private static final String STATUS_TRIGGER_ON = "STATUS_TRIGGER_ON";
    private Activity activity;
    private static final int SEQ_BARCODE_OPEN = 100;
    private static final int SEQ_BARCODE_CLOSE = 200;
    private static final int SEQ_BARCODE_GET_STATUS = 300;
    private static final int SEQ_BARCODE_SET_TRIGGER_ON = 400;
    private static final int SEQ_BARCODE_SET_TRIGGER_OFF = 500;
    private static final int SEQ_BARCODE_SET_PARAMETER = 600;
    private static final int SEQ_BARCODE_GET_PARAMETER = 700;
    //TextView txtStatus, txtTimeView, txtCutton;
    //TextView Grammagelbl, ColorShadelbl,ProductCodelbl, Gradelbl, NeckTypelbl, BatchNolbl, TareWeightlbl, GrossWeightlbl, NetWeightlbl, Pieceslbl, Mobilelbl, Holdlbl;
    //TextView Grammagelbl1, ColorShadelbl1,ProductCodelbl1, Gradelbl1, NeckTypelbl1, BatchNolbl1, TareWeightlbl1, GrossWeightlbl1, NetWeightlbl1, Pieceslbl1, Mobilelbl1, Holdlbl1;
    private int mSelectedSetParam = -1;
    private int mSelectedGetParam = -1;
    //TableLayout tb;
    public testing_Hanflers(Context context, EditText mResultText){
        this.context = context;
        this.mResultText = mResultText;
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
        context.sendBroadcast(intent);
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
    //Other Variable Initialized
    private boolean mIsOpened = false;
    private int mBarcodeHandle = -1;
    private int mCount = 0;
    private String[] STATUS_ARR = {STATUS_CLOSE, STATUS_OPEN, STATUS_TRIGGER_ON};

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
                context.sendBroadcast(intent);
                showProgressDialog(true);
                return;
            }
        }
        refreshCurrentStatus();
    }


    private void setResultText(String Json)
    {

        mResultText.setText(Json);
    }

    private Timer mTimer;

    private void showProgressDialog(boolean isShow)
    {
        if(mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(context);
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
                            activity.runOnUiThread(new Runnable() {
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

    private void refreshCurrentStatus()
    {
        mStatusText ="Status : "+mCurrentStatus;
    }

    private void setSuccessFailText(String text)
    {
        mSuccessFailText = text.toString();
    }

    private void CloseBarcode(){
        mSavedStatus = mCurrentStatus;
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BARCODE_CLOSE);
        intent.putExtra(Constants.EXTRA_HANDLE, mBarcodeHandle);
        intent.putExtra(Constants.EXTRA_INT_DATA3, SEQ_BARCODE_CLOSE);
        context.sendBroadcast(intent);
        unregisterReceiver(mReceiver);
        mCurrentStatus = STATUS_CLOSE;
    }

    private void registerReceiver(BroadcastReceiver mReceiver, IntentFilter filter)
    {
        if(mIsRegisterReceiver) return;
        IntentFilter filters = new IntentFilter();
        filters.addAction(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA);
        filters.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS);
        filters.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED);
        filters.addAction(Constants.ACTION_BARCODE_CALLBACK_PARAMETER);
        filters.addAction(Constants.ACTION_BARCODE_CALLBACK_GET_STATUS);

        registerReceiver(this.mReceiver, filter);
        mIsRegisterReceiver = true;
    }

    private void unregisterReceiver(BroadcastReceiver mReceiver)
    {
        if(!mIsRegisterReceiver) return;
        unregisterReceiver(this.mReceiver);
        mIsRegisterReceiver = false;
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int handle = intent.getIntExtra(Constants.EXTRA_HANDLE, 0);
            int seq = intent.getIntExtra(Constants.EXTRA_INT_DATA3, 0);

            if(action.equals(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA))
            {
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

    private void setResulsText(String Json){

    }



}
