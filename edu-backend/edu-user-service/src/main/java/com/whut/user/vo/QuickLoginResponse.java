package com.whut.user.vo;

import lombok.Data;

@Data
public class QuickLoginResponse {
    private Long id;
    private String username;
    private String nickname;
    private Integer role;
}
