package com.bss.arrahmanlyrics.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by immoh on 20/12/2017.
 */

public class albums {

    int album_id;
    String album_name;

    String hero;
    String heroin;

    int year;
    String image_link;
    List<song> songlist = new ArrayList<>();

    public albums(int album_id, String album_name, String hero, String heroin, int year, String image_link) {
        this.album_id = album_id;
        this.album_name = album_name;

        this.hero = hero;
        this.heroin = heroin;

        this.year = year;
        this.image_link = image_link;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }


    public String getHero() {
        return hero;
    }

    public void setHero(String hero) {
        this.hero = hero;
    }

    public String getHeroin() {
        return heroin;
    }

    public void setHeroin(String heroin) {
        this.heroin = heroin;
    }



    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public List<song> getSonglist() {
        return songlist;
    }

    public void setSonglist(song song) {
        this.songlist.add(song);
    }

    public void setList(List<song> a){
        songlist = a;
    }
}
