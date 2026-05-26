package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryCustom {
    List<Gathering> findAllByOrderByCreatedAtDesc();
    List<Gathering> findAllByLocationContainingIgnoreCaseOrderByCreatedAtDesc(String location);
    List<Gathering> findByHostEmailOrderByCreatedAtDesc(String email);
    
    @org.springframework.data.jpa.repository.Query("SELECT g FROM Gathering g JOIN g.members m WHERE m.user.email = :email AND m.status = 'APPROVED' ORDER BY g.createdAt DESC")
    List<Gathering> findJoinedGatherings(String email);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Gathering g SET g.deleted = true WHERE g.id = :id")
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Long id);
}
