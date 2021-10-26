package com.example.everylive.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.mypage.Activity_Mypage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdatperForSearch extends RecyclerView.Adapter<AdatperForSearch.MyViewHolder>  implements Filterable {

    Context context;
    private ArrayList<ItemForSearch> dataSet;
    private ArrayList<ItemForSearch> fullList;

    class MyViewHolder extends RecyclerView.ViewHolder{

        String idx_user;
        CircleImageView userProfileIMG;
        TextView userNickname;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileIMG = itemView.findViewById(R.id.userProfileIMG);
            userNickname = itemView.findViewById(R.id.userNickName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, Activity_Mypage.class);
                    i.putExtra("page_owner", idx_user);
                    context.startActivity(i);
                    ((Activity)context).finish();
                }
            });
        }
    }

    public AdatperForSearch(ArrayList<ItemForSearch> itemList, Context context){
        this.context = context;
        this.dataSet = itemList;
        this.fullList = new ArrayList<>(itemList); // arrayList를 생성하며 list.add()한 것과 동일한 효과
    }

    @NonNull
    @Override
    public AdatperForSearch.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_for_search,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdatperForSearch.MyViewHolder holder, int position) {
        ItemForSearch currentItem = dataSet.get(position);

        holder.idx_user = dataSet.get(position).getUserIdx();
        Glide.with(context).load(currentItem.getImgUrl()).into(holder.userProfileIMG);
        holder.userNickname.setText(currentItem.getNickname());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public Filter getFilter() {
        return Searched_Filter;
    }

    private Filter Searched_Filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ItemForSearch> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // 입력값이 없으면 필터리스트에 전체 값을 넣는다...
                filteredList.addAll(fullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ItemForSearch item : fullList) {
                    // filterPattern로 시작하는 값을 filteredList에 추가한다.
                    if(item.getNickname().toLowerCase().startsWith(filterPattern)){
                        filteredList.add(item);
                    }

                    // filterPattern 포함하는 값을 filteredList에 추가한다.
//                    if (item.getNickname().toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item);
//                    }

                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            dataSet.clear();
            dataSet.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
