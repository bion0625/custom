package com.self_true.model.dto.response;

import com.self_true.model.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class MytInfoResponse extends Response {
    private Long id;
    private String memberId;
    private String name;
    private Boolean isAdmin;

    public static MytInfoResponse fromEntity(Member member) {
        return new MytInfoResponse(member.getId(), member.getMemberId(), member.getName(), member.getRole().equalsIgnoreCase("ADMIN"));
    }
}
