package com.example.demo.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {

    String storeFile(MultipartFile file, String uniqueFileName);

    /**
     * 이 저장소가 직접 저장한 파일의 주소인지 판단한다.
     *
     * <p>본문(게시글·리뷰)에는 외부 이미지 주소나 base64 data URI 가 그대로 들어올 수 있다.
     * 삭제를 곁들일 때 남의 주소까지 삭제 시도하지 않도록 먼저 가려낸다.
     */
    boolean isManaged(String fileUrl);

    void deleteFile(String fileUrl);
}
