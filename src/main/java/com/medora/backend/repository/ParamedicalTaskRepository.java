package com.medora.backend.repository;

import com.medora.backend.entity.ParamedicalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamedicalTaskRepository extends JpaRepository<ParamedicalTask, Long> {
    List<ParamedicalTask> findByStatus(String status);
    List<ParamedicalTask> findByStaffType(String staffType);
    List<ParamedicalTask> findByDepartmentNameContainingIgnoreCase(String departmentName);
}
