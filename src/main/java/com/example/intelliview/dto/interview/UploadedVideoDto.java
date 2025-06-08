package com.example.intelliview.dto.interview;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadedVideoDto {

    private final String key;
    private final String s3Url;
}
