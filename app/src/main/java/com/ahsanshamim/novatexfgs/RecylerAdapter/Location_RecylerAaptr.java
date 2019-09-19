package com.ahsanshamim.novatexfgs.RecylerAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahsanshamim.novatexfgs.BagInfo;
import com.ahsanshamim.novatexfgs.R;

import java.util.List;

public class Location_RecylerAaptr extends RecyclerView.Adapter<Location_RecylerAaptr.MyVieHolder> {

    BagInfo[] BagArray;
    List<BagInfo> BagList;
    Context context;
    String Url = "http://192.168.96.148:8080/api/diliverychalan/";
    AlertDialog alertDialog;
    TextView TotalCount;

    public Location_RecylerAaptr(Context context, List<BagInfo> BagArray){
        this.BagArray = BagArray.toArray(new BagInfo[BagArray.size()]);
        BagList = BagArray;
        //Foo[] array = list.toArray(new Foo[list.size()]);
        this.context = context;
        TotalCount = (TextView) ((Activity)context).findViewById(R.id.txttotal);
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recylerview_loction, viewGroup, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder viewHolder, int i) {
        final BagInfo Bag = BagArray[i];
        viewHolder.grammagetxt.setText(Bag.Grammage);
        viewHolder.lblPackagetxt.setText(Bag.lbl_Package_No);
        viewHolder.colorShadetxt.setText(Bag.Color + "/" + Bag.Shade);
        int j = i;
        j++;
        if(i < 10)
            viewHolder.counttxt.setText("0" + String.valueOf(j));
        else
            viewHolder.counttxt.setText(String.valueOf(j));
    }

    @Override
    public int getItemCount() {
        return BagArray.length;
    }

    public static class MyVieHolder extends RecyclerView.ViewHolder{
        TextView grammagetxt;
        TextView lblPackagetxt;
        TextView colorShadetxt, counttxt;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            grammagetxt = (TextView) itemView.findViewById(R.id.grammagetxt);
            lblPackagetxt = (TextView) itemView.findViewById(R.id.lblPackagetxt);
            colorShadetxt = (TextView) itemView.findViewById(R.id.colorShadetxt);
            counttxt = (TextView) itemView.findViewById(R.id.counttxt);
        }
    }
}
