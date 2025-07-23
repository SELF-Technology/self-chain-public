# Running Your Own Node

## IMPORTANT: Who This Guide Is For

### This guide is for:
- 🔬 **Developers** testing applications locally
- 🖥️ **Power users** who want to run infrastructure
- 🏢 **Enterprises** running private deployments
- 🧪 **Researchers** experimenting with SELF Chain

### This guide is NOT for:
- ❌ **Regular users** - You get cloud automatically via Super-App!
- ❌ **App developers** - You don't need to run nodes, just use the SDK
- ❌ **People trying to "save money"** - The Super-App is easier and managed

## Why Run Your Own Node?

**Most users should NOT run their own node.** The SELF Super-App automatically provisions and manages everything for you. However, you might want to run a node if:

1. **Local Development**: Test your apps without using testnet resources
2. **Enterprise Deployment**: Run SELF Chain in your private infrastructure  
3. **Network Support**: Contribute to network decentralization as a validator
4. **Research**: Experiment with blockchain and AI integration

---

## The Easy Way: Use the Super-App

Before diving into manual node operation, remember:

**For 99% of users, just:**
1. Download SELF Super-App
2. Sign up
3. Done! Your cloud is ready

**Only continue if you have a specific technical need.**

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
# Pull OpenLLM image
docker pull bentoml/openllm:latest

# Run OpenLLM
docker run -d \
  --name openllm \
  -p 11434:11434 \
  -v ~/openllm:/data \
  bentoml/openllm:latest \
  start --model-id microsoft/phi-3-mini-4k-instruct
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
      - openllm
      - ipfs

  openllm:
    image: bentoml/openllm:latest
    container_name: openllm
    ports:
      - "11434:11434"
    volumes:
      - ./openllm:/data
    restart: unless-stopped
    command: start --model-id microsoft/phi-3-mini-4k-instruct

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

### 3. Install OpenLLM

```bash
# Install OpenLLM via pip
pip install openllm

# Download and start a model
openllm start microsoft/phi-3-mini-4k-instruct
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
docker stats self-node openllm ipfs

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

- **Discord**: https://discord.gg/WdMdVpA4C8
- **Issues**: https://github.com/SELF-Software/self-chain-public/issues

---

*Note: This guide covers running SELF Chain nodes on your own infrastructure. For automatic provisioning through the Super-App, users don't need to manage any of this complexity.*
