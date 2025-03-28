package io.github.zuneho.domain.file.type;

import io.github.zuneho.domain.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FolderTypeTest {

    @Test
    @DisplayName("FolderType - getMaxUploadSizeText Test")
    void testGetMaxUploadSizeText() {
        assertEquals("1 MB", FolderType.BANK_IMAGE.getMaxUploadSizeText());
        assertEquals("2 MB", FolderType.AGREEMENT.getMaxUploadSizeText());
        assertEquals("4 MB", FolderType.NOTICE.getMaxUploadSizeText());
    }

    @Test
    @DisplayName("FolderType - getSupportedExtensions Test")
    void testGetSupportExtensions() {
        assertEquals("pdf", FolderType.AGREEMENT.getSupportExtensions());
        assertEquals("bmp,jpg,jpeg,png", FolderType.APP_MENU.getSupportExtensions());
        assertEquals("hwp", FolderType.HWP.getSupportExtensions());
    }

    @Test
    @DisplayName("FolderType - getUploadPath Test")
    void testGetUploadPath() {
        assertEquals("upload/BankImg", FolderType.BANK_IMAGE.getUploadPath());
        assertEquals("upload/event", FolderType.EVENT.getUploadPath());

        // ChildFolderType.DATE_TIME 이 적용된 경우
        assertTrue(FolderType.COMMON.getUploadPath().matches("upload/common/\\d{4}/\\d{2}/\\d{2}"));

        // ChildFolderType.CUSTOM 인 경우 예외 발생 Test
        assertThrows(BusinessException.class, FolderType.AGREEMENT::getUploadPath);
    }

    @Test
    @DisplayName("FolderType - buildUploadPath Test")
    void testBuildUploadPath() {
        // 올바른 커스텀 경로
        String customPath = "/2024/custom/path";
        String expectedPath = "upload/agreement/2024/custom/path";
        assertEquals(expectedPath, FolderType.AGREEMENT.buildUploadPath(customPath));

        // 잘못된 커스텀 경로
        String invalidPath = "invalid/custom/path";
        assertThrows(BusinessException.class, () -> FolderType.HWP.buildUploadPath(invalidPath));

        // ChildFolderType.CUSTOM 이 아닌 경우 예외 발생
        assertThrows(BusinessException.class, () -> FolderType.HWP.buildUploadPath("/valid/path"));
    }

    @Test
    @DisplayName("FolderType - isAllowed Test")
    void testIsFileAllowed() {
        MockMultipartFile allowedFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile disallowedFile = new MockMultipartFile("file", "test.gif", "image/gif", new byte[0]);

        // GIF 는 허용되지 않는 파일 타입
        assertFalse(FolderType.APP_MENU.isFileAllowed(disallowedFile));

        // JPG 는 허용되는 파일 타입
        assertTrue(FolderType.APP_MENU.isFileAllowed(allowedFile));

        // CUSTOM 타입 허용 Test
        MockMultipartFile customFile = new MockMultipartFile("file", "test.hwp", "application/x-hwp", new byte[0]);
        assertTrue(FolderType.HWP.isFileAllowed(customFile));
    }

    @Test
    @DisplayName("FolderType - getFileExtension Test")
    void testGetFileExtension() {
        MockMultipartFile fileWithExtension = new MockMultipartFile("file", "test.jpg", null, new byte[0]);
        assertEquals("jpg", FolderType.getFileExtension(fileWithExtension));

        MockMultipartFile fileWithoutExtension = new MockMultipartFile("file", "testfile", null, new byte[0]);
        assertNull(FolderType.getFileExtension(fileWithoutExtension));
    }


    @Test
    @DisplayName("FolderType - isAllowed multi Test")
    void testIsFileAllowed_with_multi_extension() {
        MockMultipartFile pdfAllowedFile = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        MockMultipartFile imageAllowedFile = new MockMultipartFile("file", "test.gif", "image/gif", new byte[0]);

        // COMMON 은 PDF 를 허용
        assertTrue(FolderType.COMMON.isFileAllowed(pdfAllowedFile));

        // COMMON 은 IMAGE 를 허용
        assertTrue(FolderType.COMMON.isFileAllowed(imageAllowedFile));
    }
}