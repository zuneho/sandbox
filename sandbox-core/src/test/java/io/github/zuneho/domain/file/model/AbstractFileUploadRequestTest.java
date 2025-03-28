package io.github.zuneho.domain.file.model;

import io.github.zuneho.domain.file.type.FolderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AbstractFileUploadRequestTest {

    private AbstractFileUploadRequest fileUploadRequest;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = Mockito.mock(MultipartFile.class);
        fileUploadRequest = new AbstractFileUploadRequest() {
            @Override
            public FolderType getFolderType() {
                return FolderType.APP_MENU; // 기본 FolderType 설정
            }

            @Override
            public boolean isFileRequired() {
                return true; // 기본 파일 필수 설정
            }

            @Override
            public int getMaxFileCount() {
                return 2; // 기본 최대 파일 개수 설정
            }

            @Override
            public String getUserIdx() {
                return "";
            }

            @Override
            public Long getAdminIdx() {
                return 0L;
            }
        };
    }

    @Test
    void testValidateAndGetError_NoFolderType() {
        fileUploadRequest = new AbstractFileUploadRequest() {
            @Override
            public FolderType getFolderType() {
                return null;
            }

            @Override
            public boolean isFileRequired() {
                return true;
            }

            @Override
            public int getMaxFileCount() {
                return 2;
            }

            @Override
            public String getUserIdx() {
                return "";
            }

            @Override
            public Long getAdminIdx() {
                return 0L;
            }
        };

        assertEquals("올바른 형태의 요청이 아닙니다.", fileUploadRequest.validateAndGetError());
    }

    @Test
    void testValidateAndGetError_FileRequiredButEmpty() {
        fileUploadRequest.setFiles(null);
        assertEquals("첨부된 파일을 확인할 수 없습니다.", fileUploadRequest.validateAndGetError());
    }

    @Test
    void testValidateAndGetError_FileCountExceeded() {
        MultipartFile[] files = new MultipartFile[3]; // 최대 파일 개수를 초과하는 파일 배열 생성
        fileUploadRequest.setFiles(files);

        assertEquals("최대 2개의 파일을 첨부할 수 있습니다.", fileUploadRequest.validateAndGetError());
    }

    @Test
    void testValidateAndGetError_FileSizeExceeded() {
        when(mockFile.getSize()).thenReturn(3L * 1024 * 1024); // 최대 크기를 초과하는 파일 크기 설정
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});

        assertEquals("파일 크기 초과입니다. 최대 1 MB의 파일을 첨부할 수 있습니다.", fileUploadRequest.validateAndGetError());
    }

    @Test
    void testValidateAndGetError_FileExtensionNotAllowed() {
        when(mockFile.getOriginalFilename()).thenReturn("test.gif"); // 허용되지 않는 파일 형식 설정
        when(mockFile.getContentType()).thenReturn("image/gif");
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});

        assertEquals("지원하지 않는 형식의 파일입니다. (bmp,jpg,jpeg,png) 형식의 파일만 업로드 가능합니다.", fileUploadRequest.validateAndGetError());
    }

    @Test
    void testValidateAndGetError_ValidRequest() {
        when(mockFile.getSize()).thenReturn(500L * 1024); // 유효한 파일 크기 설정
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});
        fileUploadRequest.addParentIdx(1L);

        assertNull(fileUploadRequest.validateAndGetError());
    }

    @Test
    void testIsEmptyFiles() {
        assertTrue(fileUploadRequest.isEmptyFiles()); // 파일이 없을 때 true 반환

        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});
        assertFalse(fileUploadRequest.isEmptyFiles()); // 파일이 있을 때 false 반환
    }

    @Test
    void testPassUploadProcess_FileNotRequiredAndEmpty() {
        fileUploadRequest = new AbstractFileUploadRequest() {
            @Override
            public FolderType getFolderType() {
                return FolderType.BANNER;
            }

            @Override
            public boolean isFileRequired() {
                return false; // 파일 필수 아님
            }

            @Override
            public int getMaxFileCount() {
                return 2;
            }

            @Override
            public String getUserIdx() {
                return "";
            }

            @Override
            public Long getAdminIdx() {
                return 0L;
            }
        };

        assertTrue(fileUploadRequest.passUploadProcess()); // 파일이 필수 아님 + 비어있음
    }

    @Test
    void testPassUploadProcess_FileRequiredOrNotEmpty() {
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});
        assertFalse(fileUploadRequest.passUploadProcess()); // 파일 필수 또는 비어있지 않음
    }


    @Test
    void testValidateAndGetError_CustomPathRequiredButEmpty() {
        fileUploadRequest = new AbstractFileUploadRequest() {
            @Override
            public FolderType getFolderType() {
                return FolderType.AGREEMENT; // 커스텀 경로가 필요한 FolderType 설정
            }

            @Override
            public boolean isFileRequired() {
                return true;
            }

            @Override
            public int getMaxFileCount() {
                return 2;
            }

            @Override
            public String getUserIdx() {
                return "";
            }

            @Override
            public Long getAdminIdx() {
                return 0L;
            }
        };

        when(mockFile.getOriginalFilename()).thenReturn("test.pdf"); // 허용되지 않는 파일 형식 설정
        when(mockFile.getContentType()).thenReturn("application/pdf");
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});
        RuntimeException exception = assertThrows(IllegalArgumentException.class, fileUploadRequest::validateAndGetError);
        assertEquals("올바른 형태의 요청이 아닙니다.", exception.getMessage());
    }

    @Test
    void testValidateAndGetError_CustomPathProvided() {
        fileUploadRequest = new AbstractFileUploadRequest() {
            @Override
            public FolderType getFolderType() {
                return FolderType.AGREEMENT; // 커스텀 경로가 필요한 FolderType 설정
            }

            @Override
            public boolean isFileRequired() {
                return true;
            }

            @Override
            public int getMaxFileCount() {
                return 2;
            }

            @Override
            public String getUserIdx() {
                return "";
            }

            @Override
            public Long getAdminIdx() {
                return 0L;
            }
        };
        when(mockFile.getSize()).thenReturn(500L * 1024); // 유효한 파일 크기 설정
        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});

        // 커스텀 경로 설정
        fileUploadRequest.addCustomPath("/custom/path");
        fileUploadRequest.setFiles(new MultipartFile[]{mockFile});
        String message = fileUploadRequest.validateAndGetError();
        assertNull(message);
    }
}