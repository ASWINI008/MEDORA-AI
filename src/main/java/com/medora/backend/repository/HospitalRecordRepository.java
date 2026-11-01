package com.medora.backend.repository;

import com.medora.backend.entity.HospitalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for HospitalRecord entity.
 * Provides derived queries and custom JPQL for dataset analytics.
 */
@Repository
public interface HospitalRecordRepository extends JpaRepository<HospitalRecord, Long> {

    // ── Derived Count Queries ──────────────────────────────────────────────

    long countByReadmission(String readmission);

    long countByOutcome(String outcome);

    long countByGender(String gender);

    // ── Derived Finder Queries ─────────────────────────────────────────────

    List<HospitalRecord> findByCondition(String condition);

    List<HospitalRecord> findByGender(String gender);

    List<HospitalRecord> findByReadmission(String readmission);

    boolean existsByPatientExternalId(Integer patientExternalId);

    // ── Aggregate JPQL Queries ─────────────────────────────────────────────

    @Query("SELECT AVG(h.cost) FROM HospitalRecord h")
    Double findAvgCost();

    @Query("SELECT AVG(h.age) FROM HospitalRecord h")
    Double findAvgAge();

    @Query("SELECT AVG(h.lengthOfStay) FROM HospitalRecord h")
    Double findAvgLengthOfStay();

    @Query("SELECT AVG(h.satisfaction) FROM HospitalRecord h")
    Double findAvgSatisfaction();

    // ── Condition Group Stats ──────────────────────────────────────────────

    /**
     * Returns [condition, count, avgCost, avgLos] per medical condition.
     */
    @Query("SELECT h.condition, COUNT(h), AVG(h.cost), AVG(h.lengthOfStay) " +
           "FROM HospitalRecord h GROUP BY h.condition ORDER BY COUNT(h) DESC")
    List<Object[]> findConditionStats();

    // ── Procedure Group Stats ──────────────────────────────────────────────

    /**
     * Returns [procedure, count, avgCost, avgLos] per medical procedure.
     */
    @Query("SELECT h.procedure, COUNT(h), AVG(h.cost), AVG(h.lengthOfStay) " +
           "FROM HospitalRecord h GROUP BY h.procedure ORDER BY COUNT(h) DESC")
    List<Object[]> findProcedureStats();

    // ── Outcome Group Stats ────────────────────────────────────────────────

    @Query("SELECT h.outcome, COUNT(h) FROM HospitalRecord h GROUP BY h.outcome")
    List<Object[]> findOutcomeGroupCounts();

    // ── Gender Group Stats ─────────────────────────────────────────────────

    @Query("SELECT h.gender, COUNT(h) FROM HospitalRecord h GROUP BY h.gender")
    List<Object[]> findGenderGroupCounts();

    // ── Satisfaction Distribution ──────────────────────────────────────────

    @Query("SELECT h.satisfaction, COUNT(h) FROM HospitalRecord h GROUP BY h.satisfaction ORDER BY h.satisfaction")
    List<Object[]> findSatisfactionDistribution();

    // ── Cost Stats ────────────────────────────────────────────────────────

    @Query("SELECT MIN(h.cost), MAX(h.cost), AVG(h.cost) FROM HospitalRecord h")
    Object[] findCostMinMaxAvg();

    // ── All costs for median calculation ──────────────────────────────────

    @Query("SELECT h.cost FROM HospitalRecord h ORDER BY h.cost")
    List<Double> findAllCostsSorted();

    // ── Age-group raw ages for bucketing ──────────────────────────────────

    @Query("SELECT h.age FROM HospitalRecord h")
    List<Integer> findAllAges();
}
