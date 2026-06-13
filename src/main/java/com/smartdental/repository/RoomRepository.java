package com.smartdental.repository;

import com.smartdental.entity.Room;
import com.smartdental.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomCode(String roomCode);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Room> findByStatus(RoomStatus status);

    @Query("select r from Room r where " +
            "(:keyword is null or lower(r.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(r.roomCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:status is null or r.status = :status)")
    Page<Room> search(@Param("keyword") String keyword,
                       @Param("status") RoomStatus status,
                       Pageable pageable);
}
