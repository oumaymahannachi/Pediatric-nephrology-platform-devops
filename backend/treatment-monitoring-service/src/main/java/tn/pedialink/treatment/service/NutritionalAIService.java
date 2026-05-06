package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.dto.AIGeneratedPlanRequest;
import tn.pedialink.treatment.dto.AIGeneratedPlanResponse;
import tn.pedialink.treatment.entity.*;
import tn.pedialink.treatment.h2.service.TreatmentH2BackupService;
import tn.pedialink.treatment.repository.DietaryRestrictionRepository;
import tn.pedialink.treatment.repository.GrowthMeasurementRepository;
import tn.pedialink.treatment.repository.NutritionalPlanRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NutritionalAIService {

    private final GrowthMeasurementRepository measurementRepo;
    private final DietaryRestrictionRepository restrictionRepo;
    private final NutritionalPlanRepository planRepo;
    private final TreatmentH2BackupService h2BackupService;

    public AIGeneratedPlanResponse generateNutritionalPlan(AIGeneratedPlanRequest request) {
        List<GrowthMeasurement> measurements = measurementRepo.findByChildIdOrderByDateDesc(request.getChildId());
        GrowthMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);

        List<DietaryRestriction> restrictions = restrictionRepo.findByChildId(request.getChildId());

        int ageInMonths = calculateAgeInMonths(request.getDateOfBirth());
        int ageInYears = ageInMonths / 12;

        double dailyCalories = calculateDailyCalories(
            ageInMonths,
            latestMeasurement != null ? latestMeasurement.getWeight() : request.getWeight(),
            latestMeasurement != null ? latestMeasurement.getHeight() : request.getHeight(),
            request.getGender()
        );

        List<Meal> meals = generateMeals(dailyCalories, ageInMonths, restrictions);

        String goals = generateGoals(latestMeasurement, ageInMonths, request.getGender());
        String description = generateDescription(ageInYears, dailyCalories, restrictions);

        NutritionalPlan plan = new NutritionalPlan();
        plan.setChildId(request.getChildId());
        plan.setTitle("AI Generated Plan - " + LocalDate.now());
        plan.setDescription(description);
        plan.setStartDate(LocalDate.now().toString());
        plan.setEndDate(LocalDate.now().plusMonths(1).toString());
        plan.setGoals(goals);
        plan.setMeals(meals);
        plan.setStatus("ACTIVE");
        plan.setDoctorId("AI_SYSTEM");

        NutritionalPlan savedPlan = planRepo.save(plan);
        h2BackupService.backupNutritionalPlan(savedPlan);

        return AIGeneratedPlanResponse.builder()
            .plan(savedPlan)
            .dailyCalories(dailyCalories)
            .ageInMonths(ageInMonths)
            .bmi(latestMeasurement != null ? latestMeasurement.getBmi() : null)
            .recommendations(generateRecommendations(latestMeasurement, ageInMonths, restrictions))
            .build();
    }

    private int calculateAgeInMonths(LocalDate dateOfBirth) {
        Period period = Period.between(dateOfBirth, LocalDate.now());
        return period.getYears() * 12 + period.getMonths();
    }

    private double calculateDailyCalories(int ageInMonths, double weight, double height, String gender) {
        double bmr;
        int ageInYears = ageInMonths / 12;

        if (ageInYears < 3) {
            bmr = gender.equalsIgnoreCase("Male")
                ? (59.512 * weight - 30.4)
                : (58.317 * weight - 31.1);
        } else if (ageInYears < 10) {
            bmr = gender.equalsIgnoreCase("Male")
                ? (22.706 * weight + 504.3)
                : (20.315 * weight + 485.9);
        } else {
            bmr = gender.equalsIgnoreCase("Male")
                ? (13.384 * weight + 692.6)
                : (17.686 * weight + 658.2);
        }

        double activityFactor = 1.5;
        return bmr * activityFactor;
    }

    private List<Meal> generateMeals(double dailyCalories, int ageInMonths, List<DietaryRestriction> restrictions) {
        List<Meal> meals = new ArrayList<>();
        Set<String> allergens = new HashSet<>();

        for (DietaryRestriction restriction : restrictions) {
            allergens.add(restriction.getAllergen().toLowerCase());
        }

        double breakfastCal = dailyCalories * 0.25;
        double snack1Cal = dailyCalories * 0.10;
        double lunchCal = dailyCalories * 0.35;
        double snack2Cal = dailyCalories * 0.10;
        double dinnerCal = dailyCalories * 0.20;

        meals.add(createMeal("Breakfast", "07:30", breakfastCal, ageInMonths, allergens));
        meals.add(createMeal("Morning Snack", "10:00", snack1Cal, ageInMonths, allergens));
        meals.add(createMeal("Lunch", "12:30", lunchCal, ageInMonths, allergens));
        meals.add(createMeal("Afternoon Snack", "15:30", snack2Cal, ageInMonths, allergens));
        meals.add(createMeal("Dinner", "18:30", dinnerCal, ageInMonths, allergens));

        return meals;
    }

    private Meal createMeal(String name, String time, double calories, int ageInMonths, Set<String> allergens) {
        Meal meal = new Meal();
        meal.setName(name);
        meal.setTime(time);
        meal.setCalories(String.valueOf((int) Math.round(calories)));
        meal.setDescription(generateMealDescription(name, ageInMonths, allergens));
        return meal;
    }

    private String generateMealDescription(String mealType, int ageInMonths, Set<String> allergens) {
        Map<String, List<String>> mealOptions = new HashMap<>();

        if (mealType.equals("Breakfast")) {
            mealOptions.put("base", Arrays.asList(
                allergens.contains("gluten") ? "Rice porridge" : "Oatmeal",
                allergens.contains("dairy") ? "Soy yogurt" : "Greek yogurt",
                allergens.contains("gluten") ? "Rice cereal" : "Whole grain cereal"
            ));
            mealOptions.put("protein", Arrays.asList(
                allergens.contains("eggs") ? "Tofu scramble" : "Scrambled eggs",
                allergens.contains("dairy") ? "Almond butter" : "Cheese"
            ));
            mealOptions.put("fruit", Arrays.asList("Banana", "Berries", "Apple slices"));
        } else if (mealType.equals("Lunch")) {
            mealOptions.put("protein", Arrays.asList(
                "Grilled chicken breast",
                allergens.contains("fish") ? "Lean beef" : "Baked salmon",
                "Turkey meatballs"
            ));
            mealOptions.put("carbs", Arrays.asList(
                allergens.contains("gluten") ? "Brown rice" : "Whole wheat pasta",
                "Sweet potato", "Quinoa"
            ));
            mealOptions.put("vegetables", Arrays.asList("Steamed broccoli", "Carrot sticks", "Mixed vegetables"));
        } else if (mealType.equals("Dinner")) {
            mealOptions.put("protein", Arrays.asList(
                allergens.contains("fish") ? "Chicken" : "White fish",
                "Lean ground turkey", "Lentils"
            ));
            mealOptions.put("carbs", Arrays.asList(
                "Mashed potatoes",
                allergens.contains("gluten") ? "Rice" : "Whole grain bread",
                "Couscous"
            ));
            mealOptions.put("vegetables", Arrays.asList("Green beans", "Peas", "Spinach"));
        } else {
            return generateSnackDescription(ageInMonths, allergens);
        }

        StringBuilder description = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : mealOptions.entrySet()) {
            List<String> options = entry.getValue();
            description.append(options.get(new Random().nextInt(options.size())));
            description.append(", ");
        }

        return description.toString().replaceAll(", $", "");
    }

    private String generateSnackDescription(int ageInMonths, Set<String> allergens) {
        List<String> snacks = new ArrayList<>();

        if (!allergens.contains("dairy")) {
            snacks.add("Yogurt with fruit");
            snacks.add("Cheese cubes");
        }
        if (!allergens.contains("nuts")) {
            snacks.add("Apple slices with peanut butter");
            snacks.add("Trail mix");
        }

        snacks.add("Fresh fruit");
        snacks.add("Vegetable sticks with hummus");
        snacks.add("Whole grain crackers");

        return snacks.get(new Random().nextInt(snacks.size()));
    }

    private String generateGoals(GrowthMeasurement measurement, int ageInMonths, String gender) {
        StringBuilder goals = new StringBuilder();

        if (measurement != null && measurement.getBmi() != null) {
            double bmi = measurement.getBmi();
            if (bmi < 14) {
                goals.append("Increase weight gradually through balanced nutrition. ");
            } else if (bmi > 20) {
                goals.append("Maintain healthy weight through portion control. ");
            } else {
                goals.append("Maintain current healthy weight. ");
            }
        }

        goals.append("Ensure adequate intake of vitamins and minerals. ");
        goals.append("Promote healthy eating habits. ");
        goals.append("Support optimal growth and development.");

        return goals.toString();
    }

    private String generateDescription(int ageInYears, double dailyCalories, List<DietaryRestriction> restrictions) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("Personalized nutritional plan for %d year old child. ", ageInYears));
        desc.append(String.format("Daily caloric target: %.0f calories. ", dailyCalories));

        if (!restrictions.isEmpty()) {
            desc.append("Special considerations: ");
            for (DietaryRestriction r : restrictions) {
                desc.append(r.getAllergen()).append(" (").append(r.getType()).append("), ");
            }
            desc.setLength(desc.length() - 2);
            desc.append(". ");
        }

        desc.append("This plan is generated by AI and should be reviewed by a healthcare professional.");

        return desc.toString();
    }

    private List<String> generateRecommendations(GrowthMeasurement measurement, int ageInMonths, List<DietaryRestriction> restrictions) {
        List<String> recommendations = new ArrayList<>();

        if (ageInMonths < 24) {
            recommendations.add("Ensure adequate iron intake for brain development");
            recommendations.add("Include calcium-rich foods for bone growth");
        } else if (ageInMonths < 60) {
            recommendations.add("Encourage variety in food choices");
            recommendations.add("Limit sugary snacks and beverages");
        } else {
            recommendations.add("Promote balanced meals with all food groups");
            recommendations.add("Encourage regular meal times");
        }

        if (measurement != null && measurement.getBmi() != null) {
            double bmi = measurement.getBmi();
            if (bmi < 14) {
                recommendations.add("Consider nutrient-dense foods to support weight gain");
            } else if (bmi > 20) {
                recommendations.add("Focus on portion control and physical activity");
            }
        }

        if (!restrictions.isEmpty()) {
            recommendations.add("Ensure adequate nutrition despite dietary restrictions");
            recommendations.add("Consider supplementation if needed (consult doctor)");
        }

        recommendations.add("Stay hydrated with water throughout the day");
        recommendations.add("Regular monitoring of growth parameters recommended");

        return recommendations;
    }
}
