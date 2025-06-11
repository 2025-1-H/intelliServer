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

import java.io.*;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    @Value("${S3_VIDEO_BUCKET_NAME}")
    private String bucketName;

    private final S3Client s3ClientVideo;

    public UploadedVideoDto upload(MultipartFile multipartFile) {
        try {
            File original = convertMultipartToFile(multipartFile);
            File toUpload = reencodeToVp9(original);

            String key = "videos/" + UUID.randomUUID() + ".webm";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("video/webm")
                    .build();

            s3ClientVideo.putObject(putObjectRequest, RequestBody.fromFile(toUpload));

            String s3Uri = "s3://" + bucketName + "/" + key;
            return new UploadedVideoDto(key, s3Uri);
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload-", ".webm");
        file.transferTo(convFile);
        return convFile;
    }

    private File reencodeToVp9(File inputFile) throws IOException, InterruptedException {
        File outputFile = File.createTempFile("converted-", ".webm");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "libvpx-vp9",
                "-c:a", "libopus",
                "-b:v", "1M",
                "-b:a", "128k",
                outputFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true); // stdout + stderr 통합
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ffmpeg] " + line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0 || !outputFile.exists()) {
            throw new RuntimeException("ffmpeg 변환 실패");
        }

        return outputFile;
    }
}
