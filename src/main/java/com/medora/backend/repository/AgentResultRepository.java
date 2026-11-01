package com.medora.backend.repository;

import com.medora.backend.entity.AgentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentResultRepository extends JpaRepository<AgentResult, Long> {
}
