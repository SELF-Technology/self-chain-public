package org.self.system.rewards.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@OpenAPIDefinition(
    info = @Info(
        title = "SELF Chain Reward System API",
        version = "1.0",
        description = "API documentation for the SELF Chain reward system",
        license = @License(name = "MIT License")
    ),
    servers = {
        @Server(
            url = "http://localhost:4567",
            description = "Local development server"
        )
    }
)
public class RewardAPIDocs {
    
    // Metrics endpoints
    @Operation(
        summary = "Get all reward metrics",
        description = "Retrieve comprehensive reward metrics including distributions, performance, and validations",
        tags = {"metrics"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved metrics",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class MetricsEndpoint {}
    
    @Operation(
        summary = "Get specific metric type",
        description = "Retrieve metrics of a specific type",
        tags = {"metrics"},
        parameters = {
            @Parameter(
                name = "type",
                description = "Type of metric to retrieve (distribution, performance, validation)",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved metric type",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid metric type"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class MetricTypeEndpoint {}
    
    // Trend endpoints
    @Operation(
        summary = "Get all reward trends",
        description = "Retrieve trend data for all metrics",
        tags = {"trends"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved trends",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class TrendsEndpoint {}
    
    @Operation(
        summary = "Get trend data for specific metric",
        description = "Retrieve trend data for a specific metric",
        tags = {"trends"},
        parameters = {
            @Parameter(
                name = "metric",
                description = "Name of the metric to retrieve trend data for",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved trend data",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Metric not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class TrendEndpoint {}
    
    // Visualization endpoints
    @Operation(
        summary = "Get all visualizations",
        description = "Retrieve all reward visualizations",
        tags = {"visualization"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved visualizations",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class VisualizationsEndpoint {}
    
    @Operation(
        summary = "Get visualization for specific metric",
        description = "Retrieve visualization data for a specific metric",
        tags = {"visualization"},
        parameters = {
            @Parameter(
                name = "metric",
                description = "Name of the metric to retrieve visualization for",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved visualization",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Metric not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class VisualizationEndpoint {}
    
    // Dashboard endpoints
    @Operation(
        summary = "Get dashboard data",
        description = "Retrieve comprehensive dashboard data",
        tags = {"dashboard"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved dashboard data",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class DashboardEndpoint {}
    
    @Operation(
        summary = "Get dashboard metrics",
        description = "Retrieve dashboard metrics configuration",
        tags = {"dashboard"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved dashboard metrics",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class DashboardMetricsEndpoint {}
    
    // Reward records endpoints
    @Operation(
        summary = "Get all reward records",
        description = "Retrieve all reward distribution records",
        tags = {"records"},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved reward records",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class RewardRecordsEndpoint {}
    
    @Operation(
        summary = "Get reward records by type",
        description = "Retrieve reward records filtered by type",
        tags = {"records"},
        parameters = {
            @Parameter(
                name = "type",
                description = "Type of reward records to retrieve",
                required = true
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved reward records",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Type not found"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    public static class RewardRecordsByTypeEndpoint {}
}
