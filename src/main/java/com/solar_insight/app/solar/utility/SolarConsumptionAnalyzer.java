package com.solar_insight.app.solar.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SolarConsumptionAnalyzer {

    private final double pricePerKwh;
    private int annualAcKwhNeeded;

    public SolarConsumptionAnalyzer(double costOfElectricityWithoutSolar, double initialAcKwhPerYear, double solarPercentage) {
        this.pricePerKwh = calculateCurrentPricePerKWh(
                costOfElectricityWithoutSolar, initialAcKwhPerYear, solarPercentage);
    }

    private double calculateCurrentPricePerKWh(double costOfElectricityWithoutSolar,
                                               double initialAcKwhPerYear, double solarPercentage) {
        // Constants - Based on Google Solar calculations.
        final double COST_INCREASE_FACTOR = 0.022; // 2.2%
        final int YEARS = 20; // 20 years

        // Calculate the total annual consumption without solar
        double totalEnergyConsumptionKWh = initialAcKwhPerYear / (solarPercentage / 100.0);

        // Calculate the Present Value of the total cost by reversing inflation
        double presentValueTotalCost = costOfElectricityWithoutSolar / Math.pow(1 + COST_INCREASE_FACTOR, YEARS);

        // Total energy consumption over the specified number of years
        double totalEnergyConsumptionOverYears = totalEnergyConsumptionKWh * YEARS;

        // Current price per kWh
        return new BigDecimal(presentValueTotalCost / totalEnergyConsumptionOverYears)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public void setAnnualAcKwhNeeded(int monthlyBill) {
        this.annualAcKwhNeeded = (int) ((monthlyBill * 12) / pricePerKwh);
    }

    public int getAnnualAcKwhNeeded() {
        return this.annualAcKwhNeeded;
    }

    public double getPricePerKwh() {
        return this.pricePerKwh;
    }

}
