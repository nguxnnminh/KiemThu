package com.smartdental.repository;

import com.smartdental.entity.CodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CodeSequenceRepository extends JpaRepository<CodeSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CodeSequence c where c.sequenceKey = :sequenceKey")
    Optional<CodeSequence> findBySequenceKeyForUpdate(@Param("sequenceKey") String sequenceKey);

    Optional<CodeSequence> findBySequenceKey(String sequenceKey);
}
