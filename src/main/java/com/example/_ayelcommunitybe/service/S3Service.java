package com.example._ayelcommunitybe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.cloudfront-url}")
    private String cloudFrontUrl;

    public String createProfileObjectKey(String originalFilename) {
        return "profiles/" + UUID.randomUUID() + "_" + originalFilename;
    }

    public String createPostObjectKey(String originalFilename) {
        return "posts/" + UUID.randomUUID() + "_" + originalFilename;
    }

    public String createFileUrl(String objectKey) {
        return cloudFrontUrl + "/" + objectKey;
    }

    public PresignedPutObjectRequest createPresignedPutObjectRequest(
            String objectKey,
            String contentType
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest);
    }
}