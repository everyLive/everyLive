package com.example.everylive.mypage.Request;


import com.android.volley.Response;
import com.android.volley.request.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestGetUseInfo extends StringRequest {

    final static private String URL = "http://3.36.159.193/everyLive/mypage/RequestGetUserInfo.php";
    private Map<String, String> map;

    public RequestGetUseInfo(String page_owner, String viewer_idx, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("page_owner", page_owner);
        map.put("viewer_idx", viewer_idx);
    }

    protected Map<String, String> getParams() throws Error{
        return map;
    }
}
