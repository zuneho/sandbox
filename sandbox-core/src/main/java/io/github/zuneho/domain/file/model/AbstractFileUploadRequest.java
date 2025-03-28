package io.github.zuneho.domain.file.model;

import io.github.zuneho.domain.file.type.FolderType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Getter
@Slf4j
public abstract class AbstractFileUploadRequest {
    private static final String FILE_EXTENSION_ERROR = "지원하지 않는 형식의 파일입니다. (%s) 형식의 파일만 업로드 가능합니다.";
    private static final String NOT_FOUND_FILE_TYPE_ERROR = "올바른 형태의 요청이 아닙니다.";
    private static final String FILE_REQUIRED_ERROR = "첨부된 파일을 확인할 수 없습니다.";
    private static final String FILE_COUNT_EXCEEDED_ERROR = "최대 %s개의 파일을 첨부할 수 있습니다.";
    private static final String FILE_SIZE_EXCEEDED_ERROR = "파일 크기 초과입니다. 최대 %s의 파일을 첨부할 수 있습니다.";



    @Setter
    protected MultipartFile[] files;
    @Setter
    private String userIdx;
    @Setter
    private Long adminIdx;
    private Long parentIdx;
    private String customPath;

    public abstract FolderType getFolderType();

    public abstract boolean isFileRequired();

    public abstract int getMaxFileCount();

    public boolean isEmptyFiles() {
        return ArrayUtils.isEmpty(this.files);
    }

    public boolean passUploadProcess() {
        return !isFileRequired() && isEmptyFiles();
    }

    public void addParentIdx(Long parentIdx) {
        this.parentIdx = parentIdx;
    }

    public void addCustomPath(String customPath) {
        this.customPath = customPath;
    }

    public String validateAndGetError() {
        FolderType folderType = getFolderType();

        if (folderType == null) {
            return NOT_FOUND_FILE_TYPE_ERROR;
        }

        if (isFileRequired() && isEmptyFiles()) {
            return FILE_REQUIRED_ERROR;
        }

        if (files != null && files.length > getMaxFileCount()) {
            return String.format(FILE_COUNT_EXCEEDED_ERROR, getMaxFileCount());
        }

        if (files != null && Arrays.stream(files)
                .anyMatch(file -> file.getSize() > folderType.getMaxUploadSize() * 1024)) {// KB -> bytes 변환
            return String.format(FILE_SIZE_EXCEEDED_ERROR, folderType.getMaxUploadSizeText());
        }

        if (files != null && Arrays.stream(files).anyMatch(file -> !folderType.isFileAllowed(file))) {
            return String.format(FILE_EXTENSION_ERROR, folderType.getSupportExtensions());
        }

        if (folderType.useCustomPath() && StringUtils.isEmpty(this.customPath)) {
            log.error("code error! file upload Request not found customPath!. class={}, FileFolderType = {}", this.getClass().getSimpleName(), folderType);
            throw new IllegalArgumentException(NOT_FOUND_FILE_TYPE_ERROR);
        }
        return null;
    }
}
