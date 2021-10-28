package com.example.everylive.game;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class roulette extends AppCompatActivity {

    RequestQueue requestQueue;

    TextView resultTv;
    ImageView wheel;

    private static final String[] sectors = { "32", "15",
            "19", "4", "21", "2", "25", "17", "34",
            "6", "27","13", "36", "11", "30", "8",
            "23", "10", "5", "24", "16", "33",
            "1", "20", "14", "31", "9", "22",
            "18", "29", "7", "28", "12", "35",
            "3", "26", "0"
    };

    // We create a Random instance to make our wheel spin randomly
    private static final Random RANDOM = new Random();
    private int degree = 0, degreeOld = 0;

    // We have 37 sectors on the wheel, we divide 360 by this value to have angle for each sector
    // we divide by 2 to have a half sector
    private static final float HALF_SECTOR = 360f / 37f / 2f;

    String idx_user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roulette);

        requestQueue = Volley.newRequestQueue(this);

        Button spinBtn = findViewById(R.id.spinBtn);
        resultTv = findViewById(R.id.resultTv);
        wheel = findViewById(R.id.wheel);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        idx_user = sharedPreferences.getString("idx_user",null);

        spinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkParti(idx_user);
            }
        });

    }

    public void checkParti(String idx_user){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestCheckParti.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("checkParti", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");

                    if(success){ // 오늘 게임 참여한적 없음
                        spin();
                    }else{ // 게임 참여 이미함.
                        int myColor = ContextCompat.getColor(getApplicationContext(), R.color.red);
                        resultTv.setText("내일 다시 참여하세요 ~!");
                        resultTv.setTextColor(myColor);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", "성별 변경");
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_user", idx_user); // 공지글 idx : idx_writing

        requestQueue.add(smpr);
    }

    public void spin() {
        degreeOld = degree % 360;
        // we calculate random angle for rotation of our wheel
        degree = RANDOM.nextInt(360) + 720;
        // rotation effect on the center of the wheel
        RotateAnimation rotateAnim = new RotateAnimation(degreeOld, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(3600);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // we empty the result text view when the animation start

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // we display the correct sector pointed by the triangle at the end of the rotate animation
                String coin = getSector(360 - (degree % 360));

                int myColor = ContextCompat.getColor(getApplicationContext(), R.color.design_default_color_primary_dark);
                resultTv.setText(coin+"코인을 얻었습니다 ~!");
                resultTv.setTextColor(myColor);

                saveCoin(coin); // 디비에 받은 코인 저장 + 게임 참여했다고 오늘 데이터 저장.
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // we start the animation
        wheel.startAnimation(rotateAnim);
    }

    private String getSector(int degrees) {
        int i = 0;
        String text = null;

        do {
            // start and end of each sector on the wheel
            float start = HALF_SECTOR * (i * 2 + 1);
            float end = HALF_SECTOR * (i * 2 + 3);

            if (degrees >= start && degrees < end) {
                // degrees is in [start;end[
                // so text is equals to sectors[i];
                text = sectors[i];
            }

            i++;
            // now we can test our Android Roulette Game :)
            // That's all !
            // In the second part, you will learn how to add some bets on the table to play to the Roulette Game :)
            // Subscribe and stay tuned !

        } while (text == null  &&  i < sectors.length);

        return text;
    }

    public void saveCoin(String coin){
        String serverUrl = "http://3.36.159.193/everyLive/mypage/RequestsaveCoin.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("saveCoin", response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        //요청 객체에 보낼 데이터를 추가
        smpr.addStringParam("idx_user", idx_user); // 공지글 idx : idx_writing
        smpr.addStringParam("coin", coin);

        requestQueue.add(smpr);
    }
}
