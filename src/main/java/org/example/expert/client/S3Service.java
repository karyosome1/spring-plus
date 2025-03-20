package org.example.expert.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Getter
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${s3.allowed-extensions}")
    private List<String> allowedExtensions;

    @PostConstruct
    private void normalizeAllowedExtensions() {
        allowedExtensions = allowedExtensions.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public String upload(MultipartFile file) {
        validateFile(file);
        String newFileName = generateNewFileName(file.getOriginalFilename());
        uploadToS3(file, newFileName);
        return generatePublicUrl(newFileName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("이미지는 필수 값입니다.");
        }

        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!allowedExtensions.contains(fileExtension)) {
            String allowedExtensionsString = String.join(",", allowedExtensions);
            throw new InvalidRequestException("허용되는 파일 확장자: " + allowedExtensionsString);
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InvalidRequestException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private String generateNewFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidRequestException("유효하지 않은 파일 이름입니다.");
        }
        return UUID.randomUUID() + "_" + originalFilename;
    }

    private void uploadToS3(MultipartFile file, String newFileName) {
        try {
            String contentType = Optional.ofNullable(file.getContentType())
                    .filter(ct -> ct.startsWith("image/"))
                    .orElseThrow(() -> new InvalidRequestException("이미지가 아닙니다."));

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new ServerException("S3 업로드 중 오류가 발생했습니다.", e);
        }
    }

    private String generatePublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }

    public void delete(String objectPath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectPath)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (NoSuchKeyException e) {
            throw new ServerException("파일을 찾을 수 없습니다: " + objectPath, e);
        } catch (AwsServiceException e) {
            throw new ServerException("파일 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String newFileName) {
        if (newFileName == null || !newFileName.contains(".") || newFileName.endsWith(".")) {
            throw new InvalidRequestException("유효하지 않은 파일 확장자입니다.");
        }
        return newFileName.substring(newFileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
