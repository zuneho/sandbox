package io.github.zuneho.domain.file.type;

import io.github.zuneho.domain.common.exception.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public enum FolderType {
    AGREEMENT("약관 동의", "/agreement", 2048, ChildFolderType.CUSTOM, List.of(AllowType.PDF), null),
    BANK_IMAGE("은행 아이콘", "/BankImg", 1024, ChildFolderType.BASE, List.of(AllowType.IMAGE_EXCLUDE_GIF), null),
    BANNER("APP 배너", "/banner", 2048, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    COMMERCE_BANNER("상점 배너", "/commerceBanner", 1024, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    EVENT("이벤트", "/event", 4096, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    FEED("피드", "/feed", 2048, ChildFolderType.DATE_TIME, List.of(AllowType.IMAGE), null),
    GIFT_BRAND("기프티콘", "/giftBrand", 2048, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    EXTERNAL_SERVICE("금복이", "/gumbok", 2048, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    NOTICE("공지사항", "/notice", 4096, ChildFolderType.BASE, List.of(AllowType.IMAGE), null),
    SMALL_BUSINESS("소상공인", "/small-business", 2048, ChildFolderType.DATE_TIME, List.of(AllowType.IMAGE_EXCLUDE_GIF), null),
    STAGE_FEED("스테이지 피드", "/stage-feed", 2048, ChildFolderType.DATE_TIME, List.of(AllowType.IMAGE_EXCLUDE_GIF), null),
    APP_MENU("APP 메뉴 아이콘", "/app-menu", 1024, ChildFolderType.BASE, List.of(AllowType.IMAGE_EXCLUDE_GIF), null),
    COMMON("공용", "/common", 2048, ChildFolderType.DATE_TIME, List.of(AllowType.IMAGE, AllowType.PDF), null),
    HWP("HWP 파일(CUSTOM 테스트용)", "/hwp", 4096, ChildFolderType.DATE_TIME, List.of(AllowType.CUSTOM), List.of("hwp")),
    TRIPS("아임인 여행", "/trips", 5120, ChildFolderType.DATE_TIME, List.of(AllowType.IMAGE), null);

    @Getter
    private final String desc;
    private final String rootFolder;
    @Getter
    private final long maxUploadSize; //MB
    private final ChildFolderType childFolderType;
    private final List<AllowType> allowTypes;
    private final List<String> allowCustomExtensions;

    private static final String BASE_UPLOAD_PATH = "upload";
    private static final String DATE_TIME_PATTERN = "/yyyy/MM/dd";
    private static final String UNSUPPORTED_TYPE_MESSAGE = "지원하지 않는 파일 형식입니다.";


    public boolean useCustomPath() {
        return this.childFolderType == ChildFolderType.CUSTOM;
    }

    public String getMaxUploadSizeText() {
        return (this.maxUploadSize / 1024) + " MB";
    }

    public boolean isCustomAllowType() {
        return this.allowTypes.contains(AllowType.CUSTOM);
    }

    public String getSupportExtensions() {
        List<String> fileExtensions = isCustomAllowType()
                ? this.allowCustomExtensions
                : this.allowTypes.stream()
                .flatMap(allowType -> allowType.getSupportedExtensions().stream())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(fileExtensions)) {
            log.warn("FileFolderType not found support extensions.this enum=[{}].", this.name());
            return "";
        }

        return String.join(",", fileExtensions);
    }

    public String getUploadPath() {
        if (useCustomPath()) {
            log.error("FileFolderType getUploadPath not supported type. type={}", this.name());
            throw new BusinessException(UNSUPPORTED_TYPE_MESSAGE);
        }

        StringBuilder uploadPath = new StringBuilder(BASE_UPLOAD_PATH)
                .append(this.rootFolder);

        if (this.childFolderType == ChildFolderType.DATE_TIME) {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            uploadPath.append(datePath);
        }

        return uploadPath.toString();
    }

    public String buildUploadPath(String customPath) {
        if (!useCustomPath()) {
            log.error("FileFolderType buildUploadPath not supported for type. type={}", this.name());
            throw new BusinessException(UNSUPPORTED_TYPE_MESSAGE);
        }

        if (StringUtils.isEmpty(customPath) || customPath.charAt(0) != '/') {
            log.error("FileFolderType buildUploadPath invalid customPath. customPath={}", customPath);
            throw new BusinessException(UNSUPPORTED_TYPE_MESSAGE);
        }

        return BASE_UPLOAD_PATH + this.rootFolder + customPath;
    }

    public boolean isFileAllowed(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }

        String filename = file.getOriginalFilename();
        String fileExtension = getFileExtension(file);

        if (isCustomAllowType()) {
            return fileExtension != null &&
                    this.allowCustomExtensions.stream()
                            .anyMatch(e -> StringUtils.equalsIgnoreCase(e, fileExtension));
        }

        String mediaTypeText = URLConnection.getFileNameMap().getContentTypeFor(filename);
        String fileContentType = file.getContentType() == null ? StringUtils.EMPTY : file.getContentType();
        if (StringUtils.isEmpty(mediaTypeText) || !fileContentType.contains(mediaTypeText)) {
            return false;
        }

        try {
            MediaType detectedMediaType = MediaType.parseMediaType(mediaTypeText);
            return this.allowTypes.stream()
                    .anyMatch(allowType -> allowType.mediaTypes.stream().anyMatch(detectedMediaType::equals));
        } catch (Exception e) {
            log.warn("FileFolderType isAllowedFileExtension encountered an error. mediaType={} message={}", mediaTypeText, e.getMessage(), e);
            return false;
        }
    }

    public static String getFileExtension(MultipartFile file) {
        if (file == null) {
            return null;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return null;
        }

        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0
                ? filename.substring(dotIndex + 1)
                : null;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ChildFolderType {
        BASE("기본 경로"),
        CUSTOM("직접 업로드"),
        DATE_TIME("날짜별 경로");

        private final String desc;
    }

    @Getter
    @RequiredArgsConstructor
    public enum AllowType {
        CUSTOM(List.of()),
        IMAGE(List.of(CustomMediaType.IMAGE_BMP, CustomMediaType.IMAGE_JPG, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG, MediaType.IMAGE_GIF)),
        IMAGE_EXCLUDE_GIF(List.of(CustomMediaType.IMAGE_BMP, CustomMediaType.IMAGE_JPG, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)),
        EXCEL(List.of(CustomMediaType.APPLICATION_EXCEL, CustomMediaType.APPLICATION_EXCEL_XLSX)),
        WORD(List.of(CustomMediaType.APPLICATION_MS_WORD, CustomMediaType.APPLICATION_MS_WORD_DOCX)),
        PDF(List.of(MediaType.APPLICATION_PDF));

        private final List<MediaType> mediaTypes;

        private static final Map<MediaType, String> mediaTypeToExtensionMap = new HashMap<>();

        static {
            mediaTypeToExtensionMap.put(MediaType.IMAGE_JPEG, "jpeg");
            mediaTypeToExtensionMap.put(MediaType.IMAGE_PNG, "png");
            mediaTypeToExtensionMap.put(MediaType.IMAGE_GIF, "gif");
            mediaTypeToExtensionMap.put(MediaType.APPLICATION_PDF, "pdf");
            mediaTypeToExtensionMap.put(CustomMediaType.IMAGE_BMP, "bmp");
            mediaTypeToExtensionMap.put(CustomMediaType.IMAGE_JPG, "jpg");
            mediaTypeToExtensionMap.put(CustomMediaType.APPLICATION_EXCEL, "xls");
            mediaTypeToExtensionMap.put(CustomMediaType.APPLICATION_EXCEL_XLSX, "xlsx");
            mediaTypeToExtensionMap.put(CustomMediaType.APPLICATION_MS_WORD, "doc");
            mediaTypeToExtensionMap.put(CustomMediaType.APPLICATION_MS_WORD_DOCX, "docx");
            // 필요한 다른 MediaType 과 확장자 매핑을 여기에 추가
        }


        public List<String> getSupportedExtensions() {
            return mediaTypes.stream()
                    .map(mediaTypeToExtensionMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private static class CustomMediaType {
        private static final MediaType IMAGE_BMP = MediaType.parseMediaType("image/bmp");
        private static final MediaType IMAGE_JPG = MediaType.parseMediaType("image/jpg");
        private static final MediaType APPLICATION_EXCEL = MediaType.parseMediaType("application/vnd.ms-excel");
        private static final MediaType APPLICATION_EXCEL_XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        private static final MediaType APPLICATION_MS_WORD = MediaType.parseMediaType("application/msword");
        private static final MediaType APPLICATION_MS_WORD_DOCX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
}
