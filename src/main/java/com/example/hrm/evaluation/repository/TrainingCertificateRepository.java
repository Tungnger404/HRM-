package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.TrainingCertificate;
import com.example.hrm.evaluation.model.TrainingCertificate.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingCertificateRepository extends JpaRepository<TrainingCertificate, Integer> {

    List<TrainingCertificate> findByEmpId(Integer empId);

    List<TrainingCertificate> findByProgramId(Integer programId);

    List<TrainingCertificate> findByStatus(CertificateStatus status);

    List<TrainingCertificate> findByStatusOrderByUploadedAtDesc(CertificateStatus status);

    List<TrainingCertificate> findByEmpIdAndProgramId(Integer empId, Integer programId);
}