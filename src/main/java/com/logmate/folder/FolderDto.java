package com.logmate.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@Builder
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
