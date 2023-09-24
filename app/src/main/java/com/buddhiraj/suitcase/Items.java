package com.buddhiraj.suitcase;

public class Items {
    private final String imageUrl;
    private final String name;
    private final String price;
    private final String description;
    private final String storeName;
    private String userId;
    private boolean status;

    public Items(String imageUrl, String name, String price, String description, String storeName) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeName = storeName;
        this.status = false;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    public String getName() {
        return name;
    }



    public String getPrice() {
        return price;
    }


    public String getDescription() {
        return description;
    }


    public String getStoreName() {
        return storeName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}