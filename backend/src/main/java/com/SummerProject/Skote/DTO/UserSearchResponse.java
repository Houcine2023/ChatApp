package com.SummerProject.Skote.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse {
    private String id;
    private String username;
    private String email;
    private String image;
}
