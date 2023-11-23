package com.stalex.weightapp;

public class WeightItem {
    private String date;
    private String weightValue;
    private int image;
    private String id;

    public WeightItem(String date, String weightValue, int image) {
        this.date = date;
        this.weightValue = weightValue;
        this.image = image;
    }

    public WeightItem(String date, String weightValue) {
        this.date = date;
        this.weightValue = weightValue;
    }

    public WeightItem() {
    }
    public void setDate(String date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeightValue(String weightValue) {
        this.weightValue = weightValue;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getWeightValue() {
        return weightValue;
    }

    public int getImage() {
        return image;
    }

}
