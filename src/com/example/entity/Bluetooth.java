package com.example.entity;

public class Bluetooth {
    private int image;
    private String name;
    private String addr;

    public Bluetooth(int image, String name, String addr) {
        super();
        this.image = image;
        this.name = name;
        this.addr = addr;
    }

    public Bluetooth() {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

}
