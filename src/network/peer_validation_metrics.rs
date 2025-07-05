use prometheus::{register_int_counter, register_int_gauge, register_histogram, IntCounter, IntGauge, Histogram, HistogramOpts};
use std::sync::Arc;
use std::time::Duration;

pub struct PeerValidationMetrics {
    // Validation Metrics
    pub validations_performed: IntCounter,
    pub validations_success: IntCounter,
    pub validations_failed: IntCounter,
    pub validation_latency: Histogram,
    pub validation_score: Histogram,
    
    // Certificate Validation Metrics
    pub certificate_validations: IntCounter,
    pub certificate_validation_errors: IntCounter,
    pub certificate_latency: Histogram,
    
    // Reputation Metrics
    pub reputation_checks: IntCounter,
    pub reputation_failures: IntCounter,
    pub reputation_score: Histogram,
    
    // Response Time Metrics
    pub response_time_checks: IntCounter,
    pub response_time_failures: IntCounter,
    pub response_time: Histogram,
    
    // Error Metrics
    pub validation_errors: IntCounter,
    pub validation_error_types: Histogram,
    
    // Validation Window Metrics
    pub validation_window: IntGauge,
    pub validation_interval: IntGauge,
    
    // Validation Cache Metrics
    pub cache_hits: IntCounter,
    pub cache_misses: IntCounter,
    pub cache_size: IntGauge,
}

impl PeerValidationMetrics {
    pub fn new() -> Self {
        let validation_latency = register_histogram!(
            "peer_validation_latency_seconds",
            "Time taken for peer validation",
            vec![0.1, 0.5, 1.0, 2.0, 5.0, 10.0]
        ).unwrap();

        let validation_score = register_histogram!(
            "peer_validation_score",
            "Peer validation score (0-1)",
            vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
        ).unwrap();

        let certificate_latency = register_histogram!(
            "peer_certificate_validation_latency_seconds",
            "Time taken for certificate validation",
            vec![0.01, 0.05, 0.1, 0.5, 1.0]
        ).unwrap();

        let response_time = register_histogram!(
            "peer_response_time_seconds",
            "Peer response time",
            vec![0.01, 0.05, 0.1, 0.5, 1.0]
        ).unwrap();

        let validation_error_types = register_histogram!(
            "peer_validation_error_type",
            "Type of validation error",
            vec![0.0, 1.0, 2.0, 3.0, 4.0] // 0=none, 1=cert, 2=reputation, 3=response, 4=other
        ).unwrap();

        Self {
            validations_performed: register_int_counter!(
                "peer_validations_performed",
                "Number of peer validations performed"
            ).unwrap(),
            validations_success: register_int_counter!(
                "peer_validations_success",
                "Number of successful peer validations"
            ).unwrap(),
            validations_failed: register_int_counter!(
                "peer_validations_failed",
                "Number of failed peer validations"
            ).unwrap(),
            validation_latency,
            validation_score,
            certificate_validations: register_int_counter!(
                "peer_certificate_validations",
                "Number of certificate validations"
            ).unwrap(),
            certificate_validation_errors: register_int_counter!(
                "peer_certificate_validation_errors",
                "Number of certificate validation errors"
            ).unwrap(),
            certificate_latency,
            reputation_checks: register_int_counter!(
                "peer_reputation_checks",
                "Number of reputation checks"
            ).unwrap(),
            reputation_failures: register_int_counter!(
                "peer_reputation_failures",
                "Number of reputation check failures"
            ).unwrap(),
            reputation_score: register_histogram!(
                "peer_reputation_score",
                "Peer reputation score (0-1)",
                vec![0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
            ).unwrap(),
            response_time_checks: register_int_counter!(
                "peer_response_time_checks",
                "Number of response time checks"
            ).unwrap(),
            response_time_failures: register_int_counter!(
                "peer_response_time_failures",
                "Number of response time check failures"
            ).unwrap(),
            response_time,
            validation_errors: register_int_counter!(
                "peer_validation_errors",
                "Number of validation errors"
            ).unwrap(),
            validation_error_types,
            validation_window: register_int_gauge!(
                "peer_validation_window_seconds",
                "Validation window duration"
            ).unwrap(),
            validation_interval: register_int_gauge!(
                "peer_validation_interval_seconds",
                "Validation interval"
            ).unwrap(),
            cache_hits: register_int_counter!(
                "peer_validation_cache_hits",
                "Number of cache hits"
            ).unwrap(),
            cache_misses: register_int_counter!(
                "peer_validation_cache_misses",
                "Number of cache misses"
            ).unwrap(),
            cache_size: register_int_gauge!(
                "peer_validation_cache_size",
                "Size of validation cache"
            ).unwrap(),
        }
    }

    pub fn observe_validation_latency(&self, duration: f64) {
        self.validation_latency.observe(duration);
    }

    pub fn observe_validation_score(&self, score: f64) {
        self.validation_score.observe(score);
    }

    pub fn observe_certificate_latency(&self, duration: f64) {
        self.certificate_latency.observe(duration);
    }

    pub fn observe_response_time(&self, duration: f64) {
        self.response_time.observe(duration);
    }

    pub fn observe_validation_error_type(&self, error_type: u64) {
        self.validation_error_types.observe(error_type as f64);
    }

    pub fn increment_validations_performed(&self) {
        self.validations_performed.inc();
    }

    pub fn increment_validations_success(&self) {
        self.validations_success.inc();
    }

    pub fn increment_validations_failed(&self) {
        self.validations_failed.inc();
    }

    pub fn increment_certificate_validations(&self) {
        self.certificate_validations.inc();
    }

    pub fn increment_certificate_validation_errors(&self) {
        self.certificate_validation_errors.inc();
    }

    pub fn increment_reputation_checks(&self) {
        self.reputation_checks.inc();
    }

    pub fn increment_reputation_failures(&self) {
        self.reputation_failures.inc();
    }

    pub fn increment_response_time_checks(&self) {
        self.response_time_checks.inc();
    }

    pub fn increment_response_time_failures(&self) {
        self.response_time_failures.inc();
    }

    pub fn increment_validation_errors(&self) {
        self.validation_errors.inc();
    }

    pub fn set_validation_window(&self, duration: Duration) {
        self.validation_window.set(duration.as_secs() as i64);
    }

    pub fn set_validation_interval(&self, duration: Duration) {
        self.validation_interval.set(duration.as_secs() as i64);
    }

    pub fn increment_cache_hits(&self) {
        self.cache_hits.inc();
    }

    pub fn increment_cache_misses(&self) {
        self.cache_misses.inc();
    }

    pub fn set_cache_size(&self, size: usize) {
        self.cache_size.set(size as i64);
    }
}
