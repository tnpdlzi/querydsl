package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // getter, setter, requiredArgsConstructor, ToString, EqualsAndHashCode 넣어줌
@NoArgsConstructor
public class MemberDto {
    private String username;
    private int age;


    // DTO도 Q파일로 생성해줌.
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
