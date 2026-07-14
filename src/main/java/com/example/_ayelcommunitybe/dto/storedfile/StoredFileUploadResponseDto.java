package com.example._ayelcommunitybe.dto.storedfile;

public record StoredFileUploadResponseDto(
        String presignedUrl,
        String fileUrl
) {
}