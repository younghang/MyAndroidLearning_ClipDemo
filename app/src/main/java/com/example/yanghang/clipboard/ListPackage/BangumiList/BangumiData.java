package com.example.yanghang.clipboard.ListPackage.BangumiList;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by young on 2017/7/3.
 */

public class BangumiData implements Serializable {
    public static final String TAG = "bangumiDataTag";
    private String name;
    private List<String> classifications=new ArrayList<>();
    private int episodes;
    private Map<String, String> comments = new HashMap<String, String>();
    private String remark;
    private int grades;
    private String imageUrl;
    //是否观看了
    private Map<String, Boolean> episodesState = new HashMap<String, Boolean>();
    //用有序的序列保存集数名
    private List<String> episodeNames = new ArrayList<>();
    private Map<String, Boolean> episodeLikes = new HashMap<>();
    public int getEvaluateScore()
    {
        int watchedCount=0;
        for (String key : episodesState.keySet()) {
            if (episodesState.get(key)) {
                watchedCount++;
            }
        }
        int watchScore=(int) (100 * watchedCount * 1.0f / Math.max(episodes, episodesState.size()) + 0.5);
        return (int)((65*watchScore+35*grades)/100);
    }
    public String getProgress()
    {
        int watchedCount=0;
        for (String key : episodesState.keySet()) {
            if (episodesState.get(key)) {
                watchedCount++;
            }
        }
        return (episodes == 0 ? 0 : (int) (100 * watchedCount * 1.0f / Math.max(episodes, episodesState.size()) + 0.5)) + "%";
    }


    public List<String> getEpisodeNames() {
        return episodeNames;
    }

    public void setEpisodeNames(List<String> episodeNames) {
        this.episodeNames = episodeNames;
    }

    public Map<String, Boolean> getEpisodeLikes() {
        return episodeLikes;
    }

    public void setEpisodeLikes(Map<String, Boolean> episodeLikes) {
        this.episodeLikes = episodeLikes;
    }




    public Map<String, Boolean> getEpisodesState() {
        return episodesState;
    }

    public void setEpisodesState(Map<String, Boolean> episodesState) {
        this.episodesState = episodesState;
    }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BangumiData(String name, List<String> classifications, int episodes, Map<String, String> comments, String remark, int grades, String imageUrl, Map<String, Boolean> episodesState, List<String> episodeNames, Map<String, Boolean> episodeLikes) {
        this.name = name;
        this.classifications = classifications;
        this.episodes = episodes;
        this.comments = comments;
        this.remark = remark;
        this.grades = grades;
        this.imageUrl = imageUrl;
        this.episodesState = episodesState;
        this.episodeNames = episodeNames;
        this.episodeLikes = episodeLikes;
    }

    public BangumiData(String name, List<String> classifications, int episodes, Map<String, String> comments, String remark, int grades, String imageUrl, Map<String, Boolean> episodesState) {
        this.name = name;
        this.classifications = classifications;
        this.episodes = episodes;
        this.comments = comments;
        this.remark = remark;
        this.grades = grades;
        this.imageUrl = imageUrl;
        this.episodesState = episodesState;
    }

    public BangumiData() {
//        Log.d(TAG, "BangumiData: episodeStates=" + episodesState == null ? "null" : "not null");
//        Log.d(TAG, "BangumiData: comments=" + comments == null ? "null" : "not null");
//        Log.d(TAG, "BangumiData: classifications=" + classifications == null ? "null" : "not null");

    }

    public List<String> getClassifications() {
        return classifications;
    }

    public void setClassifications(List<String> classifications) {
        this.classifications = classifications;
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public Map<String, String> getComments() {
        return comments;
    }

    public void setComments(Map<String, String> comments) {
        this.comments = comments;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getGrades() {
        return grades;
    }

    public void setGrades(int grades) {
        this.grades = grades;
    }

    public BangumiData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
