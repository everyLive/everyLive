package com.example.everylive.home;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.everylive.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Activity_Home extends AppCompatActivity {

    private static final String TAG="사용자";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

    }


}
