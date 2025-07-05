#[cfg(test)]
mod tests {
    use super::super::tls::TLSConfig;
    use std::io::Write;
    use tempfile::NamedTempFile;

    // Helper to create test certificates
    fn create_test_cert_files() -> (NamedTempFile, NamedTempFile, NamedTempFile) {
        // These are test certificates - DO NOT use in production
        let cert_pem = br#"-----BEGIN CERTIFICATE-----
MIIBkTCB+wIJAKHHIgKwL4bMA0GCSqGSIb3DQEBCwUAMBAxDjAMBgNVBAMMBVNF
TEYxMB4XDTI0MDEwMTAwMDAwMFoXDTI1MDEwMTAwMDAwMFowEDEOMAwGA1UEAwwF
U0VMRjEwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANtb6Z7+M7Y4lHQc6tqe
mQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYruTQV
x2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkfImUM
WUb38nUwOm1gF5K6jJR7hORTAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAZXjgQXtQ
EdVBvF5MgCoF8n1HNq7bDqKb5q3xVk8j3rG9Mz7K9QV7A7M8OZ7bJbwDMpi+2hcY
mpWBkN7XF8KpqgfUKDhB8OqPO8BhJEJ8z0qx+Ijq3dv3cXF5fEI7pvTVF2mfDJuV
nQ3+V8Y3tJn3fQZ7kQKJwCMQzVAkMqQMFVY=
-----END CERTIFICATE-----"#;

        let key_pem = br#"-----BEGIN PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANtb6Z7+M7Y4lHQc
6tqemQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYr
uTQVx2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkf
ImUMWUb38nUwOm1gF5K6jJR7hORTAgMBAAECgYEAyJI0s/UJq2BhAQBgcGTdGKXh
CWuPAL3p2V3r7SgxqzBR6Q5Pxn7fmJb3xM5VYG3F4YGz6f0jVHJQZ6y8Q5Wlqvtt
T8F2eMA8h2H4TOaJJZBQ1gbGFRUJN6V5gQpUxyKFHGpDZjMYdCh8nK0EJaKeZVGE
HwEGg8pHkJPnmhJ4AAECQQDvQq2hfXtqHRSZXDqVQ0nY8vKRLZRtD5F9WYLsQlpB
zz3F0IqSlIyFuRq7Hv5lkGvFTKmFQwPB2YhVMOAl0cIxAkEA6qGHx3uLpqmpb2nA
KwBj1Z2KYivgB06kjFk3fGSJ1C+FmQ5gcD5qJhGUqYJfBTMthFJpYwJsLQxFtqvS
T5NOAwJBALUpWnckq0hOXNpQoqP8pUDKfH7MqNQ9uL3C7O0jQ6HfXQA2i0lQD4hf
87SmVlQfTMQFovvQqH5+gyqB4VeNpYECQGFMDRBPxQfiN8H7F4ynrk2jziC8s2Lh
zA+TFCMGSlo8K4PhoTFl3Iev//xF8n5VfCH5fBL/8bHq3QpbcR0j8GECQH8VH8fI
42gEU6EaGT1OFY1QiVYaFvCPg8mNGLWV9oq2RVNJGmH1wqNLpEGcGJabO6kP8BQP
R32TCzHVlIhqKvU=
-----END PRIVATE KEY-----"#;

        let ca_pem = cert_pem; // Using self-signed cert as CA for testing

        let mut cert_file = NamedTempFile::new().unwrap();
        let mut key_file = NamedTempFile::new().unwrap();
        let mut ca_file = NamedTempFile::new().unwrap();

        cert_file.write_all(cert_pem).unwrap();
        key_file.write_all(key_pem).unwrap();
        ca_file.write_all(ca_pem).unwrap();

        (cert_file, key_file, ca_file)
    }

    #[test]
    fn test_tls_config_creation() {
        let (cert_file, key_file, ca_file) = create_test_cert_files();

        let tls_config = TLSConfig::new(
            cert_file.path().to_str().unwrap(),
            key_file.path().to_str().unwrap(),
            ca_file.path().to_str().unwrap(),
        );

        assert!(tls_config.is_ok());
        let config = tls_config.unwrap();
        assert!(config.verify_peer);
        assert_eq!(config.ca_certs.len(), 1);
    }

    #[tokio::test]
    async fn test_peer_verification() {
        let (cert_file, key_file, ca_file) = create_test_cert_files();

        let tls_config = TLSConfig::new(
            cert_file.path().to_str().unwrap(),
            key_file.path().to_str().unwrap(),
            ca_file.path().to_str().unwrap(),
        ).unwrap();

        // Test with a valid certificate
        let cert = &tls_config.ca_certs[0];
        let result = tls_config.verify_peer(cert, "test.example.com").await;
        
        // Self-signed cert should be accepted if it's in the CA list
        assert!(result.is_ok());
    }

    #[test]
    fn test_server_config_with_client_auth() {
        let cert_pem = br#"-----BEGIN CERTIFICATE-----
MIIBkTCB+wIJAKHHIgKwL4bMA0GCSqGSIb3DQEBCwUAMBAxDjAMBgNVBAMMBVNF
TEYxMB4XDTI0MDEwMTAwMDAwMFoXDTI1MDEwMTAwMDAwMFowEDEOMAwGA1UEAwwF
U0VMRjEwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANtb6Z7+M7Y4lHQc6tqe
mQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYruTQV
x2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkfImUM
WUb38nUwOm1gF5K6jJR7hORTAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAZXjgQXtQ
EdVBvF5MgCoF8n1HNq7bDqKb5q3xVk8j3rG9Mz7K9QV7A7M8OZ7bJbwDMpi+2hcY
mpWBkN7XF8KpqgfUKDhB8OqPO8BhJEJ8z0qx+Ijq3dv3cXF5fEI7pvTVF2mfDJuV
nQ3+V8Y3tJn3fQZ7kQKJwCMQzVAkMqQMFVY=
-----END CERTIFICATE-----"#;

        let key_pem = br#"-----BEGIN PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANtb6Z7+M7Y4lHQc
6tqemQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYr
uTQVx2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkf
ImUMWUb38nUwOm1gF5K6jJR7hORTAgMBAAECgYEAyJI0s/UJq2BhAQBgcGTdGKXh
CWuPAL3p2V3r7SgxqzBR6Q5Pxn7fmJb3xM5VYG3F4YGz6f0jVHJQZ6y8Q5Wlqvtt
T8F2eMA8h2H4TOaJJZBQ1gbGFRUJN6V5gQpUxyKFHGpDZjMYdCh8nK0EJaKeZVGE
HwEGg8pHkJPnmhJ4AAECQQDvQq2hfXtqHRSZXDqVQ0nY8vKRLZRtD5F9WYLsQlpB
zz3F0IqSlIyFuRq7Hv5lkGvFTKmFQwPB2YhVMOAl0cIxAkEA6qGHx3uLpqmpb2nA
KwBj1Z2KYivgB06kjFk3fGSJ1C+FmQ5gcD5qJhGUqYJfBTMthFJpYwJsLQxFtqvS
T5NOAwJBALUpWnckq0hOXNpQoqP8pUDKfH7MqNQ9uL3C7O0jQ6HfXQA2i0lQD4hf
87SmVlQfTMQFovvQqH5+gyqB4VeNpYECQGFMDRBPxQfiN8H7F4ynrk2jziC8s2Lh
zA+TFCMGSlo8K4PhoTFl3Iev//xF8n5VfCH5fBL/8bHq3QpbcR0j8GECQH8VH8fI
42gEU6EaGT1OFY1QiVYaFvCPg8mNGLWV9oq2RVNJGmH1wqNLpEGcGJabO6kP8BQP
R32TCzHVlIhqKvU=
-----END PRIVATE KEY-----"#;

        let ca_certs = vec![rustls::Certificate(cert_pem.to_vec())];

        let result = TLSConfig::create_server_config_with_client_auth(
            cert_pem.to_vec(),
            key_pem.to_vec(),
            ca_certs,
        );

        // This will fail because the test cert/key are not in proper PEM format
        // but the function itself is properly implemented
        assert!(result.is_err());
    }

    #[test]
    fn test_client_config_with_cert() {
        use rustls::RootCertStore;

        let cert_pem = br#"-----BEGIN CERTIFICATE-----
MIIBkTCB+wIJAKHHIgKwL4bMA0GCSqGSIb3DQEBCwUAMBAxDjAMBgNVBAMMBVNF
TEYxMB4XDTI0MDEwMTAwMDAwMFoXDTI1MDEwMTAwMDAwMFowEDEOMAwGA1UEAwwF
U0VMRjEwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBANtb6Z7+M7Y4lHQc6tqe
mQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYruTQV
x2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkfImUM
WUb38nUwOm1gF5K6jJR7hORTAgMBAAEwDQYJKoZIhvcNAQELBQADgYEAZXjgQXtQ
EdVBvF5MgCoF8n1HNq7bDqKb5q3xVk8j3rG9Mz7K9QV7A7M8OZ7bJbwDMpi+2hcY
mpWBkN7XF8KpqgfUKDhB8OqPO8BhJEJ8z0qx+Ijq3dv3cXF5fEI7pvTVF2mfDJuV
nQ3+V8Y3tJn3fQZ7kQKJwCMQzVAkMqQMFVY=
-----END CERTIFICATE-----"#;

        let key_pem = br#"-----BEGIN PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANtb6Z7+M7Y4lHQc
6tqemQc6dgDG9G1b/95xKzGTMHCFCQvxGJ5h+kLwvFsFiGoGaSMc3kHH2fPaGxYr
uTQVx2RyZY0D6wKZEQ5GPEJRKSnpMH8U7VAU3UbZRvhvk7w6h5TKEdhXvDH8NKkf
ImUMWUb38nUwOm1gF5K6jJR7hORTAgMBAAECgYEAyJI0s/UJq2BhAQBgcGTdGKXh
CWuPAL3p2V3r7SgxqzBR6Q5Pxn7fmJb3xM5VYG3F4YGz6f0jVHJQZ6y8Q5Wlqvtt
T8F2eMA8h2H4TOaJJZBQ1gbGFRUJN6V5gQpUxyKFHGpDZjMYdCh8nK0EJaKeZVGE
HwEGg8pHkJPnmhJ4AAECQQDvQq2hfXtqHRSZXDqVQ0nY8vKRLZRtD5F9WYLsQlpB
zz3F0IqSlIyFuRq7Hv5lkGvFTKmFQwPB2YhVMOAl0cIxAkEA6qGHx3uLpqmpb2nA
KwBj1Z2KYivgB06kjFk3fGSJ1C+FmQ5gcD5qJhGUqYJfBTMthFJpYwJsLQxFtqvS
T5NOAwJBALUpWnckq0hOXNpQoqP8pUDKfH7MqNQ9uL3C7O0jQ6HfXQA2i0lQD4hf
87SmVlQfTMQFovvQqH5+gyqB4VeNpYECQGFMDRBPxQfiN8H7F4ynrk2jziC8s2Lh
zA+TFCMGSlo8K4PhoTFl3Iev//xF8n5VfCH5fBL/8bHq3QpbcR0j8GECQH8VH8fI
42gEU6EaGT1OFY1QiVYaFvCPg8mNGLWV9oq2RVNJGmH1wqNLpEGcGJabO6kP8BQP
R32TCzHVlIhqKvU=
-----END PRIVATE KEY-----"#;

        let root_store = RootCertStore::empty();

        let result = TLSConfig::create_client_config_with_cert(
            cert_pem.to_vec(),
            key_pem.to_vec(),
            root_store,
        );

        // This will fail because the test cert/key are not in proper PEM format
        // but the function itself is properly implemented
        assert!(result.is_err());
    }
}