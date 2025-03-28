package io.github.zuneho.domain.test;

import io.github.zuneho.domain.file.type.FolderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "test_data")
public class TestData {

    @Id
    @Column(name = "idx")
    private Long idx;

    @Column(name = "folder_type")
    @Enumerated(EnumType.STRING)
    private FolderType folderType;

}
