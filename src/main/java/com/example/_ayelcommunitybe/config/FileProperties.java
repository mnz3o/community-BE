package com.example._ayelcommunitybe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// 파일 업로드 관련 설정값
@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileProperties {

    private String uploadDir;

    private List<String> allowedExtensions;
}