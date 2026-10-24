package com.tripgather.dto;

import com.tripgather.domain.PackingItem;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingItemResponse {
    private Long id;
    private String name;
    private String category;
    private boolean checked;

    public static PackingItemResponse from(PackingItem item) {
        return PackingItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .checked(item.isChecked())
                .build();
    }
}
