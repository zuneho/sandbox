package io.github.zuneho.domain.file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@Setter
public class SimpleBase64FileUploadRequest extends SimpleFileUploadRequest {
    public List<Base64File> base64FileList;

    @Override
    public MultipartFile[] getFiles() {
        if (super.files == null && CollectionUtils.isNotEmpty(this.base64FileList)) {
            super.files = base64FileList.stream()
                    .map(Base64File::toMultipartFile)
                    .toArray(Base64ToMultipartFile[]::new);
        }
        return super.getFiles();
    }

    @Override
    public String validateAndGetError() {
        this.getFiles();
        return super.validateAndGetError();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Base64File {
        private String base64;  // Base64 인코딩된 파일 데이터
        private String fileName;  // 파일 이름
        private String contentType;  // MIME 타입

        public Base64ToMultipartFile toMultipartFile() {
            return new Base64ToMultipartFile(this.base64, this.fileName, this.contentType);
        }
    }

    public static class Base64ToMultipartFile implements MultipartFile {
        private final byte[] fileContent;
        private final String fileName;
        private final String contentType;

        public Base64ToMultipartFile(String base64, String fileName, String contentType) {
            // 줄바꿈이나 공백을 제거하고 디코딩
            String cleanedBase64 = base64.replaceAll("\\s+", "");
            this.fileContent = Base64.getDecoder().decode(cleanedBase64);
            this.fileName = fileName;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return this.fileName;
        }

        @Override
        public String getOriginalFilename() {
            return this.fileName;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return this.fileContent.length == 0;
        }

        @Override
        public long getSize() {
            return this.fileContent.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return this.fileContent;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(fileContent);
        }

        @Override
        public void transferTo(java.io.File dest) throws IllegalStateException {
            throw new UnsupportedOperationException("This operation is not supported.");
        }
    }
}
