package com.example.everylive.mypage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.everylive.R;

import java.util.ArrayList;

public class ViewpagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    LayoutInflater inflater;
    ArrayList<Data_Type> mdata;
    String page_owner;

    ViewpagerAdapter(Context context, ArrayList<Data_Type> mdata, String page_owner){
        this.context = context;
        this.mdata = mdata;
        this.page_owner = page_owner;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){ // viewType = getItemViewType 지정해준 Data_Type에 type?
            case 1: {
                View view = inflater.inflate(R.layout.fragment_notice, parent, false); // 프래그먼트 생성해서
                return new ViewHolder_Notice(view, viewType, context, page_owner); // 뷰를 뷰홀더에 넣어준다. 뷰홀더 안에서 뷰 findViewById 가능하다.
            }
            case 2: {
                View view = inflater.inflate(R.layout.fragment_fanboard, parent, false);
                return new ViewHolder_Fanboard(view, viewType, context, page_owner);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mdata.get(position).getType()){ //데이터 모델에서 Type 을 받아들여 ItemViewType으로 씀
            case 1: // A
                return 1;
            case 2: // B
                return 2;
        }
        return 1;
    }
}
