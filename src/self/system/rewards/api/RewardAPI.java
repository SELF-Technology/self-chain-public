package org.self.system.rewards.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.self.objects.MiniNumber;
import org.self.system.rewards.RewardDashboard;
import org.self.system.rewards.RewardMetrics;
import org.self.system.rewards.RewardTrendTracker;
import org.self.system.rewards.RewardVisualization;
import org.self.system.rewards.RewardRecord;
import org.self.system.rewards.DashboardMetric;
import org.self.system.rewards.TrendDataPoint;
import org.self.system.rewards.VisualizationData;
import org.self.system.rewards.RewardDistribution;
import org.self.system.rewards.RewardPerformance;
import org.self.system.rewards.RewardValidation;

import spark.Request;
import spark.Response;
import spark.Route;
import com.google.gson.Gson;

public class RewardAPI {
    private static final Gson gson = new Gson();
    private static final RewardMetrics metrics = RewardMetrics.getInstance();
    private static final RewardTrendTracker trendTracker = RewardTrendTracker.getInstance();
    private static final RewardVisualization visualization = RewardVisualization.getInstance();
    private static final RewardDashboard_new dashboard = RewardDashboard_new.getInstance();
    
    public static void initializeAPI() {
        // Metrics endpoints
        spark.Spark.get("/api/rewards/metrics", getMetrics());
        spark.Spark.get("/api/rewards/metrics/:type", getMetricType());
        
        // Trend endpoints
        spark.Spark.get("/api/rewards/trends", getTrends());
        spark.Spark.get("/api/rewards/trends/:metric", getTrend());
        
        // Visualization endpoints
        spark.Spark.get("/api/rewards/visualization", getVisualizations());
        spark.Spark.get("/api/rewards/visualization/:metric", getVisualization());
        
        // Dashboard endpoints
        spark.Spark.get("/api/rewards/dashboard", getDashboard());
        spark.Spark.get("/api/rewards/dashboard/metrics", getDashboardMetrics());
        
        // Reward records endpoints
        spark.Spark.get("/api/rewards/records", getRewardRecords());
        spark.Spark.get("/api/rewards/records/:type", getRewardRecordsByType());
    }
    
    private static Route getMetrics() {
        return (Request req, Response res) -> {
            Map<String, RewardDistribution> distributions = metrics.getDistributions();
            Map<String, RewardPerformance> performance = metrics.getPerformance();
            Map<String, RewardValidation> validations = metrics.getValidations();
            
            Map<String, Object> response = new HashMap<>();
            response.put("distributions", distributions);
            response.put("performance", performance);
            response.put("validations", validations);
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getMetricType() {
        return (Request req, Response res) -> {
            String type = req.params(":type");
            Map<String, Object> response = new HashMap<>();
            
            switch (type.toLowerCase()) {
                case "distribution":
                    response.put("distributions", metrics.getDistributions());
                    break;
                case "performance":
                    response.put("performance", metrics.getPerformance());
                    break;
                case "validation":
                    response.put("validations", metrics.getValidations());
                    break;
                default:
                    res.status(400);
                    return "Invalid metric type";
            }
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getTrends() {
        return (Request req, Response res) -> {
            Map<String, List<TrendDataPoint>> trends = trendTracker.getTrendData();
            Map<String, Object> response = new HashMap<>();
            response.put("trends", trends);
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getTrend() {
        return (Request req, Response res) -> {
            String metric = req.params(":metric");
            List<TrendDataPoint> trend = trendTracker.getTrendData(metric);
            
            if (trend == null) {
                res.status(404);
                return "Metric not found";
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("metric", metric);
            response.put("trend", trend);
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getVisualizations() {
        return (Request req, Response res) -> {
            Map<String, VisualizationData> visualizations = visualization.getVisualizations();
            Map<String, Object> response = new HashMap<>();
            response.put("visualizations", visualizations);
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getVisualization() {
        return (Request req, Response res) -> {
            String metric = req.params(":metric");
            VisualizationData visualization = visualization.getVisualization(metric);
            
            if (visualization == null) {
                res.status(404);
                return "Metric not found";
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("metric", metric);
            response.put("visualization", visualization);
            
            res.type("application/json");
            return gson.toJson(response);
        };
    }
    
    private static Route getDashboard() {
        return (Request req, Response res) -> {
            Map<String, Object> dashboardData = dashboard.getDashboardData();
            res.type("application/json");
            return gson.toJson(dashboardData);
        };
    }
    
    private static Route getDashboardMetrics() {
        return (Request req, Response res) -> {
            List<DashboardMetric> metrics = dashboard.getDashboardMetrics();
            res.type("application/json");
            return gson.toJson(metrics);
        };
    }
    
    private static Route getRewardRecords() {
        return (Request req, Response res) -> {
            List<RewardRecord> records = metrics.getRewardRecords();
            res.type("application/json");
            return gson.toJson(records);
        };
    }
    
    private static Route getRewardRecordsByType() {
        return (Request req, Response res) -> {
            String type = req.params(":type");
            List<RewardRecord> records = metrics.getRewardRecordsByType(type);
            
            if (records == null) {
                res.status(404);
                return "Type not found";
            }
            
            res.type("application/json");
            return gson.toJson(records);
        };
    }
}
