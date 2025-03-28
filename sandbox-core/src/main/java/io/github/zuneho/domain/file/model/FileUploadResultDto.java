package io.github.zuneho.domain.file.model;


import io.github.zuneho.domain.file.type.FolderType;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class FileUploadResultDto {
    private final FolderType folderType;
    private final List<FileS3UploadResult> s3UploadResults;
    private Long attachmentIdx;

    public void updateAttachmentIdx(Long attachmentIdx) {
        this.attachmentIdx = attachmentIdx;
    }

    public static FileUploadResultDto buildEmpty() {
        return builder()
                .s3UploadResults(List.of())
                .build();
    }

    public static FileUploadResultDto of(FolderType folderType, List<FileS3UploadResult> s3UploadResults, Long attachmentIdx) {
        return FileUploadResultDto.builder()
                .folderType(folderType)
                .s3UploadResults(s3UploadResults)
                .attachmentIdx(attachmentIdx)
                .build();
    }

    @Getter
    @Builder
    public static class FileS3UploadResult {
        private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        private final String originalFileName;
        private final String uploadedFileName;
        private final String fileExtension;
        private final String uploadedPath;
        private final String uploadedUrl;
        private final long fileSize;
        private final long attachSeq;
    }
}
