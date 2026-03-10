package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> {
    List<Gathering> findAllByOrderByCreatedAtDesc();
}
