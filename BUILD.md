# Building SELF Chain

This guide covers building SELF Chain from source.

## Prerequisites

### Required
- Rust 1.70.0 or later
- Git
- C compiler (gcc, clang, or MSVC)
- pkg-config (Linux/macOS)
- OpenSSL development headers

### Platform-Specific

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install -y build-essential pkg-config libssl-dev git curl
```

#### macOS
```bash
# Install Xcode Command Line Tools
xcode-select --install

# Install Homebrew if needed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install dependencies
brew install pkg-config openssl
```

#### Windows
- Install [Visual Studio](https://visualstudio.microsoft.com/downloads/) with C++ support
- Install [Rust](https://rustup.rs/)
- Install [Git](https://git-scm.com/download/win)

## Installing Rust

```bash
# Install rustup
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Add to PATH
source $HOME/.cargo/env

# Verify installation
rustc --version
cargo --version
```

## Building from Source

### 1. Clone the Repository

```bash
git clone https://github.com/SELF-Technology/self-chain-public.git
cd self-chain-public
```

### 2. Build Debug Version

```bash
# Build with default features
cargo build

# Build with all features
cargo build --all-features
```

### 3. Build Release Version

```bash
# Optimized build (takes longer)
cargo build --release

# The binary will be in target/release/
```

### 4. Run Tests

```bash
# Run all tests
cargo test

# Run tests with output
cargo test -- --nocapture

# Run specific test
cargo test test_name
```

## Build Features

SELF Chain supports various build features:

```toml
[features]
default = ["std", "network", "storage"]
std = []
network = ["libp2p", "tls"]
storage = ["ipfs", "orbitdb"]
cuda = ["cuda-support"]  # GPU acceleration
```

Build with specific features:
```bash
# Build with CUDA support
cargo build --features cuda

# Build without default features
cargo build --no-default-features

# Build with specific features
cargo build --no-default-features --features std,network
```

## Cross-Compilation

### Linux → Windows
```bash
# Add Windows target
rustup target add x86_64-pc-windows-gnu

# Install cross-compilation tools
sudo apt install mingw-w64

# Build for Windows
cargo build --target x86_64-pc-windows-gnu --release
```

### Linux → macOS
```bash
# Requires osxcross toolchain
# See: https://github.com/tpoechtrager/osxcross

cargo build --target x86_64-apple-darwin --release
```

### For ARM (Raspberry Pi)
```bash
# Add ARM target
rustup target add armv7-unknown-linux-gnueabihf

# Install cross-compilation tools
sudo apt install gcc-arm-linux-gnueabihf

# Build for ARM
cargo build --target armv7-unknown-linux-gnueabihf --release
```

## Build Optimization

### Release Optimizations

Edit `Cargo.toml` for custom optimization:
```toml
[profile.release]
opt-level = 3          # Maximum optimization
lto = true            # Link-time optimization
codegen-units = 1     # Single codegen unit
strip = true          # Strip symbols
panic = "abort"       # Smaller binary
```

### Build for Production
```bash
# Maximum optimization
RUSTFLAGS="-C target-cpu=native" cargo build --release

# Check binary size
ls -lh target/release/self-chain-node
```

## Development Build

### Quick Iteration
```bash
# Faster builds, slower runtime
cargo build --profile dev

# Check for errors without building
cargo check

# Auto-rebuild on changes
cargo install cargo-watch
cargo watch -x build
```

### Debug Symbols
```bash
# Build with debug symbols in release
cargo build --release --features debug-symbols

# Or set in Cargo.toml
[profile.release]
debug = true
```

## Docker Build

### Build Docker Image
```bash
# Build standard image
docker build -t self-chain:latest .

# Build with specific features
docker build --build-arg FEATURES="cuda" -t self-chain:cuda .

# Multi-stage build for smaller image
docker build -f Dockerfile.alpine -t self-chain:alpine .
```

### Using Pre-built Images
```bash
# Pull latest image
docker pull selfchain/node:latest

# Run container
docker run -d \
  --name self-node \
  -p 9000:9000 \
  -v ~/.self:/root/.self \
  selfchain/node:latest
```

## Troubleshooting

### Common Build Issues

#### OpenSSL Not Found
```bash
# macOS
export OPENSSL_DIR=$(brew --prefix openssl)

# Linux
sudo apt install libssl-dev pkg-config
```

#### Out of Memory
```bash
# Limit parallel jobs
cargo build -j 2

# Or set in config
[build]
jobs = 2
```

#### Slow Builds
```bash
# Use sccache for caching
cargo install sccache
export RUSTC_WRAPPER=sccache

# Use mold linker (Linux)
sudo apt install mold
RUSTFLAGS="-C link-arg=-fuse-ld=mold" cargo build
```

### Clean Build
```bash
# Remove build artifacts
cargo clean

# Deep clean (removes all target dirs)
find . -type d -name target -exec rm -rf {} +
```

## Build Verification

### Verify Binary
```bash
# Check version
./target/release/self-chain-node --version

# Verify functionality
./target/release/self-chain-node --help

# Run basic test
./target/release/self-chain-node validate-config
```

### Security Verification
```bash
# Check for known vulnerabilities
cargo audit

# Verify checksums
sha256sum target/release/self-chain-node
```

## Next Steps

After building:
1. [Configure your node](docs/Development/Running-A-Node.md)
2. [Join the network](docs/Development/Network-Protocol.md)
3. [Set up storage](docs/Development/Storage-Integration.md)

## Getting Help

- GitHub Issues: [Report build issues](https://github.com/SELF-Technology/self-chain-public/issues)
- Discord: [Join #dev-building](https://discord.gg/WdMdVpA4C8)
- Docs: [Full documentation](https://docs.self.app)