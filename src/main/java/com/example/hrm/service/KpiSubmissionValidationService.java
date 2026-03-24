package com.example.hrm.service;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KpiSubmissionValidationService {

    private static final Set<String> KPI_EXTENSIONS = Set.of("xlsx", "xls");
    private static final Set<String> EVIDENCE_EXTENSIONS = Set.of("pdf", "xlsx", "xls", "docx", "doc", "jpg", "jpeg", "png", "gif", "webp");
    private static final int MIN_EVIDENCE_COUNT = 1;

    private final KpiEvidenceService kpiEvidenceService;

    public SubmissionValidationResult validateDraftSubmission(KpiAssignment assignment,
                                                               MultipartFile kpiExcelFile,
                                                               List<MultipartFile> evidenceFiles,
                                                               String employeeComment) {
        SubmissionValidationResult result = SubmissionValidationResult.valid();

        if (assignment == null) {
            result.addError("KPI assignment not found.");
            return result;
        }

        List<KpiEvidence> existingEvidences = kpiEvidenceService.getEvidencesByAssignment(assignment.getAssignmentId());
        boolean hasExistingKpiFile = hasText(assignment.getEmployeeExcelPath());
        boolean hasNewKpiFile = isRealFile(kpiExcelFile);

        if (isNamedButEmpty(kpiExcelFile)) {
            result.addError("KPI Excel file '" + safeName(kpiExcelFile.getOriginalFilename()) + "' is empty.");
        }

        if (!hasExistingKpiFile && !hasNewKpiFile) {
            result.addError("Please upload KPI Excel file before continuing.");
        }

        if (hasNewKpiFile) {
            validateMultipartFile(kpiExcelFile, KPI_EXTENSIONS, "KPI Excel file", result);
        }

        int persistedEvidenceCount = (int) existingEvidences.stream().filter(this::isStoredEvidenceValid).count();
        int newEvidenceCount = 0;
        if (evidenceFiles != null) {
            for (MultipartFile evidenceFile : evidenceFiles) {
                if (isRealFile(evidenceFile)) {
                    newEvidenceCount++;
                    validateMultipartFile(evidenceFile, EVIDENCE_EXTENSIONS, "Evidence file", result);
                } else if (isNamedButEmpty(evidenceFile)) {
                    result.addError("Evidence file '" + safeName(evidenceFile.getOriginalFilename()) + "' is empty.");
                }
            }
        }

        int totalEvidenceCount = persistedEvidenceCount + newEvidenceCount;
        if (totalEvidenceCount < MIN_EVIDENCE_COUNT) {
            result.addError("At least " + MIN_EVIDENCE_COUNT + " evidence file is required.");
        }

        if (!hasText(employeeComment)) {
            result.addError("KPI comment is required.");
        }

        return result;
    }

    public SubmissionValidationResult validateBeforeHrSubmission(KpiAssignment assignment,
                                                                 String selfAssessment,
                                                                 Integer selfScore) {
        SubmissionValidationResult result = SubmissionValidationResult.valid();

        if (assignment == null) {
            result.addError("KPI assignment not found.");
            return result;
        }

        if (!hasText(assignment.getEmployeeExcelPath())) {
            result.addError("Missing KPI Excel file. Please complete Step 1 first.");
        } else {
            String excelExtension = extensionOf(assignment.getEmployeeExcelPath());
            if (!KPI_EXTENSIONS.contains(excelExtension)) {
                result.addError("KPI Excel file format is not allowed.");
            }
        }

        List<KpiEvidence> evidences = kpiEvidenceService.getEvidencesByAssignment(assignment.getAssignmentId());
        if (evidences.size() < MIN_EVIDENCE_COUNT) {
            result.addError("At least " + MIN_EVIDENCE_COUNT + " evidence file is required before submitting to HR.");
        }

        for (KpiEvidence evidence : evidences) {
            if (!isStoredEvidenceValid(evidence)) {
                result.addError("Evidence file '" + safeName(evidence.getFileName()) + "' is invalid or empty.");
                continue;
            }

            String extension = extensionOf(evidence.getFileName());
            if (!EVIDENCE_EXTENSIONS.contains(extension)) {
                result.addError("Evidence file '" + safeName(evidence.getFileName()) + "' has unsupported format.");
            }
        }

        if (!hasText(assignment.getEmployeeComment())) {
            result.addError("KPI comment is required before submitting to HR.");
        }

        if (!hasText(selfAssessment)) {
            result.addError("Overall self assessment is required.");
        }

        if (selfScore == null) {
            result.addError("Overall self score is required.");
        } else if (selfScore < 0 || selfScore > 100) {
            result.addError("Overall self score must be between 0 and 100.");
        }

        return result;
    }

    private void validateMultipartFile(MultipartFile file,
                                       Set<String> allowedExtensions,
                                       String label,
                                       SubmissionValidationResult result) {
        if (file == null) {
            result.addError(label + " is missing.");
            return;
        }

        if (file.getSize() <= 0) {
            result.addError(label + " '" + safeName(file.getOriginalFilename()) + "' is empty.");
            return;
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!allowedExtensions.contains(extension)) {
            result.addError(label + " '" + safeName(file.getOriginalFilename()) + "' has unsupported format.");
        }
    }

    private boolean isStoredEvidenceValid(KpiEvidence evidence) {
        return evidence != null
                && hasText(evidence.getStoredPath())
                && evidence.getFileSize() != null
                && evidence.getFileSize() > 0;
    }

    private boolean isRealFile(MultipartFile file) {
        return file != null && !file.isEmpty() && hasText(file.getOriginalFilename());
    }

    private boolean isNamedButEmpty(MultipartFile file) {
        return file != null && file.isEmpty() && hasText(file.getOriginalFilename());
    }

    private String extensionOf(String filename) {
        if (!hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String safeName(String filename) {
        return hasText(filename) ? filename : "unknown";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
