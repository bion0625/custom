package com.self_true.model.dto;

import com.self_true.model.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

@Schema(description = "회원가입 요청 DTO")
@Data
public class MembersRequest {
    private String id;
    private String password;
    private String name;
    private String email;
    private String phoneNumber;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(password);
        return Member.builder()
                .memberId(id).password(encodedPassword)
                .name(name).email(email).phoneNumber(phoneNumber)
                .build();
    }
}
