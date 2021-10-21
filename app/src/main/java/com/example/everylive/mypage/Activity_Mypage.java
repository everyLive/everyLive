package com.example.everylive.mypage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.everylive.R;
import com.example.everylive.mypage.Request.RequestGetUseInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Mypage extends AppCompatActivity {

    ImageView btn_back;
    TextView nickname, btn_edit_userInfo, cnt_fan, cnt_star;
    CircleImageView userProfile;

    TextView profileMSG1, profileMSG2; // 1: 작은박스, 2: 큰박스
    CheckBox checkbox_arrow;

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

        // checkbox_arrow로 열고 닫고
        checkbox_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkbox_arrow.isChecked()){ // 누르면, 큰박스 보이기
                    profileMSG1.setVisibility(View.INVISIBLE);
                    profileMSG2.setVisibility(View.VISIBLE);
                }else{
                    profileMSG1.setVisibility(View.VISIBLE);
                    profileMSG2.setVisibility(View.GONE);
                }
            }
        });
    }

    // 액티비티가 가려졌다 다시 보였을 때, 정보 수정하고 온 것일 수도 있음.
    // userInfo 가져오는 부분 생명주기 고려해서 onStart()에 배치.
    @Override
    protected void onStart() {
        super.onStart();

        // 쉐어드에서 idx_user 값을 가져와서 DB에서 정보 가져오기.
        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", MODE_PRIVATE);
        String idx_user = sharedPreferences.getString("idx_user",null);
        getUserInfo(idx_user);
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

                        nickname.setText(nickName_server);
                        cnt_fan.setText(Integer.toString(cnt_fan_server));
                        cnt_star.setText(Integer.toString(cnt_star_server));

                        profileMSG1.setText(introduce);
                        profileMSG2.setText(introduce);

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

        RequestGetUseInfo requestGetUseInfo = new RequestGetUseInfo(idx_user, responseListener);
        RequestQueue queue = Volley.newRequestQueue(Activity_Mypage.this);
        queue.add(requestGetUseInfo);
    }
}
