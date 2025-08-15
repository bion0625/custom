package com.project.base.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Table("app_user")
public class AppUser {

    @Id
    private Long id;

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("roles")
    private String roles;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;
}
