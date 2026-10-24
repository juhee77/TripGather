package com.tripgather.usecase;

import com.tripgather.dto.StampResponse;
import java.util.List;

public interface StampUseCase {
    List<StampResponse> getMyStamps();
    void awardStamp(Long userId, Long gatheringId, String title, String stampImageUrl);
}
