package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringLike;
import com.example.demo.domain.User;
import com.example.demo.domain.Itinerary;
import java.util.List;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GatheringLikeRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatheringServiceImplTest {

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GatheringMemberRepository gatheringMemberRepository;
    @Mock
    private GatheringLikeRepository gatheringLikeRepository;
    @Mock
    private ItineraryRepository itineraryRepository;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GatheringServiceImpl gatheringService;

    @Test
    @DisplayName("모임 생성 성공")
    void createGathering_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().title("New Gathering").members(new ArrayList<>()).build();
        
        given(securityService.getCurrentUser()).willReturn(host);
        given(gatheringRepository.save(any())).willReturn(gathering);

        // when
        Gathering result = gatheringService.createGathering(gathering);

        // then
        assertThat(result.getHost()).isEqualTo(host);
        verify(gatheringMemberRepository).save(any());
        verify(gatheringRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("모임 좋아요 성공 - 좋아요 추가")
    void likeGathering_AddLike() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).likeCount(0).build();
        
        given(securityService.getCurrentUser()).willReturn(user);
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.findByUserAndGathering(user, gathering)).willReturn(Optional.empty());

        // when
        gatheringService.likeGathering(10L);

        // then
        assertThat(gathering.getLikeCount()).isEqualTo(1);
        verify(gatheringLikeRepository).save(any(GatheringLike.class));
    }

    @Test
    @DisplayName("모임 상세 조회 실패 - 존재하지 않는 모임")
    void getGathering_NotFound_ThrowsException() {
        // given
        given(gatheringRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringService.getGathering(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GATHERING_NOT_FOUND);
    }

    @Test
    @DisplayName("전체 모임 조회 - 위치가 null인 경우 전체 검색")
    void getAllGatherings_WithNullLocation() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Gathering 1").build();
        given(gatheringRepository.searchGatherings(null, null, null, null)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getAllGatherings(null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Gathering 1");
    }

    @Test
    @DisplayName("전체 모임 조회 - 위치가 빈 문자열인 경우 전체 검색")
    void getAllGatherings_WithEmptyLocation() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Gathering 1").build();
        given(gatheringRepository.searchGatherings(null, null, null, null)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getAllGatherings("  ");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("전체 모임 조회 - 위치가 '전체'인 경우 전체 검색")
    void getAllGatherings_WithAllLocation() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Gathering 1").build();
        given(gatheringRepository.searchGatherings(null, null, null, null)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getAllGatherings("전체");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("전체 모임 조회 - 특정 위치가 들어올 경우 해당 위치로 검색")
    void getAllGatherings_WithSpecificLocation() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Seoul Gathering").build();
        given(gatheringRepository.searchGatherings(null, null, "서울", null)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getAllGatherings("서울");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Seoul Gathering");
    }

    @Test
    @DisplayName("모임 검색 - 복합 필터 검색 작동 확인")
    void searchGatherings_WithAllFilters() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Search Result").build();
        given(gatheringRepository.searchGatherings("쿼리", "카테고리", "인천", true)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.searchGatherings("쿼리", "카테고리", "인천", true);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("모임 검색 - 위치 필터가 null 일 때 전체 지역 검색 작동 확인")
    void searchGatherings_WithNullLocation() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Search Result").build();
        given(gatheringRepository.searchGatherings("쿼리", "카테고리", null, true)).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.searchGatherings("쿼리", "카테고리", null, true);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("모임 생성 시 연동된 일정이 있는 경우 매핑 처리")
    void createGathering_WithLinkedItinerary() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Itinerary itinerary = Itinerary.builder().id(5L).title("Trip Plan").build();
        Gathering gathering = Gathering.builder().title("New Gathering").linkedItinerary(itinerary).members(new ArrayList<>()).build();

        given(securityService.getCurrentUser()).willReturn(host);
        given(gatheringRepository.save(any())).willReturn(gathering);
        given(itineraryRepository.findById(5L)).willReturn(Optional.of(itinerary));

        // when
        Gathering result = gatheringService.createGathering(gathering);

        // then
        assertThat(result.getLinkedItinerary()).isEqualTo(itinerary);
        verify(itineraryRepository).findById(5L);
        verify(gatheringRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("모임 정보 수정 성공")
    void updateGathering_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).title("Old Title").build();
        Gathering updateData = Gathering.builder().title("New Title").location("부산").maxJoining(10).isGalleryPublic(true).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");

        // when
        Gathering result = gatheringService.updateGathering(10L, updateData);

        // then
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getLocation()).isEqualTo("부산");
        assertThat(result.getMaxJoining()).isEqualTo(10);
        assertThat(result.isGalleryPublic()).isTrue();
    }

    @Test
    @DisplayName("모임 정보 수정 성공 - 연동 일정 추가")
    void updateGathering_Success_WithLinkedItinerary() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).title("Title").build();
        Itinerary itinerary = Itinerary.builder().id(5L).title("Plan").build();
        Gathering updateData = Gathering.builder().title("Title").linkedItinerary(itinerary).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(itineraryRepository.findById(5L)).willReturn(Optional.of(itinerary));

        // when
        Gathering result = gatheringService.updateGathering(10L, updateData);

        // then
        assertThat(result.getLinkedItinerary()).isEqualTo(itinerary);
    }

    @Test
    @DisplayName("모임 정보 수정 성공 - 연동 일정 해제")
    void updateGathering_Success_RemoveLinkedItinerary() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Itinerary itinerary = Itinerary.builder().id(5L).title("Plan").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).title("Title").linkedItinerary(itinerary).build();
        Gathering updateData = Gathering.builder().title("Title").linkedItinerary(null).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");

        // when
        Gathering result = gatheringService.updateGathering(10L, updateData);

        // then
        assertThat(result.getLinkedItinerary()).isNull();
    }

    @Test
    @DisplayName("호스트가 아닌 유저가 모임 정보 수정 시 예외 발생")
    void updateGathering_NotHost_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).title("Title").build();
        Gathering updateData = Gathering.builder().title("New Title").build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("not-host@test.com");

        // when & then
        assertThatThrownBy(() -> gatheringService.updateGathering(10L, updateData))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("모임 삭제 성공")
    void deleteGathering_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");

        // when
        gatheringService.deleteGathering(10L);

        // then
        verify(gatheringRepository).softDeleteById(10L);
    }

    @Test
    @DisplayName("호스트가 아닌 유저가 모임 삭제 시 예외 발생")
    void deleteGathering_NotHost_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("not-host@test.com");

        // when & then
        assertThatThrownBy(() -> gatheringService.deleteGathering(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("내가 호스트인 모임 목록 조회")
    void getHostedGatherings_Success() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("My Hosted").build();
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringRepository.findByHostEmailOrderByCreatedAtDesc("host@test.com")).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getHostedGatherings();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("My Hosted");
    }

    @Test
    @DisplayName("단건 모임 정상 조회")
    void getGathering_Success() {
        // given
        Gathering gathering = Gathering.builder().id(10L).title("Gathering Title").build();
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));

        // when
        Gathering result = gatheringService.getGathering(10L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Gathering Title");
    }

    @Test
    @DisplayName("모임 좋아요 취소 성공")
    void likeGathering_RemoveLike() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).likeCount(1).build();
        GatheringLike like = GatheringLike.builder().id(100L).user(user).gathering(gathering).build();

        given(securityService.getCurrentUser()).willReturn(user);
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.findByUserAndGathering(user, gathering)).willReturn(Optional.of(like));

        // when
        gatheringService.likeGathering(10L);

        // then
        assertThat(gathering.getLikeCount()).isEqualTo(0);
        verify(gatheringLikeRepository).delete(like);
        verify(gatheringRepository).save(gathering);
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 익명 유저일 때 false 반환")
    void isLikedByUser_Anonymous_ReturnsFalse() {
        // given
        given(securityService.isAnonymous()).willReturn(true);

        // when & then
        assertThat(gatheringService.isLikedByUser(10L, "user@test.com")).isFalse();
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 사용자가 존재하지 않을 때 false 반환")
    void isLikedByUser_UserNotFound_ReturnsFalse() {
        // given
        given(securityService.isAnonymous()).willReturn(false);
        given(userRepository.findByEmail("stranger@test.com")).willReturn(Optional.empty());

        // when & then
        assertThat(gatheringService.isLikedByUser(10L, "stranger@test.com")).isFalse();
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 모임이 존재하지 않을 때 false 반환")
    void isLikedByUser_GatheringNotFound_ReturnsFalse() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        given(securityService.isAnonymous()).willReturn(false);
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(gatheringRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThat(gatheringService.isLikedByUser(99L, "user@test.com")).isFalse();
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 좋아요가 눌린 상태일 때 true 반환")
    void isLikedByUser_Liked_ReturnsTrue() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).build();

        given(securityService.isAnonymous()).willReturn(false);
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.existsByUserAndGathering(user, gathering)).willReturn(true);

        // when & then
        assertThat(gatheringService.isLikedByUser(10L, "user@test.com")).isTrue();
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 좋아요가 눌리지 않은 상태일 때 false 반환")
    void isLikedByUser_NotLiked_ReturnsFalse() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).build();

        given(securityService.isAnonymous()).willReturn(false);
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.existsByUserAndGathering(user, gathering)).willReturn(false);

        // when & then
        assertThat(gatheringService.isLikedByUser(10L, "user@test.com")).isFalse();
    }
}
