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

@Service
@RequiredArgsConstructor
public class PackingService {

    private final PackingItemRepository packingItemRepository;
    private final TripRepository tripRepository;
    private final ProfanityFilterService profanityFilterService;
    private final TripAccessGuard tripAccessGuard;

    /**
     * 준비물 카테고리를 화면에 보여줄 순서.
     *
     * 이름순으로 정렬하면 유니코드 차례상 "기타" 가 맨 위, "필수" 가 맨 아래로 간다.
     * 목록을 열었을 때 상비약과 우산이 먼저 보이고 여권이 맨 끝에 있었다.
     * 여기에 없는 카테고리(사용자가 직접 만든 것)는 뒤에 이름순으로 붙는다.
     */
    private static final List<String> CATEGORY_ORDER = List.of("필수", "전자기기", "의류", "세면", "기타");

    /** 기본 준비물이 이 여행에 해당하는지 가리는 조건. */
    private enum Applies {
        /** 어떤 여행이든 챙긴다. */
        ALWAYS,
        /** 해외 여행에만 해당한다. */
        OVERSEAS_ONLY,
        /** 하룻밤 이상 묵는 여행에만 해당한다. */
        OVERNIGHT_ONLY
    }

    /**
     * @param perDay 여행 일수만큼 필요한 물건이면 true. 이름 뒤에 "(3일치)" 처럼 며칠치인지 붙인다.
     */
    private record DefaultItem(String category, String name, Applies applies, boolean perDay) {
        static DefaultItem of(String category, String name) {
            return new DefaultItem(category, name, Applies.ALWAYS, false);
        }

        static DefaultItem of(String category, String name, Applies applies) {
            return new DefaultItem(category, name, applies, false);
        }

        static DefaultItem perDay(String category, String name, Applies applies) {
            return new DefaultItem(category, name, applies, true);
        }
    }

    /**
     * 기본 준비물 템플릿.
     *
     * 예전에는 여행과 무관하게 같은 16개를 넣었다. 그래서 당일치기 국내 여행에도
     * 여권과 멀티어댑터, 잠옷이 따라붙었고 사용자가 매번 지워야 했다.
     * 여행의 나라와 기간을 보고 해당하는 것만 넣는다.
     */
    private static final List<DefaultItem> DEFAULT_ITEMS = List.of(
            DefaultItem.of("필수", "여권", Applies.OVERSEAS_ONLY),
            DefaultItem.of("필수", "여행자 보험", Applies.OVERSEAS_ONLY),
            DefaultItem.of("필수", "항공권/e-티켓"),
            DefaultItem.of("필수", "현금/카드"),
            DefaultItem.of("필수", "숙소 예약 확인서", Applies.OVERNIGHT_ONLY),

            DefaultItem.of("전자기기", "충전기"),
            DefaultItem.of("전자기기", "보조배터리"),
            DefaultItem.of("전자기기", "멀티어댑터", Applies.OVERSEAS_ONLY),

            DefaultItem.perDay("의류", "속옷/양말", Applies.OVERNIGHT_ONLY),
            DefaultItem.of("의류", "잠옷", Applies.OVERNIGHT_ONLY),
            DefaultItem.of("의류", "외투"),

            DefaultItem.of("세면", "칫솔/치약", Applies.OVERNIGHT_ONLY),
            DefaultItem.of("세면", "샴푸/바디워시", Applies.OVERNIGHT_ONLY),
            DefaultItem.of("세면", "썬크림"),

            DefaultItem.of("기타", "상비약"),
            DefaultItem.of("기타", "우산")
    );

    /** 국내 여행을 가리키는 국가 코드. */
    private static final String DOMESTIC_COUNTRY = "KR";

    @Transactional
    public List<PackingItemResponse> initDefaultItems(Long tripId) {
        tripAccessGuard.requireOwner(tripId);
        if (tripId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 ID가 올바르지 않습니다.");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));

        // 이미 있는 항목은 건너뛴다. 버튼을 두 번 누르면 목록이 통째로 두 벌이 되던 문제를 막고,
        // 사용자가 직접 넣은 항목을 지우지 않으면서 빠진 것만 채워 넣는다.
        java.util.Set<String> existing = packingItemRepository.findByTripId(tripId).stream()
                .map(PackingItem::getName)
                .collect(java.util.stream.Collectors.toSet());

        for (DefaultItem item : DEFAULT_ITEMS) {
            if (!appliesTo(item, trip)) {
                continue;
            }
            String name = itemName(item, trip);
            if (existing.add(name)) {
                packingItemRepository.save(PackingItem.of(trip, name, item.category()));
            }
        }

        return getItems(tripId);
    }

    /** 정의된 카테고리는 그 순서대로, 사용자가 만든 카테고리는 그 뒤에 이름순으로 둔다. */
    private static int categoryRank(String category) {
        int index = CATEGORY_ORDER.indexOf(category);
        return index >= 0 ? index : CATEGORY_ORDER.size();
    }

    /** 이 여행이 해외 여행인가. 국가를 모르면 해외로 본다(빠뜨리는 쪽보다 낫다). */
    private boolean isOverseas(Trip trip) {
        return trip.getCountry() == null || !DOMESTIC_COUNTRY.equalsIgnoreCase(trip.getCountry().trim());
    }

    /**
     * 몇 밤을 묵는 여행인가. 날짜를 모르면 -1 을 돌려준다.
     *
     * 날짜가 비어 있으면 당일치기인지 알 수 없으므로, 호출부는 숙박 조건을 걸지 않고 모두 넣는다.
     */
    private long nights(Trip trip) {
        if (trip.getStartDate() == null || trip.getEndDate() == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate());
    }

    private boolean appliesTo(DefaultItem item, Trip trip) {
        return switch (item.applies()) {
            case ALWAYS -> true;
            case OVERSEAS_ONLY -> isOverseas(trip);
            // 날짜를 모르면(-1) 판단을 미루고 넣어 둔다.
            case OVERNIGHT_ONLY -> nights(trip) != 0;
        };
    }

    /** 일수만큼 필요한 물건이면 며칠치인지 이름에 적어 준다. */
    private String itemName(DefaultItem item, Trip trip) {
        long nights = nights(trip);
        if (!item.perDay() || nights < 0) {
            return item.name();
        }
        return item.name() + " (" + (nights + 1) + "일치)";
    }

    @Transactional(readOnly = true)
    public List<PackingItemResponse> getItems(Long tripId) {
        tripAccessGuard.requireOwner(tripId);
        if (tripId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 ID가 올바르지 않습니다.");
        }
        if (!tripRepository.existsById(tripId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다.");
        }
        return packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(tripId)
                .stream()
                .sorted(java.util.Comparator
                        .comparingInt((PackingItem i) -> categoryRank(i.getCategory()))
                        .thenComparing(PackingItem::getCategory)
                        .thenComparing(PackingItem::getName))
                .map(PackingItemResponse::from)
                .toList();
    }

    @Transactional
    public PackingItemResponse addItem(Long tripId, String name, String category) {
        tripAccessGuard.requireOwner(tripId);
        if (tripId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 ID가 올바르지 않습니다.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물 항목명을 입력해주세요.");
        }
        if (name.trim().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물 항목명은 100자 이내여야 합니다.");
        }
        if (category != null && category.trim().length() > 50) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카테고리명은 50자 이내여야 합니다.");
        }
        profanityFilterService.validateText(name);

        if (category != null && !category.trim().isEmpty()) {
            profanityFilterService.validateText(category);
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));
        PackingItem item = PackingItem.of(trip, name.trim(), category != null ? category.trim() : "기타");
        return PackingItemResponse.from(packingItemRepository.save(item));
    }

    @Transactional
    public PackingItemResponse toggleCheck(Long itemId) {
        if (itemId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물 ID가 올바르지 않습니다.");
        }
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물을 찾을 수 없습니다."));
        item.setChecked(!item.isChecked());
        return PackingItemResponse.from(packingItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (itemId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물 ID가 올바르지 않습니다.");
        }
        PackingItem item = packingItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "준비물을 찾을 수 없습니다."));
        packingItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public com.example.demo.dto.PackingProgressResponse getPackingProgress(Long tripId) {
        tripAccessGuard.requireOwner(tripId);
        if (tripId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 ID가 올바르지 않습니다.");
        }
        if (!tripRepository.existsById(tripId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다.");
        }
        List<PackingItem> items = packingItemRepository.findByTripId(tripId);
        int totalCount = items.size();
        int checkedCount = (int) items.stream().filter(PackingItem::isChecked).count();
        double percentage = totalCount > 0 ? (double) checkedCount / totalCount * 100.0 : 0.0;

        return com.example.demo.dto.PackingProgressResponse.builder()
                .tripId(tripId)
                .totalCount(totalCount)
                .checkedCount(checkedCount)
                .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                .build();
    }
}
