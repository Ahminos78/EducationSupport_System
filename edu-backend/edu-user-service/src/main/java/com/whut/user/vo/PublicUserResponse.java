package com.whut.user.vo;

public class PublicUserResponse {

    private Long id;
    private String nickname;
    private Integer role;

    public PublicUserResponse() {
    }

    public PublicUserResponse(Long id, String nickname, Integer role) {
        this.id = id;
        this.nickname = nickname;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
