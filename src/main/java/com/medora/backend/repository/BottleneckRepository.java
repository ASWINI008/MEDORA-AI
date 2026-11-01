package com.medora.backend.repository;

import com.medora.backend.entity.Bottleneck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BottleneckRepository extends JpaRepository<Bottleneck, Long> {
}
