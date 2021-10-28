package com.example.everylive.mypage.setting;

public class Item_live {
    String startDate_origin;
    String startDate;
    String totalTime;
    String totalCoin;

    String minute;

    public Item_live(String startDate_origin, String startDate, String totalTime, String totalCoin, String minute) {
        this.startDate_origin = startDate_origin;
        this.startDate = startDate;
        this.totalTime = totalTime;
        this.totalCoin = totalCoin;
        this.minute = minute;
    }

    public String getStartDate_origin() {
        return startDate_origin;
    }

    public void setStartDate_origin(String startDate_origin) {
        this.startDate_origin = startDate_origin;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalCoin() {
        return totalCoin;
    }

    public void setTotalCoin(String totalCoin) {
        this.totalCoin = totalCoin;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }
}
