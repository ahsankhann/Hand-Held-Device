package com.ahsanshamim.novatexfgs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ShowRecords_RecyclerViewAdptr extends RecyclerView.Adapter<ShowRecords_RecyclerViewAdptr.RecordsViewHolder> {
    BagInfo[] BagData;
    Context context;

    public ShowRecords_RecyclerViewAdptr(Context context, BagInfo[] BagData){
        this.BagData = BagData;
        this.context = context;
    }
    @NonNull
    @Override
    public RecordsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.showrecords_recycleview, viewGroup, false);

        return new RecordsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsViewHolder recordsViewHolder, int i) {
        BagInfo Bag = BagData[i];
        recordsViewHolder.txtgrammge.setText(Bag.Grammage);
        recordsViewHolder.txtlblpakg.setText(Bag.lbl_Package_No);
        recordsViewHolder.txtdatetime.setText(Bag.Scan_DT);
        recordsViewHolder.txtcolorshade.setText(Bag.Color + "/" + Bag.Shade);
    }

    @Override
    public int getItemCount() {
        return BagData.length;
    }

    public class RecordsViewHolder extends RecyclerView.ViewHolder{
        TextView txtgrammge, txtlblpakg, txtdatetime, txtcolorshade;
        public RecordsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtgrammge = (TextView) itemView.findViewById(R.id.grammagetxt);
            txtlblpakg = (TextView) itemView.findViewById(R.id.lblPackagetxt);
            txtdatetime = (TextView) itemView.findViewById(R.id.datetxt);
            txtcolorshade = (TextView) itemView.findViewById(R.id.colorShadetxt);

        }
    }

}
