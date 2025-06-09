package com.example.intelliview.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO {

    //private Long id;
    private String username;
    private String password;
    //private String role;
    private String email;
    private String githubUsername;
}
