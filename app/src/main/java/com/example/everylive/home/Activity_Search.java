package com.example.everylive.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.example.everylive.R;
import com.example.everylive.login.Activity_Register;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Activity_Search extends AppCompatActivity {

    ImageView btn_search;
    RecyclerView recycler_search;

    private AdatperForSearch adatperForSearch;
    private ArrayList<ItemForSearch> dataSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btn_search = findViewById(R.id.btn_search);
        recycler_search = findViewById(R.id.recycler_search);

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        recycler_search.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_search.setLayoutManager(layoutManager);

        dataSet = new ArrayList<>();

        String serverUrl = "http://3.36.159.193/everyLive/home/RequestGetAllUserData.php";

        //파일 전송 요청 객체 생성[결과를 String으로 받음]
        SimpleMultiPartRequest smpr= new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Boolean success = jsonObject.getBoolean("success");
                    if(success){
                        JSONArray jsonArray = jsonObject.getJSONArray("arrayList");

                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject json_userInfo = jsonArray.getJSONObject(i);
                            dataSet.add(new ItemForSearch(json_userInfo.getString("idx_user"),json_userInfo.getString("profileIMG"),json_userInfo.getString("nickName")));
                        }

                        adatperForSearch = new AdatperForSearch(dataSet, Activity_Search.this);
                        recycler_search.setAdapter(adatperForSearch);
                        recycler_search.setVisibility(View.INVISIBLE);
                    }
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

        //요청객체를 서버로 보낼 우체통 같은 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(smpr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 액션바에 Title 기본값 : 앱 이름 -> 없애기
        androidx.appcompat.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("");

        // 액션바에 메뉴 붙이기
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() > 0){
                    recycler_search.setVisibility(View.VISIBLE);
                    adatperForSearch.getFilter().filter(newText);
                }else{
                    recycler_search.setVisibility(View.INVISIBLE);
                }

                return false;
            }
        });
        return true;
    }
}
