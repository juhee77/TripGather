package com.example.demo.service;

import com.example.demo.domain.PackingItem;
import com.example.demo.domain.Trip;
import com.example.demo.dto.PackingItemResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PackingItemRepository;
import com.example.demo.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PackingService {

    private final PackingItemRepository packingItemRepository;
    private final TripRepository tripRepository;

    private static final Map<String, List<String[]>> DEFAULT_ITEMS = Map.of(
            "필수", List.of(
                    new String[]{"여권"}, new String[]{"항공권/e-티켓"}, new String[]{"현금/카드"},
                    new String[]{"여행자 보험"}, new String[]{"호텔 예약 확인서"}
            ),
            "전자기기", List.of(
                    new String[]{"충전기"}, new String[]{"보조배터리"}, new String[]{"멀티어댑터"}
            ),
            "세면", List.of(
                    new String[]{"칫솔/치약"}, new String[]{"샴푸/바디워시"}, new String[]{"썬크림"}
            ),
            "의류", List.of(
                    new String[]{"속옷/양말"}, new String[]{"잠옷"}, new String[]{"외투"}
            ),
            "기타", List.of(
                    new String[]{"상비약"}, new String[]{"우산"}
            )
    );

    @Transactional
    public List<PackingItemResponse> initDefaultItems(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));

        DEFAULT_ITEMS.forEach((category, items) ->
                items.forEach(item -> packingItemRepository.save(PackingItem.of(trip, item[0], category)))
        );

        return getItems(tripId);
    }

    @Transactional(readOnly = true)
    public List<PackingItemResponse> getItems(Long tripId) {
        return packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(tripId)
                .stream()
                .map(PackingItemResponse::from)
                .toList();
    }

    @Transactional
    public PackingItemResponse addItem(Long tripId, String name, String category) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));
        PackingItem item = PackingItem.of(trip, name, category != null ? category : "기타");
        return PackingItemResponse.from(packingItemRepository.save(item));
    }

    @Transactional
    public PackingItemResponse toggleCheck(Long itemId) {
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물을 찾을 수 없습니다."));
        item.setChecked(!item.isChecked());
        return PackingItemResponse.from(packingItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId) {
        packingItemRepository.deleteById(itemId);
    }
}
