package com.smartdental.repository;

import com.smartdental.entity.DentalService;
import com.smartdental.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DentalServiceRepository extends JpaRepository<DentalService, Long> {

    Optional<DentalService> findByServiceCode(String serviceCode);

    @Query("select case when count(s) > 0 then true else false end from DentalService s " +
            "where lower(s.name) = lower(:name) and s.category.id = :categoryId")
    boolean existsByNameIgnoreCaseAndCategoryId(@Param("name") String name, @Param("categoryId") Long categoryId);

    @Query("select case when count(s) > 0 then true else false end from DentalService s " +
            "where lower(s.name) = lower(:name) and s.category.id = :categoryId and s.id <> :id")
    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(@Param("name") String name, @Param("categoryId") Long categoryId, @Param("id") Long id);

    long countByCategoryIdAndStatus(Long categoryId, CommonStatus status);

    long countByStatus(CommonStatus status);

    List<DentalService> findByStatus(CommonStatus status);

    @Query("select s from DentalService s join fetch s.category where " +
            "(:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(s.serviceCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:categoryId is null or s.category.id = :categoryId) " +
            "and (:unit is null or s.unit = :unit) " +
            "and (:status is null or s.status = :status)")
    Page<DentalService> search(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId,
                                @Param("unit") com.smartdental.enums.ServiceUnit unit,
                                @Param("status") CommonStatus status,
                                Pageable pageable);
}
