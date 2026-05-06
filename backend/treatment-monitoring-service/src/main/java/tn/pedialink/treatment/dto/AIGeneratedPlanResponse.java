package tn.pedialink.treatment.dto;

import lombok.Builder;
import lombok.Data;
import tn.pedialink.treatment.entity.NutritionalPlan;
import java.util.List;

@Data
@Builder
public class AIGeneratedPlanResponse {
    private NutritionalPlan plan;
    private double dailyCalories;
    private int ageInMonths;
    private Double bmi;
    private List<String> recommendations;
}
