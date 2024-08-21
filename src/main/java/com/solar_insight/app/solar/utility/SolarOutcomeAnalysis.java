package com.solar_insight.app.solar.utility;

import java.util.ArrayList;
import java.util.List;

public class SolarCostCalculator {

    // Config
    private final double dcToAcEfficiency;
    private final double efficiencyDepreciationFactor;
    private final double costIncreaseFactor;
    private final double monthlyAverageEnergyBill;
    private final double panelCapacityWatts;
    private final double installationCostPerWatt;
    private final int installationLifeSpan;
    private final double energyCostPerKwh;
    private final int panelCount;
    private final double yearlyEnergyDcKwh;
    private final double interestRate;

    // Calculated Metrics
    private double yearlyProductionAcKwh;
    private double totalCostWithSolar;
    private double totalCostWithoutSolar;
    private double solarIncentives;
    private double savings;
    private int monthlyBillWithSolar;

    public SolarCostCalculator(int monthlyAverageEnergyBill, double energyCostPerKwh, int panelCount, double yearlyEnergyDcKwh) {
        // Store Predefined Config
        this.dcToAcEfficiency = SolarConfig.DC_TO_AC_EFFICIENCY;
        this.efficiencyDepreciationFactor = SolarConfig.EFFICIENCY_DEPRECIATION_FACTOR;
        this.costIncreaseFactor = SolarConfig.COST_INCREASE_FACTOR;
        this.panelCapacityWatts = SolarConfig.PANEL_CAPACITY_WATTS;
        this.installationCostPerWatt = SolarConfig.INSTALLATION_COST_PER_WATT;
        this.installationLifeSpan = SolarConfig.INSTALLATION_LIFE_SPAN;
        this.interestRate = SolarConfig.INTEREST_RATE;

        // Store Parameters
        this.energyCostPerKwh = energyCostPerKwh;
        this.monthlyAverageEnergyBill = monthlyAverageEnergyBill;
        this.panelCount = panelCount;
        this.yearlyEnergyDcKwh = yearlyEnergyDcKwh;

        // Set Data
        calculateAndStoreData();
    }

    public double getYearlyProductionAcKwh() { return yearlyProductionAcKwh; }

    public double getTotalCostWithSolar() { return totalCostWithSolar; }

    public double getTotalCostWithoutSolar() { return totalCostWithoutSolar; }

    public double getSavings() { return savings; }

    public int getMonthlyBillWithSolar() { return monthlyBillWithSolar; }

    public double getSolarIncentives() { return solarIncentives; }


    /*
     * Calculates and stores the key financial metrics related to solar installation:
     *
     * - Installation cost and size
     * - Yearly utility bills with solar
     * - Yearly costs without solar
     * - Solar incentives and total costs
     * - Savings and monthly bill with solar
     * - Yearly energy production
     */
    private void calculateAndStoreData() {
        double installationSizeKw = (panelCount * panelCapacityWatts) / 1000.0;
        double installationCostTotal = installationCostPerWatt * installationSizeKw * 1000;
        installationCostTotal = (interestRate * installationCostTotal) + installationCostTotal;


        List<Double> yearlyUtilityBillEstimates = calculateYearlyUtilityBillEstimates();
        double remainingLifetimeUtilityBill = yearlyUtilityBillEstimates.stream().mapToDouble(Double::doubleValue).sum();

        List<Double> yearlyCostWithoutSolar = calculateYearlyCostWithoutSolar();

        this.solarIncentives = installationCostTotal * .30;
        this.totalCostWithSolar = installationCostTotal + remainingLifetimeUtilityBill - solarIncentives;
        this.totalCostWithoutSolar = yearlyCostWithoutSolar.stream().mapToDouble(Double::doubleValue).sum();
        this.savings = totalCostWithoutSolar - totalCostWithSolar + solarIncentives;
        this.monthlyBillWithSolar = (int) (totalCostWithSolar / (12 * installationLifeSpan));
        this.yearlyProductionAcKwh = yearlyEnergyDcKwh * dcToAcEfficiency;
    }


    /*
     * Calculates the estimated yearly costs without solar installation over the lifespan.
     *
     * @return A list of yearly costs without solar over the installation lifespan.
     */
    private List<Double> calculateYearlyCostWithoutSolar() {
        List<Double> yearlyCostWithoutSolar = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            double costWithoutSolar = (monthlyAverageEnergyBill * 12 * Math.pow(costIncreaseFactor, year));
            yearlyCostWithoutSolar.add(costWithoutSolar);
        }
        return yearlyCostWithoutSolar;
    }


    /*
     * Calculates the estimated yearly utility bills with solar installation over the lifespan.
     * It first calculates the annual energy consumption and production, then delegates the
     * bill estimation to the calculateYearlyBillEstimate method.
     *
     * @return A list of yearly utility bill estimates over the installation lifespan.
     */
    private List<Double> calculateYearlyUtilityBillEstimates() {
        double monthlyKwhEnergyConsumption = monthlyAverageEnergyBill / energyCostPerKwh;
        double yearlyKwhEnergyConsumption = monthlyKwhEnergyConsumption * 12;

        // Energy produced for installation life span
        double initialAcKwhPerYear = yearlyEnergyDcKwh * dcToAcEfficiency;
        List<Double> yearlyProductionAcKwh = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            yearlyProductionAcKwh.add(initialAcKwhPerYear * Math.pow(efficiencyDepreciationFactor, year));
        }

        // Cost with solar for installation life span
        return calculateYearlyBillEstimate(yearlyProductionAcKwh, yearlyKwhEnergyConsumption);
    }


    /*
     * Estimates the utility bill for each year by subtracting solar energy production from consumption,
     * then calculating the cost based on energy prices adjusted for inflation.
     *
     * @param yearlyProductionAcKwh List of yearly AC energy production values over the installation lifespan.
     * @param yearlyKwhEnergyConsumption Annual energy consumption in kWh.
     * @return A list of estimated yearly utility bills after solar installation.
     */
    private List<Double> calculateYearlyBillEstimate(List<Double> yearlyProductionAcKwh, double yearlyKwhEnergyConsumption) {
        List<Double> yearlyUtilityBillEstimates = new ArrayList<>();
        for (int year = 0; year < installationLifeSpan; year++) {
            double yearlyKwhEnergyProduced = yearlyProductionAcKwh.get(year);
            double billEnergyKwh = yearlyKwhEnergyConsumption - yearlyKwhEnergyProduced;
            double billEstimate = (billEnergyKwh * energyCostPerKwh * Math.pow(costIncreaseFactor, year));
            yearlyUtilityBillEstimates.add(Math.max(billEstimate, 0));
        }
        return yearlyUtilityBillEstimates;
    }


    @Override
    public String toString() {
        return "SolarCostCalculator{" +
                "yearlyProductionAcKwh=" + yearlyProductionAcKwh +
                ", totalCostWithSolar=" + totalCostWithSolar +
                ", totalCostWithoutSolar=" + totalCostWithoutSolar +
                ", solarIncentives=" + solarIncentives +
                ", savings=" + savings +
                ", monthlyBillWithSolar=" + monthlyBillWithSolar +
                '}';
    }


}