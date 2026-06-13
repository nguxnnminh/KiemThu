package com.smartdental.service;

import com.smartdental.entity.CodeSequence;
import com.smartdental.enums.CodePrefix;
import com.smartdental.repository.CodeSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sinh ma tu dong an toan (BS0001, LT0001, QL0001, BN0001, DV001, ...).
 * Chay trong transaction rieng (REQUIRES_NEW) va khoa pessimistic tren
 * code_sequences de chong sinh trung ma khi nhieu nguoi thao tac dong thoi.
 */
@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final CodeSequenceRepository codeSequenceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextCode(CodePrefix prefix) {
        CodeSequence sequence = codeSequenceRepository.findBySequenceKeyForUpdate(prefix.getSequenceKey())
                .orElseGet(() -> {
                    CodeSequence created = new CodeSequence();
                    created.setSequenceKey(prefix.getSequenceKey());
                    created.setCurrentValue(0L);
                    return created;
                });

        long nextValue = sequence.getCurrentValue() + 1;
        sequence.setCurrentValue(nextValue);
        sequence.setUpdatedAt(LocalDateTime.now());
        codeSequenceRepository.save(sequence);

        String numberPart = String.format("%0" + prefix.getPaddingLength() + "d", nextValue);
        return prefix.getPrefix() + numberPart;
    }
}
