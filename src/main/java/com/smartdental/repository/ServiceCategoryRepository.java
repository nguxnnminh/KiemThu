package com.smartdental.repository;

import com.smartdental.entity.ServiceCategory;
import com.smartdental.enums.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    @Query("select case when count(c) > 0 then true else false end from ServiceCategory c where lower(c.name) = lower(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("select case when count(c) > 0 then true else false end from ServiceCategory c " +
            "where lower(c.name) = lower(:name) and c.id <> :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    Optional<ServiceCategory> findByCategoryCode(String categoryCode);

    List<ServiceCategory> findByStatus(CommonStatus status);

    List<ServiceCategory> findAllByOrderByNameAsc();
}
