package com.ahsanshamim.novatexfgs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class DC_RecycleListAdptr extends RecyclerView.Adapter<DC_RecycleListAdptr.MyViewHolder> {

    BagInfo[] BagArray;
    List<BagInfo> BagList;
    Context context;
    String Url = "http://192.168.96.148:8080/api/diliverychalan/";
    AlertDialog alertDialog;
    TextView TotalCount;
    public DC_RecycleListAdptr(Context context, List<BagInfo> BagArray){
        this.BagArray = BagArray.toArray(new BagInfo[BagArray.size()]);
        BagList = BagArray;
        //Foo[] array = list.toArray(new Foo[list.size()]);
        this.context = context;
         TotalCount = (TextView) ((Activity)context).findViewById(R.id.txttotal);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.dc_listviews, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        final BagInfo Bag = BagArray[i];
        myViewHolder.grammagetxt.setText(Bag.Grammage);
        myViewHolder.lblPackagetxt.setText(Bag.lbl_Package_No);
        myViewHolder.colorShadetxt.setText(Bag.Color + "/" + Bag.Shade);

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Record");
                builder.setMessage("Are you Sure You Want to Delete this " + Bag.lbl_Package_No + " From List?");
                final String UrlDelete = Url + Bag.lbl_Package_No;
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, UrlDelete,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(context, Bag.lbl_Package_No + " Delete Successfully", Toast.LENGTH_SHORT).show();
                                        /*DiliveryChalan_Activity DA = new DiliveryChalan_Activity();
                                        DA.DCRecords();*/
                                        BagList.remove(Bag);
                                        BagArray = BagList.toArray(new BagInfo[BagList.size()]);
                                        notifyItemRemoved(i);
                                        notifyItemRangeChanged(i, BagList.size());
                                        TotalCount.setText("Total Cartons Scanned: " + BagList.size());
//                                        notifyDataSetChanged();
                                    }
                                }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ErrorResponse();
                            }
                        }
                        );

                        requestQueue.add(stringRequest);}catch (Exception ex){
                        Toast.makeText(context,ex.getMessage(), Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                    }
                });

                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void ErrorResponse(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Server Error");
        builder.setMessage("Connecting Server Error. Please Contact to IS Department");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.dismiss();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }



    @Override
    public int getItemCount() {
        return BagArray.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView grammagetxt;
        TextView lblPackagetxt;
        TextView colorShadetxt;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            grammagetxt = (TextView) itemView.findViewById(R.id.grammagetxt);
            lblPackagetxt = (TextView) itemView.findViewById(R.id.lblPackagetxt);
            colorShadetxt = (TextView) itemView.findViewById(R.id.colorShadetxt);

        }
    }
}
