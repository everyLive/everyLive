package com.example.everylive.mypage.Request;


import com.android.volley.Response;
import com.android.volley.request.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestGetUseInfo extends StringRequest {

    final static private String URL = "http://3.36.159.193/everyLive/mypage/RequestGetUserInfo.php";
    private Map<String, String> map;

    public RequestGetUseInfo(String idx_user, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("idx_user", idx_user);
    }

    protected Map<String, String> getParams() throws Error{
        return map;
    }
}
