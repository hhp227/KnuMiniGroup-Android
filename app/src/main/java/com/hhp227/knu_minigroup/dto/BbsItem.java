package com.hhp227.knu_minigroup.dto;

public class BbsItem {
    private String Type, Title, Url, Writer, Date;

    public BbsItem() {
    }

    public BbsItem(String type, String title, String url, String writer, String date) {
        this.Type = type;
        this.Title = title;
        this.Url = url;
        this.Writer = writer;
        this.Date = date;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getWriter() {
        return Writer;
    }

    public void setWriter(String writer) {
        Writer = writer;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
