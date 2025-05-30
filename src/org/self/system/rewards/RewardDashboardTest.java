package org.self.system.rewards;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.self.objects.MiniNumber;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;

public class RewardDashboardTest {
    private RewardDashboard dashboard;
    private RewardMetrics metrics;
    private PointSystem pointSystem;
    private AIValidator validator;

    @Before
    public void setUp() {
        // Initialize test instances
        metrics = RewardMetrics.getInstance();
        pointSystem = PointSystem.getInstance();
        validator = new AIValidator();
        
        // Initialize dashboard
        dashboard = RewardDashboard.getInstance();
        
        // Set up test data
        setupTestData();
    }

    private void setupTestData() {
        // Add test distributions
        metrics.addDistribution("user", MiniNumber.valueOf(1000));
        metrics.addDistribution("validator", MiniNumber.valueOf(500));
        
        // Add test performance scores
        metrics.addPerformanceScore("user", 0.85);
        metrics.addPerformanceScore("validator", 0.92);
        
        // Add test validation data
        metrics.addValidationRate(0.95);
        metrics.addValidationAmount(MiniNumber.valueOf(10000));
    }

    @Test
    public void testGenerateDashboardReport() {
        String report = dashboard.generateDashboardReport();
        assertTrue(report.contains("=== SELF REWARD DASHBOARD ==="));
        assertTrue(report.contains("=== Validator Metrics ==="));
        assertTrue(report.contains("=== User Metrics ==="));
        assertTrue(report.contains("=== Validation Metrics ==="));
    }

    @Test
    public void testGenerateWebDashboard() {
        String html = dashboard.generateWebDashboard();
        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("<title>SELF Chain Reward Dashboard</title>"));
        assertTrue(html.contains("<canvas id=\"distributionChart\""));
        assertTrue(html.contains("<canvas id=\"performanceChart\""));
    }

    @Test
    public void testMetricStatus() {
        // Test with normal values
        DashboardMetric metric = new DashboardMetric("Test Metric", "user", "distribution");
        String status = dashboard.getMetricStatus(metric);
        assertEquals("normal", status);
    }

    @Test
    public void testValidatorTable() {
        String table = dashboard.generateValidatorTable();
        assertTrue(table.contains("<table>"));
        assertTrue(table.contains("<th>Validator ID</th>"));
        assertTrue(table.contains("<th>Stake</th>"));
        assertTrue(table.contains("<th>Reputation</th>"));
        assertTrue(table.contains("<th>Hex Score</th>"));
    }

    @Test
    public void testUserTable() {
        String table = dashboard.generateUserTable();
        assertTrue(table.contains("<table>"));
        assertTrue(table.contains("<th>User ID</th>"));
        assertTrue(table.contains("<th>Points</th>"));
        assertTrue(table.contains("<th>Participation Rate</th>"));
        assertTrue(table.contains("<th>Stake</th>"));
    }
}
