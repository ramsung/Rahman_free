package com.bss.arrahmanlyrics.model;

/**
 * Created by mohan on 10/2/18.
 */

public class song {

    int song_id;
    String song_title;
    int album_id;
    String album_name;
    String download_link;
    String lyricist;
    String track_no;

    public song(int song_id, String song_title, int album_id,String album_name, String download_link, String lyricist, String track_no) {
        this.song_id = song_id;
        this.song_title = song_title;
        this.album_id = album_id;
        this.album_name = album_name;
        this.download_link = download_link;
        this.lyricist = lyricist;
        this.track_no = track_no;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public String getSong_title() {
        return song_title;
    }

    public void setSong_title(String song_title) {
        this.song_title = song_title;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getDownload_link() {
        return download_link;
    }

    public void setDownload_link(String download_link) {
        this.download_link = download_link;
    }

    public String getLyricist() {
        return lyricist;
    }

    public void setLyricist(String lyricist) {
        this.lyricist = lyricist;
    }

    public String getTrack_no() {
        return track_no;
    }

    public void setTrack_no(String track_no) {
        this.track_no = track_no;
    }
}
