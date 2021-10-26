package com.example.everylive.mypage.Item;

public class Item_notice_Comment {
    String idx_comment;
    String idx_writer;
    String userImgUrl;
    String userNickName;
    String writeDate;
    String commentContents;

    public Item_notice_Comment(String idx_comment, String idx_writer, String userImgUrl, String userNickName, String writeDate, String commentContents) {
        this.idx_comment = idx_comment;
        this.idx_writer = idx_writer;
        this.userImgUrl = userImgUrl;
        this.userNickName = userNickName;
        this.writeDate = writeDate;
        this.commentContents = commentContents;
    }

    public String getIdx_comment() {
        return idx_comment;
    }

    public void setIdx_comment(String idx_comment) {
        this.idx_comment = idx_comment;
    }

    public String getUserImgUrl() {
        return userImgUrl;
    }

    public void setUserImgUrl(String userImgUrl) {
        this.userImgUrl = userImgUrl;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(String writeDate) {
        this.writeDate = writeDate;
    }

    public String getCommentContents() {
        return commentContents;
    }

    public void setCommentContents(String commentContents) {
        this.commentContents = commentContents;
    }

    public String getIdx_writer() {
        return idx_writer;
    }

    public void setIdx_writer(String idx_writer) {
        this.idx_writer = idx_writer;
    }
}
