package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringLike;
import com.example.demo.domain.User;
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
}
