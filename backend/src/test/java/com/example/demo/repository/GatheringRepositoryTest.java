package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import com.example.demo.config.QueryDslConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class GatheringRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Test
    void searchGatherings_shouldFilterByTitle() {
        User host = User.builder().name("Host").email("host@test.com").password("pass").build();
        entityManager.persist(host);

        Gathering g1 = Gathering.builder().title("Spring Boot Study").location("Seoul").host(host).build();
        Gathering g2 = Gathering.builder().title("React Workshop").location("Busan").host(host).build();
        entityManager.persist(g1);
        entityManager.persist(g2);

        List<Gathering> result = gatheringRepository.searchGatherings("Spring", null, null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Spring Boot Study");
    }

    @Test
    void searchGatherings_shouldFilterByLocation() {
        User host = User.builder().name("Host").email("host@test.com").password("pass").build();
        entityManager.persist(host);

        Gathering g1 = Gathering.builder().title("Seoul Party").location("Seoul, Gangnam").host(host).build();
        Gathering g2 = Gathering.builder().title("Busan Beach").location("Busan, Haeundae").host(host).build();
        entityManager.persist(g1);
        entityManager.persist(g2);

        List<Gathering> result = gatheringRepository.searchGatherings(null, null, "Seoul", null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocation()).contains("Seoul");
    }

    @Test
    void searchGatherings_Paged_ReturnsSizePlusOneForHasNextDetection() {
        User host = User.builder().name("Host").email("host@test.com").password("pass").build();
        entityManager.persist(host);

        for (int i = 1; i <= 5; i++) {
            entityManager.persist(Gathering.builder().title("Gathering " + i).location("Seoul").host(host).build());
        }
        entityManager.flush();

        // 페이지 크기 2 로 요청하면, 다음 페이지 존재 여부 판단을 위해 3건(size+1)이 돌아온다.
        List<Gathering> firstPage = gatheringRepository.searchGatherings(
                null, null, null, null, "LATEST", 0, 2);
        assertThat(firstPage).hasSize(3);
    }

    @Test
    void searchGatherings_Paged_SecondPageStartsAfterOffset() {
        User host = User.builder().name("Host").email("host@test.com").password("pass").build();
        entityManager.persist(host);

        Gathering g1 = Gathering.builder().title("A").location("Seoul").host(host).build();
        Gathering g2 = Gathering.builder().title("B").location("Seoul").host(host).build();
        Gathering g3 = Gathering.builder().title("C").location("Seoul").host(host).build();
        entityManager.persist(g1);
        entityManager.flush();
        entityManager.persist(g2);
        entityManager.flush();
        entityManager.persist(g3);
        entityManager.flush();

        // 최신순 정렬이므로 0페이지는 [C, B], 1페이지는 [A] 여야 한다.
        List<Gathering> secondPage = gatheringRepository.searchGatherings(
                null, null, null, null, "LATEST", 1, 2);
        assertThat(secondPage).extracting(Gathering::getTitle).containsExactly("A");
    }

    @Test
    void searchGatherings_Paged_LastPageReturnsFewerThanSizePlusOne() {
        User host = User.builder().name("Host").email("host@test.com").password("pass").build();
        entityManager.persist(host);
        entityManager.persist(Gathering.builder().title("Only One").location("Seoul").host(host).build());
        entityManager.flush();

        List<Gathering> result = gatheringRepository.searchGatherings(
                null, null, null, null, "LATEST", 0, 20);
        // 전체가 size(20) 이하이므로 hasNext 판단용 1건 초과분이 없다.
        assertThat(result).hasSize(1);
    }
}
