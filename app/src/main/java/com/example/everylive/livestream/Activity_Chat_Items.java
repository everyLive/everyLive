package com.example.everylive.livestream;

public class Activity_Chat_Items { // 데이터 담을 객체생성

    private String nickName;
    private String content;
    private String profileIMGurl;
    private String whois;

    public String getWhois() {
        return whois;
    }

    public void setWhois(String whois) {
        this.whois = whois;
    }




    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProfileIMGurl() {
        return profileIMGurl;
    }

    public void setProfileIMGurl(String profileIMGurl) {
        this.profileIMGurl = profileIMGurl;
    }








    // 생성자.
    // 메인액티비티에서 값을 전달하면 이곳에서 받아서 선언된 전역변수에 리터럴 해준다.(초기화 한다고도 함)
    public Activity_Chat_Items(String whois, String profileIMGurl, String nickName,
                               String content) {

        this.whois = whois;
        this.profileIMGurl = profileIMGurl;
        this.nickName = nickName;
        this.content = content;

    }

}