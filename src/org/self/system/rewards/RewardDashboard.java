/**
 * Comprehensive dashboard for monitoring SELF Chain reward metrics and performance.
 * Provides both text-based and web-based visualizations of reward distributions,
 * validator performance, user participation, and validation statistics.
 * 
 * @author SELFHQ Development Team
 * @version 1.0
 */
package org.self.system.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.stream.Collectors;

import org.self.objects.ai.AICapacityNumber;
import org.self.objects.ai.AIData;
import org.self.system.governance.monitor.GovernanceMonitor;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;
import org.self.system.params.SELFParams;
import org.self.utils.SelfLogger;
import org.self.system.governance.ai.AIValidator;
import org.self.system.governance.points.PointSystem;

/**
 * Singleton class that manages the SELF Chain reward dashboard.
 * Provides real-time monitoring of reward metrics and generates both text and web-based reports.
 */
public class RewardDashboard {
    private static RewardDashboard instance;
    private RewardMetrics metrics;
    private RewardMonitor monitor;
    private GovernanceMonitor governanceMonitor;
    private Timer dashboardTimer;
    private long refreshPeriod;
    private Map<String, DashboardMetric> dashboardMetrics;
    private StakeCalculator stakeCalculator;
    private ParticipationRateCalculator participationCalculator;
    
    private RewardDashboard() {
        metrics = RewardMetrics.getInstance();
        monitor = RewardMonitor.getInstance();
        governanceMonitor = GovernanceMonitor.getInstance();
        stakeCalculator = StakeCalculator.getInstance();
        participationCalculator = ParticipationRateCalculator.getInstance();
        refreshPeriod = RewardParams.SELF_REWARD_DASHBOARD_REFRESH.toLong();
        initializeDashboardMetrics();
        initializeDashboard();
    }
    
    public static RewardDashboard getInstance() {
        if (instance == null) {
            instance = new RewardDashboard();
        }
        return instance;
    }
    
    private void initializeDashboardMetrics() {
        dashboardMetrics = new HashMap<>();
        
        // Validator metrics
        dashboardMetrics.put("validator_distribution", 
            new DashboardMetric("Validator Distribution", "validator", "distribution"));
            
        dashboardMetrics.put("validator_performance", 
            new DashboardMetric("Validator Performance", "validator", "performance"));
        
        // User metrics
        dashboardMetrics.put("user_distribution", 
            new DashboardMetric("User Distribution", "user", "distribution"));
        
        dashboardMetrics.put("user_performance", 
            new DashboardMetric("User Performance", "user", "performance"));
        
        // Validation metrics
        dashboardMetrics.put("validation_rate", 
            new DashboardMetric("Validation Rate", "hex", "validation"));
        
        dashboardMetrics.put("validation_amount", 
            new DashboardMetric("Validation Amount", "hex", "validation"));
        
        // Set up threshold monitoring
        setupThresholds();
    }

    private void setupThresholds() {
        // Validator distribution threshold
        monitor.addThreshold("validator_distribution", 
            new AICapacityNumber(RewardParams.SELF_REWARD_DASHBOARD_THRESHOLD_VALIDATOR_DISTRIBUTION));
        
        // User distribution threshold
        monitor.addThreshold("user_distribution", 
            new AICapacityNumber(RewardParams.SELF_REWARD_DASHBOARD_THRESHOLD_USER_DISTRIBUTION));
        
        // Validation rate threshold
        monitor.addThreshold("validation_rate", 
            new AICapacityNumber(RewardParams.SELF_REWARD_DASHBOARD_THRESHOLD_VALIDATION_RATE));
        
        // Validator performance threshold
        monitor.addThreshold("validator_performance", 
            RewardParams.SELF_REWARD_THRESHOLD_VALIDATOR_PERFORMANCE.toDouble());
        
        // User performance threshold
        monitor.addThreshold("user_performance", 
            RewardParams.SELF_REWARD_THRESHOLD_USER_PERFORMANCE.toDouble());
    }
    
    private void initializeDashboard() {
        dashboardTimer = new Timer();
        dashboardTimer.schedule(new DashboardTask(), 0, refreshPeriod);
    }
    
    private class DashboardTask extends TimerTask {
        @Override
        public void run() {
            generateDashboardReport();
            checkAlerts();
        }
    }
    
    public String generateDashboardReport() {
        StringBuilder report = new StringBuilder("=== SELF REWARD DASHBOARD ===\n\n");
        
        // Add validator metrics
        report.append("=== Validator Metrics ===\n");
        addMetricToReport("validator_distribution");
        addMetricToReport("validator_performance");
        
        // Add user metrics
        report.append("\n=== User Metrics ===\n");
        addMetricToReport("user_distribution");
        addMetricToReport("user_performance");
        
        // Add validation metrics
        report.append("\n=== Validation Metrics ===\n");
        addMetricToReport("validation_rate");
        addMetricToReport("validation_amount");
        
        // Add alerts
        report.append("\n=== Alerts ===\n");
        addAlertsToReport();
        
        // Log the report
        SelfLogger.log(report.toString());
        
        // Send to governance monitor
        governanceMonitor.updateDashboard(report.toString());
    }
    
    private void addMetricToReport(String zMetricID) {
        DashboardMetric metric = dashboardMetrics.get(zMetricID);
        if (metric != null) {
            String value = getMetricValue(metric);
            String status = getMetricStatus(metric);
            
            StringBuilder line = new StringBuilder();
            line.append(String.format("%s: %s", metric.getName(), value));
            
            if (!status.equals("normal")) {
                line.append(String.format(" (%s)", status.toUpperCase()));
            }
            
            line.append("\n");
            
            SelfLogger.log(line.toString());
        }
    }
    
    private String getMetricValue(DashboardMetric zMetric) {
        if (zMetric.getType().equals("distribution")) {
            RewardDistribution dist = metrics.getDistributions().get(zMetric.getCategory());
            if (dist != null) {
                return String.format(
                    "Total: %s, Avg: %.2f, Max: %s, Min: %s",
                    dist.getTotalAmount().toString(),
                    dist.getAverageAmount(),
                    dist.getMaxAmount().toString(),
                    dist.getMinAmount().toString()
                );
            }
        } else if (zMetric.getType().equals("performance")) {
            RewardPerformance perf = metrics.getPerformance().get(zMetric.getCategory());
            if (perf != null) {
                return String.format(
                    "Total: %.2f, Avg: %.2f, Max: %.2f, Min: %.2f",
                    perf.getTotalScore(),
                    perf.getAverageScore(),
                    perf.getMaxScore(),
                    perf.getMinScore()
                );
            }
        } else if (zMetric.getType().equals("validation")) {
            RewardValidation val = metrics.getValidations().get(zMetric.getCategory());
            if (val != null) {
                return String.format(
                    "Rate: %.2f%%, Total: %s, Validated: %s",
                    val.getValidationRate(),
                    val.getTotalAmount().toString(),
                    val.getValidatedAmount().toString()
                );
            }
        }
        return "N/A";
    }

    public String getMetricStatus(DashboardMetric zMetric) {
        AlertThreshold threshold = monitor.getThreshold(zMetric.getCategory() + "_" + zMetric.getType());
        if (threshold != null && threshold.isEnabled()) {
            Object value = getMetricValueObject(zMetric);
            if (value instanceof AICapacityNumber) {
                AICapacityNumber numValue = (AICapacityNumber) value;
                return threshold.isValueWithinThreshold(numValue.getAsDouble()) ? "normal" : "alert";
            } else if (value instanceof Double) {
                Double doubleValue = (Double) value;
                return threshold.isValueWithinThreshold(doubleValue) ? "normal" : "alert";
            }
        }
        return "normal";
    }

    private Object getMetricValueObject(DashboardMetric zMetric) {
        if (zMetric.getType().equals("distribution")) {
            RewardDistribution dist = metrics.getDistributions().get(zMetric.getCategory());
            if (dist != null) {
                return dist.getAverageAmount();
            }
        } else if (zMetric.getType().equals("performance")) {
            RewardPerformance perf = metrics.getPerformance().get(zMetric.getCategory());
            if (perf != null) {
                return perf.getAverageScore();
            }
        } else if (zMetric.getType().equals("validation")) {
            RewardValidation val = metrics.getValidations().get(zMetric.getCategory());
            if (val != null) {
                return val.getValidationRate();
            }
        }
        return null;
    }

    private void addAlertsToReport() {
        StringBuilder alerts = new StringBuilder();
        alerts.append("No active alerts\n");
        
        // Check all metrics for alerts
        for (DashboardMetric metric : dashboardMetrics.values()) {
            if (getMetricStatus(metric).equals("alert")) {
                alerts.append(String.format("ALERT: %s is outside threshold\n", metric.getName()));
            }
        }
        SelfLogger.log(alerts.toString());
    }

    private void checkAlerts() {
        for (DashboardMetric metric : dashboardMetrics.values()) {
            if (!getMetricStatus(metric).equals("normal")) {
                SelfLogger.log(String.format("ALERT: %s is outside threshold", metric.getName()));
            }
        }
    }

    private void resetDashboard() {
        if (dashboardTimer != null) {
            dashboardTimer.cancel();
        }
        instance = null;
    }

    public String generateWebDashboard() {
        StringBuilder html = new StringBuilder();
        
        // Header
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>SELF Chain Reward Dashboard</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".section { margin-bottom: 30px; }");
        html.append(".chart { width: " + RewardParams.DASHBOARD_CHART_WIDTH.toString() + "px; ");
        html.append("height: " + RewardParams.DASHBOARD_CHART_HEIGHT.toString() + "px; }");
        html.append(".alert { color: red; font-weight: bold; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Distribution Metrics
        html.append("<div class=\"section\">");
        html.append("<h2>Reward Distribution</h2>");
        html.append(generateDistributionChart());
        html.append(generateDistributionTable());
        html.append("</div>");
        
        // Performance Metrics
        html.append("<div class=\"section\">");
        html.append("<h2>Performance Metrics</h2>");
        html.append(generatePerformanceChart());
        html.append(generatePerformanceTable());
        html.append("</div>");
        
        // Validator Stats
        html.append("<div class=\"section\">");
        html.append("<h2>Validator Statistics</h2>");
        html.append(generateValidatorTable());
        html.append("</div>");
        
        // User Stats
        html.append("<div class=\"section\">");
        html.append("<h2>User Statistics</h2>");
        html.append(generateUserTable());
        html.append("</div>");
        
        // Footer
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateDistributionChart() {
        StringBuilder chart = new StringBuilder();
        
        chart.append("<div class=\"chart\">");
        chart.append("<canvas id=\"distributionChart\"></canvas>");
        chart.append("</div>");
        chart.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>");
        chart.append("<script>");
        chart.append("const ctx = document.getElementById(\"distributionChart\").getContext(\"2d\");");
        chart.append("new Chart(ctx, {");
        chart.append("    type: 'pie',");
        chart.append("    data: {");
        chart.append("        labels: ['User', 'Validator'],");
        chart.append("        datasets: [{");
        chart.append("            data: [");
        
        // Get distribution data
        Map<String, RewardDistribution> distributions = metrics.getDistributions();
        AICapacityNumber userDist = distributions.get("user").getTotalAmount();
        AICapacityNumber validatorDist = distributions.get("validator").getTotalAmount();
        
        chart.append(userDist.getAsDouble()).append(", ");
        chart.append(validatorDist.getAsDouble()).append("],");
        chart.append("            backgroundColor: ['rgba(54, 162, 235, 0.2)', 'rgba(255, 99, 132, 0.2)']");
        chart.append("        }]");
        chart.append("    },");
        chart.append("    options: {");
        chart.append("        responsive: true");
        chart.append("    }");
        chart.append("});");
        chart.append("</script>");
        
        return chart.toString();
    }

    private String generateDistributionTable() {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<tr><th>Type</th><th>Total Amount</th><th>Average</th><th>Max</th><th>Min</th></tr>");
        
        Map<String, RewardDistribution> distributions = metrics.getDistributions();
        for (Map.Entry<String, RewardDistribution> entry : distributions.entrySet()) {
            RewardDistribution dist = entry.getValue();
            table.append(String.format(
                "<tr><td>%s</td><td>%.2f</td><td>%.2f</td><td>%.2f</td><td>%.2f</td></tr>", 
                entry.getKey(),
                dist.getTotalAmount().getAsDouble(),
                dist.getAverageAmount(),
                dist.getMaxAmount().getAsDouble(),
                dist.getMinAmount().getAsDouble()
            ));
        }
        table.append("</table>");
        return table.toString();
    }

    private String generatePerformanceChart() {
        StringBuilder chart = new StringBuilder();
        
        chart.append("<div class=\"chart\">");
        chart.append("<canvas id=\"performanceChart\"></canvas>");
        chart.append("</div>");
        chart.append("<script>");
        chart.append("const ctx = document.getElementById(\"performanceChart\").getContext(\"2d\");");
        chart.append("new Chart(ctx, {");
        chart.append("    type: 'line',");
        chart.append("    data: {");
        chart.append("        labels: ['User', 'Validator'],");
        chart.append("        datasets: [{");
        chart.append("            data: [");
        
        // Get performance data
        Map<String, RewardPerformance> performance = metrics.getPerformance();
        double userPerformance = performance.get("user").getAverageScore();
        double validatorPerformance = performance.get("validator").getAverageScore();
        
        chart.append(userPerformance).append(", ");
        chart.append(validatorPerformance).append("],");
        chart.append("            borderColor: 'rgba(75, 192, 192, 1)'");
        chart.append("        }]");
        chart.append("    },");
        chart.append("    options: {");
        chart.append("        responsive: true");
        chart.append("    }");
        chart.append("});");
        chart.append("</script>");
        
        return chart.toString();
    }

    private String generatePerformanceTable() {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<tr><th>Metric</th><th>Average Score</th><th>Max Score</th><th>Min Score</th></tr>");
        
        Map<String, RewardPerformance> performance = metrics.getPerformance();
        for (Map.Entry<String, RewardPerformance> entry : performance.entrySet()) {
            RewardPerformance perf = entry.getValue();
            table.append(String.format(
                "<tr><td>%s</td><td>%.2f</td><td>%.2f</td><td>%.2f</td></tr>", 
                entry.getKey(),
                perf.getAverageScore(),
                perf.getMaxScore(),
                perf.getMinScore()
            ));
        }
        table.append("</table>");
        return table.toString();
    }

    public String generateValidatorTable() {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<tr><th>Validator ID</th><th>Stake</th><th>Reputation</th><th>Hex Score</th></tr>");
        
        List<AIValidator> validators = AIValidator.getValidators();
        for (AIValidator validator : validators) {
            AICapacityNumber stake = stakeCalculator.calculateValidatorStake(validator.getValidatorID());
            double reputation = validator.getReputation();
            double hexScore = validator.getHexValidationScore();
            
            table.append(String.format(
                "<tr><td>%s</td><td>%.2f</td><td>%.2f</td><td>%.2f</td></tr>", 
                validator.getValidatorID().toString(),
                stake.getAsDouble(),
                reputation,
                hexScore
            ));
        }
        table.append("</table>");
        return table.toString();
    }

    public String generateUserTable() {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<tr><th>User ID</th><th>Points</th><th>Participation Rate</th><th>Stake</th></tr>");
        
        PointSystem pointSystem = PointSystem.getInstance();
        Map<AIData, AICapacityNumber> userPoints = pointSystem.getUserPoints();
        
        for (Map.Entry<AIData, AICapacityNumber> entry : userPoints.entrySet()) {
            AIData userID = entry.getKey();
            AICapacityNumber points = entry.getValue();
            double participationRate = participationCalculator.calculateParticipationRate(userID).getAsDouble();
            AICapacityNumber stake = stakeCalculator.calculateUserStake(userID);
            
            table.append(String.format(
                "<tr><td>%s</td><td>%.2f</td><td>%.2f%%</td><td>%.2f</td></tr>", 
                userID.toString(),
                points.getAsDouble(),
                participationRate * 100,
                stake.getAsDouble()
            ));
        }
        table.append("</table>");
        return table.toString();
    }
}
