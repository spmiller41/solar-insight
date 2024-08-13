package com.solar_insight.app.solar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SolarCostCalculator {

    // Config
    private final double dcToAcEfficiency;
    private final double efficiencyDepreciationFactor;
    private final double costIncreaseFactor;
    private final double discountRate;
    private final double monthlyAverageEnergyBill;
    private final double panelCapacityWatts;
    private final double installationCostPerWatt;
    private final int installationLifeSpan;
    private final double energyCostPerKwh;

    // Values
    private int panelCount;
    private double yearlyProductionAcKwh;
    private double totalCostWithSolar;
    private double totalCostWithoutSolar;
    private double solarIncentives;
    private double savings;
    private int monthlyBillWithSolar;


    public SolarCostCalculator(int monthlyAverageEnergyBill, double energyCostPerKwh) {
        // Load Config
        this.dcToAcEfficiency = 0.97;
        this.efficiencyDepreciationFactor = 0.995;
        this.costIncreaseFactor = 1.022;
        this.discountRate = 1.04;
        this.panelCapacityWatts = 400;
        this.installationCostPerWatt = 4.0;
        this.installationLifeSpan = 20;
        this.energyCostPerKwh = energyCostPerKwh;
        this.monthlyAverageEnergyBill = monthlyAverageEnergyBill;
    }




    private void calculateAndStoreData() {
        // Basic settings



        // Solar configuration
        int panelsCount = 20;
        double yearlyEnergyDcKwh = 12000;


        // Solar installation
        double installationSizeKw = (panelsCount * panelCapacityWatts) / 1000.0;
        double installationCostTotal = installationCostPerWatt * installationSizeKw * 1000;
        double solarIncentives = installationCostTotal * .30;

        // Energy consumption
        double monthlyKwhEnergyConsumption = monthlyAverageEnergyBill / energyCostPerKwh;
        double yearlyKwhEnergyConsumption = monthlyKwhEnergyConsumption * 12;

        // Energy produced for installation life span
        double initialAcKwhPerYear = yearlyEnergyDcKwh * dcToAcEfficiency;
        List<Double> yearlyProductionAcKwh = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            yearlyProductionAcKwh.add(initialAcKwhPerYear * Math.pow(efficiencyDepreciationFactor, year));
        }

        // Cost with solar for installation life span
        List<Double> yearlyUtilityBillEstimates = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            double yearlyKwhEnergyProduced = yearlyProductionAcKwh.get(year);
            double billEnergyKwh = yearlyKwhEnergyConsumption - yearlyKwhEnergyProduced;
            double billEstimate = (billEnergyKwh * energyCostPerKwh * Math.pow(costIncreaseFactor, year)) / Math.pow(discountRate, year);
            yearlyUtilityBillEstimates.add(Math.max(billEstimate, 0));
        }

        double remainingLifetimeUtilityBill = yearlyUtilityBillEstimates.stream().mapToDouble(Double::doubleValue).sum();
        double totalCostWithSolar = installationCostTotal + remainingLifetimeUtilityBill - solarIncentives;
        System.out.printf("Cost with solar: $%.2f%n", totalCostWithSolar);

        // Cost without solar for installation life span
        List<Double> yearlyCostWithoutSolar = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            double costWithoutSolar = (monthlyAverageEnergyBill * 12 * Math.pow(costIncreaseFactor, year)) / Math.pow(discountRate, year);
            yearlyCostWithoutSolar.add(costWithoutSolar);
        }

        double totalCostWithoutSolar = yearlyCostWithoutSolar.stream().mapToDouble(Double::doubleValue).sum();
        System.out.printf("Cost without solar: $%.2f%n", totalCostWithoutSolar);

        // Savings with solar for installation life span
        double savings = totalCostWithoutSolar - totalCostWithSolar;
        System.out.printf("Savings: $%.2f in %d years%n", savings, installationLifeSpan);
    }




}
