package com.example.everylive.splash;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;
import com.example.everylive.home.Activity_Home;
import com.example.everylive.login.Activity_Login;

public class Activity_Splash extends AppCompatActivity {

    TextView splash_everyLive;



   @Override
   protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_splash);

           // 애니메이션 동작
       splash_everyLive = findViewById(R.id.splash_everyLive);
       Animation anim = AnimationUtils.loadAnimation(this,R.anim.splash);
       splash_everyLive.setAnimation(anim);

       // 3초뒤 화면이동
           Handler hd = new Handler();
           hd.postDelayed(new SplashHandler(),4000);



           } // 온크리에이트


    // 로컬DB에 idx_user이 존재하면 메인홈으로, 존재하지않으면 로그인화면으로 이동한다
    private class SplashHandler implements Runnable{

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                "userInfo", Context.MODE_PRIVATE);


       public void run(){

//           startActivity(new Intent(getApplication(), Activity_Login.class));
//           Activity_Splash.this.finish();

           if(sharedPref.contains("idx_user")){
               startActivity(new Intent(getApplication(), Activity_Home.class));
               Activity_Splash.this.finish();
           }else{
               startActivity(new Intent(getApplication(), Activity_Login.class));
               Activity_Splash.this.finish();
           }

       }
    }
}
