package com.example.everylive.home;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemForSearch {
    String userIdx;
    String imgUrl;
    String nickname;

    public ItemForSearch(String userIdx, String imgUrl, String nickname) {
        this.userIdx = userIdx;
        this.imgUrl = imgUrl;
        this.nickname = nickname;
    }

    public String getUserIdx() {
        return userIdx;
    }

    public void setUserIdx(String userIdx) {
        this.userIdx = userIdx;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}

