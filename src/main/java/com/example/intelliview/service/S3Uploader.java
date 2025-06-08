package com.example.intelliview.service;

import com.example.intelliview.dto.interview.UploadedVideoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    @Value("${S3_VIDEO_BUCKET_NAME}")
    private String bucketName;

    private final S3Client s3ClientVideo;

    public UploadedVideoDto upload(MultipartFile file) {
        try {
            String key = "videos/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3ClientVideo.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String s3Uri = "s3://" + bucketName + "/" + key;
            return new UploadedVideoDto(key, s3Uri);
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }
}