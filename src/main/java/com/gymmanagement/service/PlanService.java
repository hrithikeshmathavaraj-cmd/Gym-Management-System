package com.gymmanagement.service;

import com.gymmanagement.entity.Plan;
import com.gymmanagement.dto.PlanRequest;

import java.util.List;

public interface PlanService {
    Plan createPlan(PlanRequest request);
    Plan updatePlan(String id, PlanRequest request);
    void deletePlan(String id);
    Plan getPlanById(String id);
    List<Plan> getAllPlans();
}
