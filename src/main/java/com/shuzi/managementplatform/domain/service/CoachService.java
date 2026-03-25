package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.Coach;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.web.dto.coach.CoachCreateRequest;
import com.shuzi.managementplatform.web.dto.coach.CoachResponse;
import com.shuzi.managementplatform.web.dto.coach.CoachUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Coach domain service for CRUD and course-name synchronization.
 */
@Service
public class CoachService {

    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;

    public CoachService(CoachMapper coachMapper, CourseMapper courseMapper) {
        this.coachMapper = coachMapper;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public CoachResponse create(CoachCreateRequest request) {
        ensureCoachCodeUnique(request.coachCode(), null);
        ensureCoachNameUnique(request.name(), null);

        Coach coach = new Coach();
        coach.setCoachCode(request.coachCode().trim());
        apply(coach, request.name(), request.gender(), request.phone(), request.specialty(), request.status(), request.remarks());
        coachMapper.insert(coach);
        return toResponse(coach);
    }

    @Transactional
    public CoachResponse update(Long id, CoachUpdateRequest request) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            throw new ResourceNotFoundException("coach not found: " + id);
        }

        String oldName = coach.getName();
        ensureCoachNameUnique(request.name(), id);

        apply(coach, request.name(), request.gender(), request.phone(), request.specialty(), request.status(), request.remarks());
        coachMapper.updateById(coach);

        if (!oldName.equals(coach.getName())) {
            syncCourseCoachName(oldName, coach.getName());
        }
        return toResponse(coach);
    }

    @Transactional
    public void delete(Long id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            throw new ResourceNotFoundException("coach not found: " + id);
        }

        Long referencedCount = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getCoachName, coach.getName())
        );
        if (referencedCount != null && referencedCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "coach is referenced by courses");
        }

        coachMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CoachResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public IPage<CoachResponse> page(String name, CoachStatus status, int page, int size) {
        Page<Coach> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<Coach> query = Wrappers.<Coach>lambdaQuery();

        if (StringUtils.hasText(name)) {
            query.like(Coach::getName, name.trim());
        }
        if (status != null) {
            query.eq(Coach::getStatus, status);
        }
        query.orderByDesc(Coach::getId);

        Page<Coach> result = coachMapper.selectPage(pageRequest, query);
        List<CoachResponse> records = result.getRecords().stream().map(this::toResponse).toList();

        Page<CoachResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Transactional(readOnly = true)
    public List<CoachResponse> listActive() {
        return coachMapper.selectList(
                        Wrappers.<Coach>lambdaQuery()
                                .eq(Coach::getStatus, CoachStatus.ACTIVE)
                                .orderByAsc(Coach::getName, Coach::getId)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Coach getEntityById(Long id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            throw new ResourceNotFoundException("coach not found: " + id);
        }
        return coach;
    }

    private void apply(
            Coach coach,
            String name,
            Gender gender,
            String phone,
            String specialty,
            CoachStatus status,
            String remarks
    ) {
        coach.setName(name.trim());
        coach.setGender(gender);
        coach.setPhone(normalize(phone));
        coach.setSpecialty(normalize(specialty));
        coach.setStatus(status == null ? CoachStatus.ACTIVE : status);
        coach.setRemarks(normalize(remarks));
    }

    private void ensureCoachCodeUnique(String coachCode, Long excludeId) {
        LambdaQueryWrapper<Coach> query = Wrappers.<Coach>lambdaQuery().eq(Coach::getCoachCode, coachCode.trim());
        if (excludeId != null) {
            query.ne(Coach::getId, excludeId);
        }
        Long count = coachMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "coachCode already exists");
        }
    }

    private void ensureCoachNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Coach> query = Wrappers.<Coach>lambdaQuery().eq(Coach::getName, name.trim());
        if (excludeId != null) {
            query.ne(Coach::getId, excludeId);
        }
        Long count = coachMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "coach name already exists");
        }
    }

    private void syncCourseCoachName(String oldName, String newName) {
        List<Course> courses = courseMapper.selectList(
                Wrappers.<Course>lambdaQuery().eq(Course::getCoachName, oldName)
        );
        for (Course course : courses) {
            course.setCoachName(newName);
            courseMapper.updateById(course);
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private CoachResponse toResponse(Coach coach) {
        return new CoachResponse(
                coach.getId(),
                coach.getCoachCode(),
                coach.getName(),
                coach.getGender(),
                coach.getPhone(),
                coach.getSpecialty(),
                coach.getStatus(),
                coach.getRemarks(),
                coach.getCreatedAt(),
                coach.getUpdatedAt()
        );
    }
}
