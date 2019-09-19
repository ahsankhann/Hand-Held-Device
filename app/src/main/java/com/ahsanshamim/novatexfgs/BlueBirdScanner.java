package com.ahsanshamim.novatexfgs;


import android.content.Context;

public class BlueBirdScanner {
    Context context;

    /*private BroadcastReceiver mReceiver = new BroadcastReceiver()
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
    };*/
}
