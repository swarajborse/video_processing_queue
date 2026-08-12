package com.videoprocessing.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class VideoUploadRequest {

    private MultipartFile file;
}
