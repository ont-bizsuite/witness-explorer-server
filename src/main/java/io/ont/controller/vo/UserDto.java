package io.ont.controller.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class UserDto {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}