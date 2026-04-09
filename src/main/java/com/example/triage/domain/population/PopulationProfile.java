package com.example.triage.domain.population;

import java.util.List;

public record PopulationProfile(String gender, Integer age, AgeGroup ageGroup, List<String> crowdTags) {
}
