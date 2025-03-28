package io.github.zuneho.domain.file.model;

import io.github.zuneho.domain.file.type.FolderType;
import lombok.Setter;

@Setter
public class SimpleFileUploadRequest extends AbstractFileUploadRequest {

    @Override
    public FolderType getFolderType() {
        return FolderType.COMMON;
    }

    @Override
    public boolean isFileRequired() {
        return true;
    }

    @Override
    public int getMaxFileCount() {
        return 5;
    }
}
