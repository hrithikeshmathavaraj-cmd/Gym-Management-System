package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.PlanRequest;
import com.gymmanagement.entity.Plan;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.PlanRepository;
import com.gymmanagement.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing membership plans (Monthly, Quarterly, Annual, etc).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    @Transactional
    public Plan createPlan(PlanRequest request) {
        Plan plan = Plan.builder()
                .planName(request.getPlanName())
                .duration(request.getDuration())
                .price(request.getPrice())
                .description(request.getDescription())
                .active(true)
                .build();
        Plan saved = planRepository.save(plan);
        log.info("Plan created: {}", saved.getPlanName());
        return saved;
    }

    @Override
    @Transactional
    public Plan updatePlan(String id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        plan.setPlanName(request.getPlanName());
        plan.setDuration(request.getDuration());
        plan.setPrice(request.getPrice());
        plan.setDescription(request.getDescription());

        Plan saved = planRepository.save(plan);
        log.info("Plan updated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deletePlan(String id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plan not found with id: " + id);
        }
        planRepository.deleteById(id);
        log.info("Plan deleted: {}", id);
    }

    @Override
    public Plan getPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }
}
