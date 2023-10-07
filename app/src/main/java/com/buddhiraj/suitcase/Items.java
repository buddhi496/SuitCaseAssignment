package com.buddhiraj.suitcase;

public class Items {
    private final String imageUrl;
    private final String name;
    private final String price;
    private final String description;
    private final String storeName;
    private String userId;
    private boolean status;
    private String itemKey;
    private String category; // Add category property

    public Items(String imageUrl, String name, String price, String description, String storeName) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeName = storeName;
        this.userId = ""; // Initialize userId as an empty string
        this.status = false;
        this.itemKey = "";
        this.category = ""; // Initialize category as an empty string
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

    public String getUserId() {
        return userId;
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

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
