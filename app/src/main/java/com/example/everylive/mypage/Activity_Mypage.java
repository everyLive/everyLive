
package com.example.everylive.mypage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.home.Activity_Search;
import com.example.everylive.home.AdatperForSearch;
import com.example.everylive.home.ItemForSearch;
import com.example.everylive.mypage.Request.RequestGetUseInfo;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Mypage extends AppCompatActivity {

    String page_owner; // 마이페이지 소유자 idx_user
    String viewer_idx; // 페이지를 보는 사람 idx

    ImageView btn_back;
    TextView nickname, btn_edit_userInfo, cnt_fan, cnt_star;
    CircleImageView userProfile;

    TextView profileMSG1, profileMSG2; // 1: 작은박스, 2: 큰박스
    CheckBox checkbox_arrow;

    ViewPager2 viewPager2;
    ViewpagerAdapter viewpagerAdapter;
    TabLayout tabLayout;
    ArrayList<Data_Type> mdata; // 데이터모델 수정필요
    Context context;
    CheckBox btn_follow;
    TextView btn_follow_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mypage);

        btn_back = findViewById(R.id.btn_back);
        nickname = findViewById(R.id.nickname);
        btn_edit_userInfo = findViewById(R.id.btn_edit_userInfo);
        cnt_fan = findViewById(R.id.cnt_fan);
        cnt_star = findViewById(R.id.cnt_star);
        userProfile = findViewById(R.id.userProfileIMG);
        profileMSG1 = findViewById(R.id.profileMSG1);
        profileMSG2 = findViewById(R.id.profileMSG2);
        checkbox_arrow = findViewById(R.id.checkbox_arrow);
        btn_follow = findViewById(R.id.btn_follow);
        btn_follow_text = findViewById(R.id.btn_follow_text);

        // 페이지 주인의 idx를 저장해둔다. onStart로 다시 데이터 가져올 때 필요하기 때문.
        Intent intent = getIntent();
        page_owner = intent.getStringExtra("page_owner");
        
        // 페이지를 보고 있는 사람이 누군지, 로그인 쉐어드에서 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        viewer_idx = sharedPreferences.getString("idx_user",null);

        // 보고 있는 사람이 페이지 주인이 아니면, 정보 수정 불가 + (팬 등록 / 팬 끊기) 버튼 없음.
        if(page_owner.equals(viewer_idx)){ // 내 페이지
            btn_edit_userInfo.setVisibility(View.VISIBLE); // 편집 버튼 보이기.
            btn_follow.setVisibility(View.INVISIBLE); // 팬 등록 버튼 없애기.
            btn_follow_text.setVisibility(View.INVISIBLE); // 팬 등록 문구 없애기.
        }else{
            btn_edit_userInfo.setVisibility(View.INVISIBLE);
            btn_follow.setVisibility(View.VISIBLE);
            btn_follow_text.setVisibility(View.VISIBLE);
        }

        /**
         * @author : 김수미
         * @breif : 공지사항, 팬보드
         * */

        context = this;
        mdata = new ArrayList<>();
        mdata.add(new Data_Type(1));
        mdata.add(new Data_Type(2));

        viewPager2 = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabs);

        viewpagerAdapter = new ViewpagerAdapter(context, mdata, page_owner); // 뷰페이저 어댑터 생성
        viewPager2.setAdapter(viewpagerAdapter); // 뷰페이저에 어댑터 연결
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); // 스크롤 방향

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) { // 탭 레이아웃이랑 연결?
                switch (position){
                    case 0:
                        tab.setText("공지사항");
                        break;
                    case 1:
                        tab.setText("팬보드");
                        break;
                }
            }
        });

        tabLayoutMediator.attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() { // 뷰페이저 해당 포지션에 위치
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position){
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        });

        // 뒤로가기 버튼
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // userInfo 수정하는 화면으로 이동
        btn_edit_userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_Mypage.this, Activity_Mypage_Modify.class);
                startActivity(intent);
            }
        });

        // 소개글 : checkbox_arrow로 열고 닫고
        checkbox_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkbox_arrow.isChecked()){ // 누르면, 큰박스 보이기
                    profileMSG1.setVisibility(View.INVISIBLE);
                    profileMSG2.setVisibility(View.VISIBLE);
                }else{
                    profileMSG1.setVisibility(View.VISIBLE);
                    profileMSG2.setVisibility(View.INVISIBLE);
                }
            }
        });

        // 팬 되기, 팬 끊기.
        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestRelationFan.php";

                //파일 전송 요청 객체 생성[결과를 String으로 받음]
                SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;

                        try {
                            jsonObject = new JSONObject(response);
                            String stat = jsonObject.getString("stat");

                            if(stat.equals("STAT_RELEASE")){
                                Toast.makeText(Activity_Mypage.this,"팬 취소했습니다",Toast.LENGTH_SHORT).show();
                                btn_follow_text.setText("팬 등록");
                            }

                            if(stat.equals("STAT_SAVE_NOW")){
                                Toast.makeText(Activity_Mypage.this,"팬 추가했습니다",Toast.LENGTH_SHORT).show();
                                btn_follow_text.setText("팬 취소");
                            }

                            int cnt_fan_server = jsonObject.getInt("cnt_fan");
                            int cnt_star_server = jsonObject.getInt("cnt_star");

                            // page_owner를 팬 추가한 유저 수
                            cnt_fan.setText(String.valueOf(cnt_fan_server));
                            // page_owner가 추가한 스타 수
                            cnt_star.setText(String.valueOf(cnt_star_server));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast.makeText(Activity_Search.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });

                smpr.addStringParam("stat_fan", String.valueOf(btn_follow.isChecked())); // boolean
                smpr.addStringParam("viewer_idx", viewer_idx); // 누가
                smpr.addStringParam("page_owner", page_owner); // 누구를 팬 ?

                //요청객체를 서버로 보낼 우체통 같은 객체 생성
                RequestQueue requestQueue= Volley.newRequestQueue(Activity_Mypage.this);
                requestQueue.add(smpr);
            }
        });
    }

    // 액티비티가 가려졌다 다시 보였을 때, 정보 수정하고 온 것일 수도 있음.
    // userInfo 가져오는 부분 생명주기 고려해서 onStart()에 배치.
    @Override
    protected void onStart() {
        super.onStart();

        getUserInfo(page_owner);
    }

    public void getUserInfo(String idx_user){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){
                        String nickName_server = jsonObject.getString("nickName");
                        String userID_server = jsonObject.getString("userID");
                        int cnt_fan_server = jsonObject.getInt("cnt_fan");
                        int cnt_star_server = jsonObject.getInt("cnt_star");
                        String profileIMG_server = jsonObject.getString("profileIMG");
                        String introduce = jsonObject.getString("introduce");
                        Boolean fan_stat = jsonObject.getBoolean("fan_stat");

                        nickname.setText(nickName_server);
                        cnt_fan.setText(Integer.toString(cnt_fan_server));
                        cnt_star.setText(Integer.toString(cnt_star_server));

                        if(introduce.equals("null")){
                            profileMSG1.setText("프로필 메시지를 작성해주세요 :>");
                            profileMSG2.setText("프로필 메시지를 작성해주세요 :>");
                        }else{
                            profileMSG1.setText(introduce);
                            profileMSG2.setText(introduce);
                        }

                        if(fan_stat){
                            btn_follow.setChecked(true);
                            btn_follow_text.setText("팬 취소");
                        }else{
                            btn_follow.setChecked(false);
                            btn_follow_text.setText("팬 등록");
                        }

                        Glide.with(Activity_Mypage.this)
                                .load(profileIMG_server)
                                .override(400, 400)
                                .centerCrop()
                                .into(userProfile);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RequestGetUseInfo requestGetUseInfo = new RequestGetUseInfo(page_owner, viewer_idx, responseListener);
        RequestQueue queue = Volley.newRequestQueue(Activity_Mypage.this);
        queue.add(requestGetUseInfo);
    }
}
