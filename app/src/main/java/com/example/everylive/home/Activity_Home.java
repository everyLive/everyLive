package com.example.everylive.home;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;
import com.example.everylive.mypage.Activity_My;

public class Activity_Home extends AppCompatActivity {

    ImageView btn_search;
    ImageView btn_mypage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_mypage = findViewById(R.id.btn_mypage);
        btn_search = findViewById(R.id.btn_search);

        // 버튼 클릭시, Activity_My로 이동.
        btn_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_Home.this, Activity_My.class);
                startActivity(intent);
            }
        });

        // 버튼 클릭시, Activity_Search로 이동.
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_Home.this, Activity_Search.class);
                startActivity(intent);
            }
        });
    }

}
