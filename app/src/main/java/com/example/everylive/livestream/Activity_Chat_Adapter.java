package com.example.everylive.livestream;
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

import java.util.ArrayList;

public class Activity_Chat_Adapter extends RecyclerView.Adapter<Activity_Chat_Adapter.ViewHolder>{

    private ArrayList<String> mData = null ;
    private ArrayList<com.example.everylive.livestream.Activity_Chat_Items> items = new ArrayList<>();

    Context mcontext;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nickName, content, middle, bj;
        ImageView profileIMG;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            nickName = itemView.findViewById(R.id.livechatnickname);
            content = itemView.findViewById(R.id.livechatcontent);
            middle= itemView.findViewById(R.id.middlemessage);
            profileIMG= itemView.findViewById(R.id.livechatimg);
            bj = itemView.findViewById(R.id.bj);

//            // 리사이클러뷰 아이템 클릭리스너
//            itemView .setOnClickListener(new View.OnClickListener() { // 리사이클러뷰 뷰홀더(ViewHolder)에서 아이템 클릭 이벤트 처리하기.
//                @Override
//                public void onClick(View v) {
//
//                    // 메서드가 리턴하는 값은 어댑터 내 아이템의 위치(position)이지만,
//                    // 리턴 값이 NO_POSITION인지에 대한 검사는 해줘야 합니다.
//                    // notifyDataSetChanged()에 의해 리사이클러뷰가 아이템뷰를 갱신하는 과정에서,
//                    // 뷰홀더가 참조하는 아이템이 어댑터에서 삭제되면 getAdapterPosition() 메서드는
//                    // NO_POSITION을 리턴하기 때문입니다.
//                    int pos = getAdapterPosition(); // 아이템 위치 알아내기
//                    if (pos != RecyclerView.NO_POSITION) {
//                        // TODO : use pos.
//
//                        // 액티비티 이동
//                        Intent intent55 = new Intent(mcontext, MainHome.class);
//                        mcontext.startActivity(intent55);
//
//                    }
//                }
//            });

        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public Activity_Chat_Adapter(ArrayList<String> list) {
        mData = list ;
    }

    // 온크리에이트뷰홀더 - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public Activity_Chat_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        mcontext = context; // 전역변수에 있는 컨텍스트에 토스.

        // 아이템 담을 뷰홀더 레이아웃 만들어야함.
        View view = inflater.inflate(R.layout.activity_chat_items, parent, false) ;
        Activity_Chat_Adapter.ViewHolder vh = new Activity_Chat_Adapter.ViewHolder(view) ;

        return vh ;
    }

    // 온바인드뷰홀더 - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(Activity_Chat_Adapter.ViewHolder holder, int position) {

        com.example.everylive.livestream.Activity_Chat_Items item = items.get(position);


        // bj 라면 bj 표시를 해준다.
        if(item.getWhois().equals("Bj")){

            holder.nickName.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
            holder.profileIMG.setVisibility(View.VISIBLE);
            holder.bj.setVisibility(View.VISIBLE);

            holder.bj.setText("BJ");
            holder.nickName.setText(item.getNickName());
            holder.content.setText(item.getContent());
            Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                    .load(item.getProfileIMGurl()) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                    .override(70, 70) // 이미지 사이즈 조절
                    .into(holder.profileIMG); // into() : 이미지를 보여줄 View를 지정한다.


            // bj가 아닌 경우 기본 채팅 내용만 표시된다.
        }else if(item.getWhois().equals("viewer")){

            holder.nickName.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
            holder.profileIMG.setVisibility(View.VISIBLE);
            holder.bj.setVisibility(View.INVISIBLE);

            holder.nickName.setText(item.getNickName());
            holder.content.setText(item.getContent());
            Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
                    .load(item.getProfileIMGurl()) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
                    .override(70, 70) // 이미지 사이즈 조절
                    .into(holder.profileIMG); // into() : 이미지를 보여줄 View를 지정한다.


        }else if(item.getWhois().equals("giftcoin")){
//            holder.nickName.setVisibility(View.VISIBLE);
//            holder.content.setVisibility(View.INVISIBLE);
//            holder.profileIMG.setVisibility(View.VISIBLE);
//            holder.bj.setVisibility(View.INVISIBLE);
//            holder.middle.setVisibility(View.VISIBLE);
//
//            holder.middle.setText(item.getContent());
//            holder.nickName.setText(item.getNickName());
//            Glide.with(mcontext) // with() : View, Fragment 혹은 Activity로부터 Context를 가져온다.
//                    .load(item.getProfileIMGurl()) // load() :  이미지를 로드한다. 다양한 방법으로 이미지를 불러올 수 있다. (Bitmap, Drawable, String, Uri, File, ResourId(Int), ByteArray)
//                    .override(70, 70) // 이미지 사이즈 조절
//                    .into(holder.profileIMG); // into() : 이미지를 보여줄 View를 지정한다.

        }
    } // 온바인드뷰홀더



    // 메인액티비티에서 아이템 로드하는데 사용하는 메서드.
    public void setItems(ArrayList<com.example.everylive.livestream.Activity_Chat_Items> items) {
        this.items = items;
    }

    // 매인액티비티에서 아이템 전체 삭제하는데 사용하는 메서드.
    public void removeItems(ArrayList<com.example.everylive.livestream.Activity_Chat_Items> items) {
        items.clear(); // 아이템 전부 삭제
        notifyDataSetChanged(); // 데이터 갱신
    }

    // 전체 데이터 갯수 리턴하는 오버라이드
    @Override
    public int getItemCount() {
//            return mData.size() ;
        return items.size();
    }

    // 아이템 뒤죽박죽 섞이는거 방지되는 오버라이드
    // 리소스를 아끼기 위해 뷰를 재사용하는데, 이것이 데이터 꼬임 현상을 만든다
    // 주로 아이템 구조나 디자인이나 값에서 변화가 일어나면 스크롤했을때 문제가 발생한다.
    // 해결방법은 뷰홀더 패턴을 적용하는 것이라 한다. 그래도 안되면 이 오버라이드 넣는다.
    @Override
    public int getItemViewType(int position) {
        return position;
    }

}