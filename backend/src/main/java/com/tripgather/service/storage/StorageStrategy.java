package com.tripgather.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    String storeFile(MultipartFile file, String uniqueFileName);
    void deleteFile(String fileUrl);
}
