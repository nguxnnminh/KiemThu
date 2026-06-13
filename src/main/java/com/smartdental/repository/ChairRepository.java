package com.smartdental.repository;

import com.smartdental.entity.Chair;
import com.smartdental.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChairRepository extends JpaRepository<Chair, Long> {

    Optional<Chair> findByChairCode(String chairCode);

    long countByRoomIdAndStatus(Long roomId, RoomStatus status);

    List<Chair> findByRoomId(Long roomId);

    @Query("select c from Chair c join fetch c.room where c.status = :status")
    List<Chair> findByStatusFetchRoom(@Param("status") RoomStatus status);

    @Query("select case when count(c) > 0 then true else false end from Chair c " +
            "where lower(c.name) = lower(:name) and c.room.id = :roomId")
    boolean existsByNameIgnoreCaseAndRoomId(@Param("name") String name, @Param("roomId") Long roomId);

    @Query("select case when count(c) > 0 then true else false end from Chair c " +
            "where lower(c.name) = lower(:name) and c.room.id = :roomId and c.id <> :id")
    boolean existsByNameIgnoreCaseAndRoomIdAndIdNot(@Param("name") String name, @Param("roomId") Long roomId, @Param("id") Long id);

    @Query("select c from Chair c join fetch c.room where " +
            "(:keyword is null or lower(c.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(c.chairCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:roomId is null or c.room.id = :roomId) " +
            "and (:status is null or c.status = :status)")
    Page<Chair> search(@Param("keyword") String keyword,
                        @Param("roomId") Long roomId,
                        @Param("status") RoomStatus status,
                        Pageable pageable);
}
