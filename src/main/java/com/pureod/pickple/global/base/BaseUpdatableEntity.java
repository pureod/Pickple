package com.pureod.pickple.global.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity{

    @LastModifiedDate
    @Column(columnDefinition = "timestamp with time zone")
    private Instant updatedAt;
}
