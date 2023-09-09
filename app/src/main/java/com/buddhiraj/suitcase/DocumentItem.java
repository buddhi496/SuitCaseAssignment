package com.buddhiraj.suitcase;

public class DocumentItem {
    private final String imageUrl;
    private final String name;
    private final String price;
    private final String description;
    private final String storeName;

    public DocumentItem(String imageUrl, String name, String price, String description, String storeName) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeName = storeName;
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
}
