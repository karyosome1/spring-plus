package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserImageResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String imageUrl;

    public UserImageResponse(Long id, String email, String nickname, String imageUrl) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }
}
