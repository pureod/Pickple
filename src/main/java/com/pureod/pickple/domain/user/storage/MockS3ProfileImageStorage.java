package com.pureod.pickple.domain.user.storage;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class MockS3ProfileImageStorage implements ProfileImageStorage {

    @Override
    public String upload(UUID userId, MultipartFile image) {
        String originalFileName = image.getOriginalFilename() == null
            ? "profile-image"
            : image.getOriginalFilename();

        String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = "profiles/%s/%s".formatted(userId, sanitizedFileName);

        log.info("[Storage] S3 업로드 예정 objectKey={} (현재는 mock URL 반환)", objectKey);

        return "https://s3.ap-northeast-2.amazonaws.com/pickple-bucket/%s".formatted(objectKey);
    }
}
