package com.example.intelliview.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JSoupService {

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    private final S3Client s3Client;

    public ArrayList<String> getPinnedRepository(String username) throws IOException {
        ArrayList<String> pinnedRepository = new ArrayList<>();
        String url = "https://github.com/" + username;
        Document doc = Jsoup.parse(String.valueOf(Jsoup.connect(url).get()));
        Elements pinnedItems = doc.select("ol.js-pinned-items-reorder-list > li");
        for (Element item : pinnedItems) {
            Element link = item.selectFirst("a[href]");
            if (link != null) {
                String href = link.attr("href");
                pinnedRepository.add("https://github.com" + href);
            }
        }
        return pinnedRepository;
    }

    public void uploadToS3(String username, Long memberId) throws IOException {
        ArrayList<String> repositoryList = getPinnedRepository(username);
        for (String repository : repositoryList) {
            // Github ZIP 다운로드 링크 가져오기
            String zipUrl = getRepositoryZipUrl(repository);
            String repositoryName = repository.replaceFirst("https://github.com/", "");
            URI uri = URI.create(zipUrl);
            URL url = uri.toURL();

            // ZIP 다운로드
            Path zipPath = Files.createTempFile("repo-", ".zip");
            try (InputStream in = url.openStream()) {
                Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 압축 풀기
            Path unzipDir = Files.createTempDirectory("unzipped-repo");
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path newFilePath = unzipDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFilePath);
                    } else {
                        Files.createDirectories(newFilePath.getParent());
                        Files.copy(zis, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            //S3 업로드
            Files.walk(unzipDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String key = unzipDir.relativize(path).toString().replace("\\", "/"); // S3 key
                    s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key("repos/" + memberId + "/" + repositoryName + "/" + key)
                            .build(),
                        RequestBody.fromFile(path));
                });

            Files.deleteIfExists(zipPath);
        }


    }

    public String getRepositoryZipUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Element downloadLink = doc.selectFirst("a.prc-ActionList-ActionListContent-sg9-x.prc-Link-Link-85e08");
        if (downloadLink != null) {
            String href = downloadLink.attr("href");
            return "https://github.com" + href;
        } else {
            throw new IOException("Download ZIP 링크를 찾을 수 없습니다.");
        }
    }
}
