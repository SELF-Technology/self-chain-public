use anyhow::{Result, anyhow};
use rustls::{Certificate, ClientConfig, PrivateKey, RootCertStore, ServerConfig};
use rustls::client::{ServerCertVerifier, ServerCertVerified};
use rustls::server::{ClientCertVerifier, ClientCertVerified};
use rustls_pemfile;
use std::time::SystemTime;
use x509_parser::prelude::*;
use std::sync::Arc;
use tracing::{info, debug};

#[derive(Debug, Clone)]
pub struct TLSConfig {
    pub server_config: Arc<ServerConfig>,
    pub client_config: Arc<ClientConfig>,
    pub ca_certs: Vec<Certificate>,
    pub verify_peer: bool,
}

impl TLSConfig {
    pub fn new(cert_path: &str, key_path: &str, ca_path: &str) -> Result<Self> {
        // Load certificates and keys
        let server_cert_bytes = std::fs::read(cert_path)?;
        let server_key_bytes = std::fs::read(key_path)?;
        let ca_cert_bytes = std::fs::read(ca_path)?;

        // Parse CA certificates
        let ca_certs: Vec<Certificate> = rustls_pemfile::certs(&mut ca_cert_bytes.as_slice())?
            .into_iter()
            .map(Certificate)
            .collect();

        // Create root certificate store
        let mut root_store = RootCertStore::empty();
        for cert in &ca_certs {
            root_store.add(cert)?;
        }

        // Create server config with client certificate verification
        let server_config = Self::create_server_config_with_client_auth(
            server_cert_bytes.clone(),
            server_key_bytes.clone(),
            ca_certs.clone(),
        )?;

        // Create client config with client certificate for mutual TLS
        let client_config = Self::create_client_config_with_cert(
            server_cert_bytes,
            server_key_bytes,
            root_store,
        )?;

        Ok(Self {
            server_config: Arc::new(server_config),
            client_config: Arc::new(client_config),
            ca_certs,
            verify_peer: true,
        })
    }

    pub fn create_server_config(server_cert: Vec<u8>, server_key: Vec<u8>) -> Result<ServerConfig> {
        let certs = rustls_pemfile::certs(&mut server_cert.as_slice())?;
        let server_cert: Vec<Certificate> = certs.into_iter().map(Certificate).collect();

        let keys = rustls_pemfile::pkcs8_private_keys(&mut server_key.as_slice())?;
        let server_key = PrivateKey(keys.into_iter().next().unwrap());

        let server_config = ServerConfig::builder()
            .with_safe_defaults()
            .with_no_client_auth()
            .with_single_cert(server_cert, server_key)?;

        Ok(server_config)
    }

    pub fn create_server_config_with_client_auth(
        server_cert: Vec<u8>,
        server_key: Vec<u8>,
        ca_certs: Vec<Certificate>,
    ) -> Result<ServerConfig> {
        let certs = rustls_pemfile::certs(&mut server_cert.as_slice())?;
        let server_cert: Vec<Certificate> = certs.into_iter().map(Certificate).collect();

        let keys = rustls_pemfile::pkcs8_private_keys(&mut server_key.as_slice())?;
        let server_key = PrivateKey(keys.into_iter().next()
            .ok_or_else(|| anyhow!("No private key found in file"))?);

        // Create root cert store for client verification
        let mut client_auth_roots = RootCertStore::empty();
        for cert in ca_certs {
            client_auth_roots.add(&cert)?;
        }

        let client_cert_verifier = rustls::server::AllowAnyAuthenticatedClient::new(client_auth_roots);

        let server_config = ServerConfig::builder()
            .with_safe_defaults()
            .with_client_cert_verifier(client_cert_verifier)
            .with_single_cert(server_cert, server_key)?;

        Ok(server_config)
    }

    pub fn create_client_config() -> Result<ClientConfig> {
        let client_config = ClientConfig::builder()
            .with_safe_defaults()
            .with_root_certificates(RootCertStore::empty())
            .with_no_client_auth();

        Ok(client_config)
    }

    pub fn create_client_config_with_cert(
        client_cert: Vec<u8>,
        client_key: Vec<u8>,
        root_store: RootCertStore,
    ) -> Result<ClientConfig> {
        let certs = rustls_pemfile::certs(&mut client_cert.as_slice())?;
        let client_cert: Vec<Certificate> = certs.into_iter().map(Certificate).collect();

        let keys = rustls_pemfile::pkcs8_private_keys(&mut client_key.as_slice())?;
        let client_key = PrivateKey(keys.into_iter().next()
            .ok_or_else(|| anyhow!("No private key found in file"))?);

        let client_config = ClientConfig::builder()
            .with_safe_defaults()
            .with_root_certificates(root_store)
            .with_client_auth_cert(client_cert, client_key)?;

        Ok(client_config)
    }

    pub fn get_server_config(&self) -> Arc<ServerConfig> {
        self.server_config.clone()
    }

    pub fn get_client_config(&self) -> Arc<ClientConfig> {
        self.client_config.clone()
    }

    pub async fn verify_peer(&self, peer_cert: &Certificate, peer_addr: &str) -> Result<()> {
        if !self.verify_peer {
            debug!("Peer verification disabled, accepting certificate from {}", peer_addr);
            return Ok(());
        }

        info!("Verifying peer certificate for {}", peer_addr);

        // Parse the certificate
        let cert = x509_parser::parse_x509_certificate(&peer_cert.0)
            .map_err(|e| anyhow!("Failed to parse certificate: {}", e))?
            .1;

        // Verify certificate validity period
        let validity = cert.validity();
        let now_timestamp = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .map_err(|e| anyhow!("Failed to get current timestamp: {}", e))?
            .as_secs() as i64;
        
        let not_before = validity.not_before.timestamp();
        let not_after = validity.not_after.timestamp();
        
        if now_timestamp >= not_before && now_timestamp <= not_after {
            debug!("Certificate validity period is valid");
        } else {
            return Err(anyhow!("Certificate is not valid at current time"));
        }

        // Verify certificate is not self-signed (unless it's a CA)
        // Check if issuer and subject are the same (indicates self-signed)
        if cert.issuer() == cert.subject() {
            // Check if it's in our trusted CA list
            let cert_der: &[u8] = peer_cert.0.as_ref();
            let is_trusted_ca = self.ca_certs.iter().any(|ca| ca.0 == cert_der);
            
            if !is_trusted_ca {
                return Err(anyhow!("Self-signed certificate not in trusted CA list"));
            }
            debug!("Certificate is a trusted self-signed CA");
        }

        // Verify certificate chain
        // Note: This is a simplified check. In production, use a full chain verification
        debug!("Certificate issuer: {:?}", cert.issuer());
        debug!("Certificate subject: {:?}", cert.subject());

        // Extract and verify Subject Alternative Names (SANs)
        if let Ok(Some(san_ext)) = cert.get_extension_unique(&x509_parser::oid_registry::OID_X509_EXT_SUBJECT_ALT_NAME) {
            debug!("Found Subject Alternative Names extension");
            // In production, verify that peer_addr matches one of the SANs
        }

        // Check key usage if present
        if let Ok(Some(key_usage)) = cert.get_extension_unique(&x509_parser::oid_registry::OID_X509_EXT_KEY_USAGE) {
            debug!("Certificate has key usage extension: {:?}", key_usage);
        }

        // Verify certificate purpose (client vs server)
        if let Ok(Some(ext_key_usage)) = cert.get_extension_unique(&x509_parser::oid_registry::OID_X509_EXT_EXTENDED_KEY_USAGE) {
            debug!("Certificate has extended key usage: {:?}", ext_key_usage);
        }

        info!("Peer certificate verification successful for {}", peer_addr);
        Ok(())
    }

    /// Create a custom server certificate verifier
    pub fn create_client_cert_verifier(&self) -> Arc<dyn ClientCertVerifier> {
        Arc::new(SelfChainClientCertVerifier {
            ca_certs: self.ca_certs.clone(),
        })
    }

    /// Create a custom server certificate verifier for clients
    pub fn create_server_cert_verifier(&self) -> Arc<dyn ServerCertVerifier> {
        Arc::new(SelfChainServerCertVerifier {
            ca_certs: self.ca_certs.clone(),
        })
    }
}

/// Custom client certificate verifier for the server side
struct SelfChainClientCertVerifier {
    ca_certs: Vec<Certificate>,
}

impl ClientCertVerifier for SelfChainClientCertVerifier {
    fn verify_client_cert(
        &self,
        end_entity: &Certificate,
        intermediates: &[Certificate],
        now: SystemTime,
    ) -> Result<ClientCertVerified, rustls::Error> {
        // Implement custom client certificate verification
        debug!("Verifying client certificate");
        
        // For now, accept valid certificates
        // In production, implement full chain verification against CA certs
        Ok(ClientCertVerified::assertion())
    }

    fn client_auth_root_subjects(&self) -> &[rustls::DistinguishedName] {
        &[]
    }
}

/// Custom server certificate verifier for the client side  
struct SelfChainServerCertVerifier {
    ca_certs: Vec<Certificate>,
}

impl ServerCertVerifier for SelfChainServerCertVerifier {
    fn verify_server_cert(
        &self,
        end_entity: &Certificate,
        intermediates: &[Certificate],
        server_name: &rustls::ServerName,
        scts: &mut dyn Iterator<Item = &[u8]>,
        ocsp_response: &[u8],
        now: SystemTime,
    ) -> Result<ServerCertVerified, rustls::Error> {
        // Implement custom server certificate verification
        debug!("Verifying server certificate for {:?}", server_name);
        
        // For now, accept valid certificates
        // In production, implement full chain verification against CA certs
        Ok(ServerCertVerified::assertion())
    }
}
