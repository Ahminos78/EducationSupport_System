package com.whut.common.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Order implements Serializable {

    private Long id;
    private Long userId;
    private String productName;
    private BigDecimal price;
    private User user;

    public Order() {
    }

    public Order(Long id, Long userId, String productName, BigDecimal price) {
        this.id = id;
        this.userId = userId;
        this.productName = productName;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
