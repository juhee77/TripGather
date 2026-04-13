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
}
