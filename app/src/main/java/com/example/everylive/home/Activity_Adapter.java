package com.example.everylive.home;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.livestream.Activity_Liveplayer;

import java.util.ArrayList;

public class Activity_Adapter extends RecyclerView.Adapter<Activity_Adapter.ViewHolder>{

    private ArrayList<String> mData = null ;
    private ArrayList<Activity_Items> items = new ArrayList<>();

    Context mcontext;
    String idx_user;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView roomIMG, genderIMG;
        TextView roomcategory, roomtitle, roomcontents, nickName, participants;



        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            roomIMG = itemView.findViewById(R.id.roomIMG);
            genderIMG = itemView.findViewById(R.id.genderIMG);
            roomcategory = itemView.findViewById(R.id.roomcategory);
            roomtitle = itemView.findViewById(R.id.roomtitle);
            roomcontents = itemView.findViewById(R.id.roomcontents);
            nickName = itemView.findViewById(R.id.nickName);
            participants = itemView.findViewById(R.id.participants);


            // 리사이클러뷰 아이템 클릭리스너
            itemView .setOnClickListener(new View.OnClickListener() { // 리사이클러뷰 뷰홀더(ViewHolder)에서 아이템 클릭 이벤트 처리하기.
                @Override
                public void onClick(View v) {

                    // 메서드가 리턴하는 값은 어댑터 내 아이템의 위치(position)이지만,
                    // 리턴 값이 NO_POSITION인지에 대한 검사는 해줘야 합니다.
                    // notifyDataSetChanged()에 의해 리사이클러뷰가 아이템뷰를 갱신하는 과정에서,
                    // 뷰홀더가 참조하는 아이템이 어댑터에서 삭제되면 getAdapterPosition() 메서드는
                    // NO_POSITION을 리턴하기 때문입니다.
                    int pos = getAdapterPosition(); // 아이템 위치 알아내기
                    if (pos != RecyclerView.NO_POSITION) {
                        // TODO : use pos.

                        // 액티비티 이동
                        Intent intent = new Intent(mcontext, Activity_Liveplayer.class);
                        intent.putExtra("idx_room", idx_user);
                        mcontext.startActivity(intent);

                    }
                }
            });

        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public Activity_Adapter(ArrayList<String> list) {
        mData = list ;
    }

    // 온크리에이트뷰홀더 - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public Activity_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        mcontext = context; // 전역변수에 있는 컨텍스트에 토스.

        // 아이템 담을 뷰홀더 레이아웃 만들어야함.
        View view = inflater.inflate(R.layout.activity_home_items, parent, false) ;
        Activity_Adapter.ViewHolder vh = new Activity_Adapter.ViewHolder(view) ;

        return vh ;
    }

    // 온바인드뷰홀더 - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(Activity_Adapter.ViewHolder holder, int position) {

        Activity_Items item = items.get(position);

        Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                .load(item.getRoomIMG()) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                .override(120, 120) // 이미지 사이즈 조절
                .into(holder.roomIMG); // into() : 이미지를 보여줄 View를 지정한다.

        if(item.getGender().equals("M")){
            Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                    .load(R.drawable.male) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                    .override(30, 30) // 이미지 사이즈 조절
                    .into(holder.genderIMG); // into() : 이미지를 보여줄 View를 지정한다.
        }else if(item.getGender().equals("F")){
            Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                    .load(R.drawable.female) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                    .override(30, 30) // 이미지 사이즈 조절
                    .into(holder.genderIMG); // into() : 이미지를 보여줄 View를 지정한다.

        }


        holder.roomcategory.setText(item.getRoomcategory());
        holder.roomtitle.setText(item.getRoomtitle());
        holder.roomcontents.setText(item.getRoomcontents());
        holder.nickName.setText(item.getNickName());
        holder.participants.setText(item.getParticipants()+" 명 시청중");
        idx_user = item.getIdx_user();


    } // 온바인드뷰홀더



    // 메인액티비티에서 아이템 로드하는데 사용하는 메서드.
    public void setItems(ArrayList<Activity_Items> items) {
        this.items = items;
    }

    // 매인액티비티에서 아이템 전체 삭제하는데 사용하는 메서드.
    public void removeItems() {
        items.clear(); // 아이템 전부 삭제
        notifyDataSetChanged(); // 데이터 갱신
    }

    // 전체 데이터 갯수 리턴하는 오버라이드
    @Override
    public int getItemCount() {
//            return mData.size() ;
        return items.size();
    }

}



