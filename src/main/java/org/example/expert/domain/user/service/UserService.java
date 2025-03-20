package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.client.S3Service;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserImageResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    @Transactional
    public UserImageResponse updateImage(AuthUser authUser, MultipartFile image) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new InvalidRequestException("유저를 찾을 수 없습니다"));

        String imageUrl = user.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                s3Service.delete(getImage(imageUrl));
            } catch (Exception e) {
                log.warn("기존 이미지 삭제 실패: {}", e.getMessage());
            }
        }

        String uploadUrl = s3Service.upload(image);
        user.updateImage(uploadUrl);

        return new UserImageResponse(user.getId(), user.getEmail(), user.getNickname(), user.getImageUrl());
    }

    private String getImage(String profileUrl) {
        if (profileUrl == null || profileUrl.trim().isEmpty()) {
            throw new InvalidRequestException("잘못된 프로필 URL입니다.");
        }

        try {
            String decodedUrl = URLDecoder.decode(profileUrl, StandardCharsets.UTF_8);
            return decodedUrl.substring(decodedUrl.lastIndexOf("/") + 1);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("잘못된 URL 형식입니다: " + profileUrl, e);
        } catch (Exception e) {
            throw new ServerException("URL 디코딩 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
