package io.github.zuneho.domain.file;

import io.github.zuneho.domain.common.exception.BusinessException;
import io.github.zuneho.domain.file.model.AbstractFileUploadRequest;
import io.github.zuneho.domain.file.model.FileUploadResultDto;
import io.github.zuneho.domain.file.type.FolderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileManagerService {
    private static final String FILE_UPLOAD_FAIL_ERROR = "파일 업로드를 실패하였습니다. 잠시 후 다시 시도해 주세요.";

    private static final int S3_FILE_UPLOAD_PARALLELISM = 2;

    //@Value("${cloud.aws.s3.bucket.url}")
    private String bucketUrl;

    //@Value("${cloud.aws.s3.bucket.name}")
    private String bucketName;


    public FileUploadResultDto uploadOnlyFile(AbstractFileUploadRequest request) {
        return uploadOnlyFile(request, 0L);
    }

    private FileUploadResultDto uploadOnlyFile(AbstractFileUploadRequest request, long startAttachSeq) {
        String errorMessage = request.validateAndGetError();
        if (errorMessage != null) {
            throw new BusinessException(errorMessage);
        }
        if (request.passUploadProcess()) {
            return FileUploadResultDto.buildEmpty();
        }

        FolderType folderType = request.getFolderType();
        String customPath = request.getCustomPath();
        AtomicLong sort = new AtomicLong(startAttachSeq);
        List<FileUploadResultDto.FileS3UploadResult> s3UploadResults;
        ForkJoinPool pool = new ForkJoinPool(S3_FILE_UPLOAD_PARALLELISM);
        try {
            s3UploadResults = pool
                    .submit(() ->
                            Arrays.stream(request.getFiles())
                                    .parallel()
                                    .map(file -> uploadS3(folderType, file, sort.getAndIncrement(), customPath))
                                    .sorted(Comparator.comparing(FileUploadResultDto.FileS3UploadResult::getAttachSeq))
                                    .collect(Collectors.toList())
                    ).get();
        } catch (Exception e) {
            log.error("uploadS3 error. message={}", e.getMessage(), e);
            throw new BusinessException(FILE_UPLOAD_FAIL_ERROR);
        } finally {
            pool.shutdown();
        }
        return FileUploadResultDto.of(folderType, s3UploadResults, null);
    }

    private FileUploadResultDto.FileS3UploadResult uploadS3(FolderType folderType, MultipartFile file, long attachSeq, String customPath) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = FolderType.getFileExtension(file);

//        String uploadFileName = originalFileName.substring(0, originalFileName.lastIndexOf("."))
//                + "_"
//                + ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
//                + "."
//                + fileExtension;

        //APP-API 의 파일 이름 생성 정책 APP-API FileService getFileUUID()
        String uploadFileName = UUID.randomUUID().toString().replace("-", "")
                + "."
                + fileExtension;

        String uploadPath = folderType.useCustomPath()
                ? folderType.buildUploadPath(customPath)
                : folderType.getUploadPath();

        String uploadFullPath = uploadPath + "/" + uploadFileName;
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(file.getContentType());
//        metadata.setContentLength(file.getSize());
//
//        try {
//            PutObjectRequest s3UploadRequest = new PutObjectRequest(bucketName, uploadFullPath, file.getInputStream(), metadata)
//                    .withCannedAcl(CannedAccessControlList.PublicRead);
//
//            awsS3.putObject(s3UploadRequest);
//        } catch (Exception e) {
//            log.error("(filename={}) s3 file upload fail. message={}", originalFileName, e.getMessage());
//            throw new BusinessException(FILE_UPLOAD_FAIL_ERROR);
//        }

        return FileUploadResultDto.FileS3UploadResult.builder()
                .originalFileName(originalFileName)
                .uploadedFileName(uploadFileName)
                .fileExtension(fileExtension)
                .uploadedPath(uploadPath)
                .uploadedUrl(bucketUrl + "/" + uploadFullPath)
                .fileSize(file.getSize())
                .attachSeq(attachSeq)
                .build();

    }

}
