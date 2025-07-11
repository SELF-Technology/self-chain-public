# Running Your Own Node

## Overview

While SELF Chain automatically provisions cloud infrastructure for Super-App users, developers and power users can run their own nodes. This guide explains how to set up and operate a SELF Chain node on your own infrastructure.

## System Requirements

### Minimum Requirements (Testnet)
- **CPU**: 2 cores (x86_64 or ARM64)
- **RAM**: 4GB
- **Storage**: 50GB SSD
- **Network**: 100 Mbps stable connection
- **OS**: Ubuntu 20.04+ or similar Linux distribution

### Recommended Requirements (Production)
- **CPU**: 4+ cores
- **RAM**: 8GB+
- **Storage**: 200GB+ SSD
- **Network**: 1 Gbps connection
- **OS**: Ubuntu 22.04 LTS

## Quick Start with Docker

### 1. Install Docker

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER
```

### 2. Run SELF Chain Node

```bash
# Pull the latest image
docker pull selfchain/node:latest

# Create data directory
mkdir -p ~/self-node-data

# Run the node
docker run -d \
  --name self-node \
  -p 8000:8000 \
  -p 3030:3030 \
  -v ~/self-node-data:/data \
  selfchain/node:latest \
  --config /data/config.toml
```

### 3. Run Private LLM

```bash
# Pull Ollama image
docker pull ollama/ollama:latest

# Run Ollama
docker run -d \
  --name ollama \
  -p 11434:11434 \
  -v ~/ollama:/root/.ollama \
  ollama/ollama:latest

# Pull a model (e.g., Phi-3)
docker exec -it ollama ollama pull phi3:mini
```

## Configuration

### Basic Configuration (config.toml)

```toml
# Node Configuration
[node]
name = "my-self-node"
data_dir = "/data"

# Network Configuration
[network]
chain_id = "self-testnet-001"
p2p_port = 8000
api_port = 3030
bootstrap_nodes = [
    "http://13.220.156.247:3030",
    "http://34.203.202.6:3030",
    "http://52.23.226.218:3030"
]

# Consensus
[consensus]
algorithm = "poai"
validator = false  # Set to true if running a validator

# Storage
[storage]
ipfs_enabled = true
orbitdb_enabled = true

# AI Configuration
[ai]
endpoint = "http://localhost:11434/v1"
model = "phi3:mini"
max_tokens = 1024
```

## Using Docker Compose

For easier management, use Docker Compose:

### docker-compose.yml

```yaml
version: '3.8'

services:
  self-node:
    image: selfchain/node:latest
    container_name: self-node
    ports:
      - "8000:8000"
      - "3030:3030"
    volumes:
      - ./data:/data
      - ./config.toml:/data/config.toml
    environment:
      - RUST_LOG=info
    restart: unless-stopped
    depends_on:
      - ollama
      - ipfs

  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    ports:
      - "11434:11434"
    volumes:
      - ./ollama:/root/.ollama
    restart: unless-stopped

  ipfs:
    image: ipfs/kubo:latest
    container_name: ipfs
    ports:
      - "4001:4001"
      - "5001:5001"
      - "8080:8080"
    volumes:
      - ./ipfs:/data/ipfs
    restart: unless-stopped
```

Start all services:
```bash
docker-compose up -d
```

## Manual Installation (Advanced)

### 1. Build from Source

```bash
# Install Rust
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source $HOME/.cargo/env

# Clone repository
git clone https://github.com/SELF-Software/self-chain-public.git
cd self-chain-public

# Build
cargo build --release

# Binary will be at ./target/release/self-chain-node
```

### 2. Install IPFS

```bash
# Download IPFS
wget https://dist.ipfs.tech/kubo/v0.24.0/kubo_v0.24.0_linux-amd64.tar.gz
tar -xvzf kubo_v0.24.0_linux-amd64.tar.gz
cd kubo
sudo bash install.sh

# Initialize IPFS
ipfs init
```

### 3. Install Ollama

```bash
# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Start Ollama service
sudo systemctl start ollama

# Pull a model
ollama pull phi3:mini
```

## Node Operations

### Check Node Status

```bash
# Via API
curl http://localhost:3030/info

# Via Docker logs
docker logs self-node
```

### Monitor Performance

```bash
# Resource usage
docker stats self-node ollama ipfs

# Node metrics
curl http://localhost:3030/metrics
```

### Backup Node Data

```bash
# Stop node
docker-compose down

# Backup data
tar -czf self-node-backup-$(date +%Y%m%d).tar.gz ./data

# Restart node
docker-compose up -d
```

## Connecting to Testnet

1. Ensure your firewall allows connections on ports 8000 and 3030
2. Your node will automatically connect to bootstrap nodes
3. Monitor peer connections:

```bash
curl http://localhost:3030/peers
```

## Running a Validator Node

To participate in consensus as a validator:

1. **Stake Requirements**: Minimum stake required (see current requirements)
2. **Uptime**: Maintain 95%+ uptime
3. **Hardware**: Use recommended specifications
4. **Configuration**: Set `validator = true` in config.toml

## Troubleshooting

### Node Won't Start
- Check port availability: `sudo lsof -i :8000`
- Verify config file syntax
- Check Docker logs: `docker logs self-node`

### Can't Connect to Peers
- Ensure firewall rules allow P2P port (8000)
- Check bootstrap nodes are reachable
- Verify network connectivity

### High Resource Usage
- Reduce AI model size
- Limit IPFS bandwidth
- Adjust resource limits in Docker

## Security Considerations

1. **Firewall**: Only expose necessary ports
2. **Updates**: Keep node software updated
3. **Backups**: Regular backups of node data
4. **Monitoring**: Set up alerts for anomalies
5. **Keys**: Secure your node's private keys

## Cloud Provider Guides

### AWS EC2
- Use t3.medium or larger
- Security group: Allow ports 8000, 3030
- Attach EBS volume for data persistence

### Google Cloud
- Use e2-medium or larger
- Firewall rules for P2P and API
- Use persistent disk for data

### DigitalOcean
- Use 4GB droplet or larger
- Configure firewall via UI
- Use block storage for data

### Hetzner
- Use CX21 or larger
- Configure firewall rules
- Use volume for data storage

## Getting Help

- **Discord**: Join #node-operators channel
- **Forums**: https://forum.self.app
- **Issues**: https://github.com/SELF-Software/self-chain-public/issues
- **Docs**: https://docs.self.app

---

*Note: This guide covers running SELF Chain nodes on your own infrastructure. For automatic provisioning through the Super-App, users don't need to manage any of this complexity.*