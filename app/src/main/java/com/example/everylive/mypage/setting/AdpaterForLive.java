package com.example.everylive.mypage.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.everylive.R;
import com.example.everylive.home.AdatperForSearch;

import java.util.ArrayList;

public class AdpaterForLive extends RecyclerView.Adapter<AdpaterForLive.MyViewHolder>{

    ArrayList<Item_live> itemLiveArrayList;

    public AdpaterForLive(ArrayList<Item_live> itemLiveArrayList){
        this.itemLiveArrayList = itemLiveArrayList;
    }

    @NonNull
    @Override
    public AdpaterForLive.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdpaterForLive.MyViewHolder holder, int position) {
        Item_live item_live = itemLiveArrayList.get(position);

        holder.liveDate.setText(item_live.getStartDate_origin());
        holder.liveTotalTime.setText(item_live.getTotalTime());
        holder.liveTotalCoin.setText(item_live.getTotalCoin());
    }

    @Override
    public int getItemCount() {
        return itemLiveArrayList.size();
    }

    protected class MyViewHolder extends RecyclerView.ViewHolder {

        TextView liveDate, liveTotalTime, liveTotalCoin;

        public MyViewHolder(View itemView) {
            super(itemView);

            liveDate = itemView.findViewById(R.id.liveDate);
            liveTotalTime = itemView.findViewById(R.id.liveTotalTime);
            liveTotalCoin = itemView.findViewById(R.id.liveTotalCoin);
        }
    }
}
