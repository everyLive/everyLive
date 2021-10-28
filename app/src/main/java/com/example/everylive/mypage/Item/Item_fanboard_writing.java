package com.example.everylive.mypage.Item;

import android.content.Context;

import java.util.ArrayList;

public class Item_fanboard_writing {
    String idx_fanboard_writing; // 공지글 idx
    String idx_writer;
    String writer_nickName; // 공지글 쓴 사람 닉네임
    String writer_imgUrl; // 공지글 쓴 사람 이미지 url
    String writeDate;
    String textContents;
    String idx_comment_writer; // 댓글 다는 사람 idx
    String comment_imgUrl; // 댓글 다는 사람 프로필 이미지

    ArrayList<Item_notice_Comment> commentArrayList;

    public Item_fanboard_writing(){
        commentArrayList = new ArrayList<>();
    }

    public void addCommentArrayList(Item_notice_Comment item_notice_comment){
        commentArrayList.add(item_notice_comment); // 최신순으로 넣기.
    }

    public String getIdx_fanboard_writing() {
        return idx_fanboard_writing;
    }

    public void setIdx_fanboard_writing(String idx_fanboard_writing) {
        this.idx_fanboard_writing = idx_fanboard_writing;
    }

    public String getIdx_writer() {
        return idx_writer;
    }

    public void setIdx_writer(String idx_writer) {
        this.idx_writer = idx_writer;
    }

    public String getWriter_nickName() {
        return writer_nickName;
    }

    public void setWriter_nickName(String writer_nickName) {
        this.writer_nickName = writer_nickName;
    }

    public String getWriter_imgUrl() {
        return writer_imgUrl;
    }

    public void setWriter_imgUrl(String writer_imgUrl) {
        this.writer_imgUrl = writer_imgUrl;
    }

    public String getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(String writeDate) {
        this.writeDate = writeDate;
    }

    public String getTextContents() {
        return textContents;
    }

    public void setTextContents(String textContents) {
        this.textContents = textContents;
    }

    public String getIdx_comment_writer() {
        return idx_comment_writer;
    }

    public void setIdx_comment_writer(String idx_comment_writer) {
        this.idx_comment_writer = idx_comment_writer;
    }

    public String getComment_imgUrl() {
        return comment_imgUrl;
    }

    public void setComment_imgUrl(String comment_imgUrl) {
        this.comment_imgUrl = comment_imgUrl;
    }

    public ArrayList<Item_notice_Comment> getCommentArrayList() {
        return commentArrayList;
    }

    public void setCommentArrayList(ArrayList<Item_notice_Comment> commentArrayList) {
        this.commentArrayList = commentArrayList;
    }
}
