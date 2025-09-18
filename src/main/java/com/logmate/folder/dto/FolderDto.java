package com.logmate.folder.dto;

import com.logmate.folder.model.Folder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class FolderDto {
    private Long id;
    private String name;
    private String createdAt;
    private String updatedAt;

    public static FolderDto from(Folder folder) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return new FolderDto(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt() != null ? folder.getCreatedAt().format(fmt) : null,
                folder.getUpdatedAt() != null ? folder.getUpdatedAt().format(fmt) : null
        );
    }
}
