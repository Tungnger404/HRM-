package com.example.hrm.service;

import com.example.hrm.entity.KpiAssignment;
import com.example.hrm.entity.KpiEvidence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpiSubmissionValidationServiceTest {

    @Mock
    private KpiEvidenceService kpiEvidenceService;

    @InjectMocks
    private KpiSubmissionValidationService validationService;

    @Test
    void validateDraftSubmission_shouldFailWhenMissingRequiredData() {
        KpiAssignment assignment = new KpiAssignment();
        assignment.setAssignmentId(10);

        when(kpiEvidenceService.getEvidencesByAssignment(10)).thenReturn(List.of());

        SubmissionValidationResult result = validationService.validateDraftSubmission(
                assignment,
                null,
                List.of(),
                ""
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 3);
    }

    @Test
    void validateDraftSubmission_shouldPassWhenDataIsValid() {
        KpiAssignment assignment = new KpiAssignment();
        assignment.setAssignmentId(11);

        when(kpiEvidenceService.getEvidencesByAssignment(11)).thenReturn(List.of());

        MockMultipartFile kpiFile = new MockMultipartFile(
                "kpiExcelFile",
                "kpi.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "data".getBytes()
        );
        MockMultipartFile evidence = new MockMultipartFile(
                "evidenceFiles",
                "proof.pdf",
                "application/pdf",
                "pdf-data".getBytes()
        );

        SubmissionValidationResult result = validationService.validateDraftSubmission(
                assignment,
                kpiFile,
                List.of(evidence),
                "Completed key KPI targets"
        );

        assertTrue(result.isValid());
    }

    @Test
    void validateBeforeHrSubmission_shouldFailWhenEvidenceOrAssessmentMissing() {
        KpiAssignment assignment = new KpiAssignment();
        assignment.setAssignmentId(12);
        assignment.setEmployeeExcelPath("kpi.xlsx");
        assignment.setEmployeeComment("Ready for review");

        when(kpiEvidenceService.getEvidencesByAssignment(12)).thenReturn(List.of());

        SubmissionValidationResult result = validationService.validateBeforeHrSubmission(
                assignment,
                "",
                80
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(err -> err.contains("evidence")));
    }

    @Test
    void validateBeforeHrSubmission_shouldPassWhenMinimumConditionsAreMet() {
        KpiAssignment assignment = new KpiAssignment();
        assignment.setAssignmentId(13);
        assignment.setEmployeeExcelPath("kpi.xlsx");
        assignment.setEmployeeComment("All KPI notes attached");

        KpiEvidence evidence = KpiEvidence.builder()
                .assignmentId(13)
                .fileName("proof.pdf")
                .storedPath("uploads/proof.pdf")
                .fileSize(1200L)
                .build();

        when(kpiEvidenceService.getEvidencesByAssignment(13)).thenReturn(List.of(evidence));

        SubmissionValidationResult result = validationService.validateBeforeHrSubmission(
                assignment,
                "I completed all critical objectives",
                88
        );

        assertTrue(result.isValid());
    }
}

