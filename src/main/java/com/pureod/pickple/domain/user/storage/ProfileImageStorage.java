package com.pureod.pickple.domain.user.storage;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {

    String upload(UUID userId, MultipartFile image);
}
