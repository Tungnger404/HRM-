package com.example.hrm.service.impl;

import com.example.hrm.dto.JobPositionForm;
import com.example.hrm.entity.JobPosition;
import com.example.hrm.repository.JobPositionRepository;
import com.example.hrm.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobPositionServiceImpl implements JobPositionService {

    private final JobPositionRepository repo;

    @Override
    public Page<JobPosition> search(String q, Integer level, Boolean active, int page, int size) {
        String qq = (q == null) ? "" : q.trim();
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                (size <= 0 ? 10 : size),
                Sort.by("jobLevel").ascending().and(Sort.by("title").ascending())
        );
        return repo.search(qq, level, active, pageable);
    }

    @Override
    public JobPosition getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job position not found: " + id));
    }

    @Override
    public JobPositionForm getFormById(Integer id) {
        JobPosition jp = getById(id);
        JobPositionForm f = new JobPositionForm();
        f.setJobId(jp.getJobId());
        f.setTitle(jp.getTitle());
        f.setJobLevel(jp.getJobLevel());
        f.setDescription(jp.getDescription());
        f.setActive(jp.getActive());
        return f;
    }

    @Override
    @Transactional
    public Integer create(JobPositionForm form) {
        // (Tuỳ bạn) chặn trùng title:
        if (repo.existsByTitleIgnoreCase(form.getTitle().trim())) {
            throw new RuntimeException("Title already exists!");
        }

        JobPosition jp = JobPosition.builder()
                .title(form.getTitle().trim())
                .jobLevel(form.getJobLevel())
                .description(form.getDescription())
                .active(true)
                .build();

        return repo.save(jp).getJobId();
    }

    @Override
    @Transactional
    public void update(Integer id, JobPositionForm form) {
        JobPosition jp = getById(id);

        String newTitle = form.getTitle().trim();
        // nếu đổi title, check trùng
        if (!jp.getTitle().equalsIgnoreCase(newTitle) && repo.existsByTitleIgnoreCase(newTitle)) {
            throw new RuntimeException("Title already exists!");
        }

        jp.setTitle(newTitle);
        jp.setJobLevel(form.getJobLevel());
        jp.setDescription(form.getDescription());
        // active không update ở form (mình tách action activate/deactivate)
        repo.save(jp);
    }

    @Override
    @Transactional
    public void setActive(Integer id, boolean active) {
        JobPosition jp = getById(id);
        jp.setActive(active);
        repo.save(jp);
    }
}