package com.smartdental.enums;

/**
 * Tien to ma tu sinh dung trong {@link com.smartdental.service.CodeGeneratorService}.
 * Moi prefix tuong ung mot sequence key rieng trong bang code_sequences.
 */
public enum CodePrefix {
    USER("ND", 4),
    DOCTOR("BS", 4),
    RECEPTIONIST("LT", 4),
    MANAGER("QL", 4),
    PATIENT("BN", 4),
    SERVICE("DV", 3),
    SERVICE_CATEGORY("NH", 3),
    HOLIDAY("NN", 3),
    WORK_SHIFT("CA", 3),
    ROOM("P", 3),
    CHAIR("G", 3),
    DOCTOR_SHIFT("TR", 6),
    APPOINTMENT("LK", 6),
    MEDICAL_RECORD("BA", 4),
    TREATMENT_SESSION("LS", 5),
    INVOICE("HD", 4),
    PAYROLL_SLIP("PL", 6),
    HOURLY_RATE("TG", 4),
    SHIFT_COEFFICIENT("HSC", 4),
    COMPLEX_CASE_COEFFICIENT("HSK", 4);

    private final String prefix;
    private final int paddingLength;

    CodePrefix(String prefix, int paddingLength) {
        this.prefix = prefix;
        this.paddingLength = paddingLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPaddingLength() {
        return paddingLength;
    }

    /** Sequence key luu trong bang code_sequences. */
    public String getSequenceKey() {
        return name();
    }
}
