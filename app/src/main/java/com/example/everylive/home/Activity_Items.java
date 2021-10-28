package com.example.everylive.home;

public class Activity_Items { // 데이터 담을 객체생성

    private String roomIMG;
    private String gender;
    private String roomcategory;
    private String roomtitle;
    private String roomcontents;
    private String nickName;
    private String participants;
    private String idx_user;


    public String getIdx_user() {
        return idx_user;
    }

    public void setIdx_user(String idx_user) {
        this.idx_user = idx_user;
    }

    public String getRoomIMG() {
        return roomIMG;
    }

    public void setRoomIMG(String roomIMG) {
        this.roomIMG = roomIMG;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRoomcategory() {
        return roomcategory;
    }

    public void setRoomcategory(String roomcategory) {
        this.roomcategory = roomcategory;
    }

    public String getRoomtitle() {
        return roomtitle;
    }

    public void setRoomtitle(String roomtitle) {
        this.roomtitle = roomtitle;
    }

    public String getRoomcontents() {
        return roomcontents;
    }

    public void setRoomcontents(String roomcontents) {
        this.roomcontents = roomcontents;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }


    // 생성자.
    // 메인액티비티에서 값을 전달하면 이곳에서 받아서 선언된 전역변수에 리터럴 해준다.(초기화 한다고도 함)
    public Activity_Items(String idx_user, String nickName, String roomIMG, String gender, String roomcategory,
                          String roomtitle, String roomcontents, String participants) {

        this.idx_user = idx_user;
        this.nickName = nickName;
        this.roomIMG = roomIMG;
        this.gender = gender;
        this.roomcategory = roomcategory;
        this.roomtitle = roomtitle;
        this.roomcontents = roomcontents;
        this.participants = participants;


    }

}