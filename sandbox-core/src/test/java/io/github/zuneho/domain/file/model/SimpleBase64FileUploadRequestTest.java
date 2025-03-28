package io.github.zuneho.domain.file.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBase64FileUploadRequestTest {

    private SimpleBase64FileUploadRequest request;

    @BeforeEach
    public void setUp() {
        // 테스트에 필요한 Base64 인코딩된 파일 데이터를 준비합니다.
        List<SimpleBase64FileUploadRequest.Base64File> base64FileList = new ArrayList<>();
        base64FileList.add(new SimpleBase64FileUploadRequest.Base64File("U29tZSBkYXRh", "file1.jpg", "image/jpeg")); // "Some data"
        base64FileList.add(new SimpleBase64FileUploadRequest.Base64File("SGVsbG8gd29ybGQ=", "file2.jpeg", "image/jpeg")); // "Hello world"

        request = new SimpleBase64FileUploadRequest();
        request.setBase64FileList(base64FileList);
    }

    @Test
    public void testGetFilesShouldConvertBase64ToMultipartFile() {
        // Base64 인코딩된 데이터를 MultipartFile로 변환하는지 테스트
        MultipartFile[] files = request.getFiles();

        assertNotNull(files);
        assertEquals(2, files.length);

        MultipartFile file1 = files[0];
        assertEquals("file1.jpg", file1.getOriginalFilename());
        assertEquals("image/jpeg", file1.getContentType());
        assertEquals(9, file1.getSize()); // "Some data" 길이

        MultipartFile file2 = files[1];
        assertEquals("file2.jpeg", file2.getOriginalFilename());
        assertEquals("image/jpeg", file2.getContentType());
        assertEquals(11, file2.getSize()); // "Hello world" 길이
    }

    @Test
    public void testFileContent() throws IOException {
        // 변환된 파일의 내용이 올바른지 테스트
        MultipartFile[] files = request.getFiles();

        assertEquals("Some data", new String(files[0].getBytes()));
        assertEquals("Hello world", new String(files[1].getBytes()));
    }

    @Test
    public void testFileValidation() {
        // 파일 검증 로직이 제대로 동작하는지 테스트
        String validationError = request.validateAndGetError();
        assertNull(validationError); // 에러가 없어야 함
    }

    @Test
    public void testMaxFileCountExceeded() {
        // 파일 개수 초과 테스트
        List<SimpleBase64FileUploadRequest.Base64File> base64FileList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            base64FileList.add(new SimpleBase64FileUploadRequest.Base64File("U29tZSBkYXRh", "file" + i + ".txt", "text/plain"));
        }

        request.setBase64FileList(base64FileList);
        String validationError = request.validateAndGetError();

        assertNotNull(validationError);
        assertTrue(validationError.contains("최대 5개의 파일을 첨부할 수 있습니다."));
    }


    @Test
    public void testInvalidFileExtensionError() {
        // 잘못된 파일 확장자를 테스트하는 로직 추가 (예: PDF 파일이 허용되지 않음)
        List<SimpleBase64FileUploadRequest.Base64File> base64FileList = new ArrayList<>();
        base64FileList.add(new SimpleBase64FileUploadRequest.Base64File("U29tZSBkYXRh", "file1.txt", "application/txt"));
        request.setBase64FileList(base64FileList);

        // 폴더 타입에서 허용되지 않는 파일 확장자가 존재하는지 검증
        String validationError = request.validateAndGetError();

        assertNotNull(validationError);
        assertTrue(validationError.contains("지원하지 않는 형식의 파일입니다."));
    }
}