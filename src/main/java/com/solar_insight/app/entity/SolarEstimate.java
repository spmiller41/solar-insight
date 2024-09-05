package com.solar_insight.app.entity;

import com.solar_insight.app.solar.utility.SolarOutcomeAnalysis;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="solar_estimates")
public class SolarEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "monthly_bill")
    private int monthlyBill;

    @Column(name = "estimated_savings")
    private int estimatedSavings;

    @Column(name = "system_size_dc")
    private int systemSizeDc;

    @Column(name = "incentives")
    private int incentives;

    @Column(name = "panel_count")
    private int panelCount;

    @Column(name = "annual_production_ac")
    private int annualProductionAc;

    @Column(name = "cost_without_solar")
    private int costWithoutSolar;

    @Column(name = "cost_with_solar")
    private int costWithSolar;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_updates")
    private int totalUpdates;

    @Column(name = "address_id")
    private int addressId;

    public SolarEstimate(SolarOutcomeAnalysis solarOutcomeAnalysis, Address address) {
        this.monthlyBill = solarOutcomeAnalysis.getMonthlyAverageEnergyBill();
        this.estimatedSavings = solarOutcomeAnalysis.getSavings();
        this.systemSizeDc = solarOutcomeAnalysis.getYearlyDcSystemSize();
        this.incentives = solarOutcomeAnalysis.getSolarIncentives();
        this.panelCount = solarOutcomeAnalysis.getPanelCount();
        this.annualProductionAc = (int) solarOutcomeAnalysis.getYearlyProductionAcKwh();
        this.costWithoutSolar = (int) solarOutcomeAnalysis.getTotalCostWithoutSolar();
        this.costWithSolar = (int) solarOutcomeAnalysis.getTotalCostWithSolar();
        this.updatedAt = LocalDateTime.now();
        this.addressId = address.getId();
    }

    public SolarEstimate() {}

    public void refreshSolarEstimate(SolarOutcomeAnalysis solarOutcomeAnalysis) {
        this.monthlyBill = solarOutcomeAnalysis.getMonthlyAverageEnergyBill();
        this.estimatedSavings = solarOutcomeAnalysis.getSavings();
        this.systemSizeDc = solarOutcomeAnalysis.getYearlyDcSystemSize();
        this.incentives = solarOutcomeAnalysis.getSolarIncentives();
        this.panelCount = solarOutcomeAnalysis.getPanelCount();
        this.annualProductionAc = (int) solarOutcomeAnalysis.getYearlyProductionAcKwh();
        this.costWithoutSolar = (int) solarOutcomeAnalysis.getTotalCostWithoutSolar();
        this.costWithSolar = (int) solarOutcomeAnalysis.getTotalCostWithSolar();
        this.updatedAt = LocalDateTime.now();
        this.totalUpdates++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMonthlyBill() {
        return monthlyBill;
    }

    public void setMonthlyBill(int monthlyBill) {
        this.monthlyBill = monthlyBill;
    }

    public int getEstimatedSavings() {
        return estimatedSavings;
    }

    public void setEstimatedSavings(int estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }

    public int getSystemSizeDc() {
        return systemSizeDc;
    }

    public void setSystemSizeDc(int systemSizeDc) {
        this.systemSizeDc = systemSizeDc;
    }

    public int getIncentives() {
        return incentives;
    }

    public void setIncentives(int incentives) {
        this.incentives = incentives;
    }

    public int getPanelCount() {
        return panelCount;
    }

    public void setPanelCount(int panelCount) {
        this.panelCount = panelCount;
    }

    public int getAnnualProductionAc() {
        return annualProductionAc;
    }

    public void setAnnualProductionAc(int annualProductionAc) {
        this.annualProductionAc = annualProductionAc;
    }

    public int getCostWithoutSolar() {
        return costWithoutSolar;
    }

    public void setCostWithoutSolar(int costWithoutSolar) {
        this.costWithoutSolar = costWithoutSolar;
    }

    public int getCostWithSolar() {
        return costWithSolar;
    }

    public void setCostWithSolar(int costWithSolar) {
        this.costWithSolar = costWithSolar;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getTotalUpdates() {
        return totalUpdates;
    }

    public void setTotalUpdates(int totalUpdates) {
        this.totalUpdates = totalUpdates;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    @Override
    public String toString() {
        return "SolarEstimate{" +
                "id=" + id +
                ", monthlyBill=" + monthlyBill +
                ", estimatedSavings=" + estimatedSavings +
                ", systemSizeDc=" + systemSizeDc +
                ", incentives=" + incentives +
                ", panelCount=" + panelCount +
                ", annualProductionAc=" + annualProductionAc +
                ", costWithoutSolar=" + costWithoutSolar +
                ", costWithSolar=" + costWithSolar +
                ", updatedAt=" + updatedAt +
                ", totalUpdates=" + totalUpdates +
                ", addressId=" + addressId +
                '}';
    }

}
