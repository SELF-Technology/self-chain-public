use std::sync::Arc;
use axum::{
    body::Body,
    http::{Request, StatusCode},
    middleware::Next,
    response::Response,
};
use tower::ServiceExt;
use crate::security::auth::{Claims, validate_token};
use crate::core::config::SecurityConfig;

pub async fn auth_middleware(
    request: Request<Body>,
    next: Next<Body>,
    config: Arc<SecurityConfig>,
) -> Result<Response, StatusCode> {
    // Get authorization header
    let auth_header = request
        .headers()
        .get("Authorization")
        .ok_or(StatusCode::UNAUTHORIZED)?
        .to_str()
        .map_err(|_| StatusCode::BAD_REQUEST)?;

    // Extract token
    let token = auth_header
        .strip_prefix("Bearer ")
        .ok_or(StatusCode::BAD_REQUEST)?;

    // Validate token
    validate_token(token, &config.jwt_secret)
        .map_err(|_| StatusCode::UNAUTHORIZED)?;

    // Proceed with request
    Ok(next.run(request).await)
}

pub async fn api_key_middleware(
    request: Request<Body>,
    next: Next<Body>,
    config: Arc<SecurityConfig>,
) -> Result<Response, StatusCode> {
    // Get API key from headers
    let api_key = request
        .headers()
        .get("X-API-Key")
        .ok_or(StatusCode::UNAUTHORIZED)?
        .to_str()
        .map_err(|_| StatusCode::BAD_REQUEST)?;

    // Validate API key length
    if api_key.len() != config.api_key_length {
        return Err(StatusCode::BAD_REQUEST);
    }

    // TODO: Validate API key against private repository
    // This would typically involve a secure communication with the private repo
    // For now, we'll just check if the key is valid format
    
    // Proceed with request
    Ok(next.run(request).await)
}

pub async fn rate_limit_middleware(
    request: Request<Body>,
    next: Next<Body>,
    config: Arc<SecurityConfig>,
) -> Result<Response, StatusCode> {
    // TODO: Implement rate limiting
    // This would typically use a distributed cache like Redis
    // For now, we'll just allow all requests
    Ok(next.run(request).await)
}
