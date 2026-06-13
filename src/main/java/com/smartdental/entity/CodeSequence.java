package com.smartdental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Bo dem sinh ma tu dong, dung cho {@link com.smartdental.service.CodeGeneratorService}.
 * Duoc khoa bang pessimistic lock (SELECT ... FOR UPDATE) trong transaction rieng
 * de tranh sinh trung ma khi nhieu nguoi thao tac dong thoi.
 */
@Getter
@Setter
@Entity
@Table(name = "code_sequences")
public class CodeSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_key", nullable = false, unique = true, length = 50)
    private String sequenceKey;

    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
