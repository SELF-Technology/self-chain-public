# Building SELF Chain

## Prerequisites

1. Java Development Kit (JDK) 17 or higher
2. Git
3. Gradle (version 7.0 or higher)

## Building the System

The SELF Chain system consists of two repositories:
1. Public repository (this repository)
2. Private repository (contains sensitive components)

### 1. Clone the Repositories

```bash
# Clone the public repository
git clone https://github.com/SELF-Technology/self-chain-public.git
cd self-chain-public

# The private repository will be automatically cloned during the build process
```

### 2. Build the System

```bash
# Build the system
./gradlew build

# Build the system with the private repository
./gradlew downloadPrivateRepo build
```

### 3. Run the System

```bash
# Run the system
./gradlew run

# Or create a fat JAR
./gradlew shadowJar
```

## Build Configuration

The build system is configured to:
1. Automatically clone and update the private repository
2. Include private repository resources in the build
3. Create a fat JAR with all dependencies
4. Support both development and production builds

## Security Notes

1. The private repository contains sensitive components and requires proper authentication
2. Never commit sensitive information to the public repository
3. Use environment variables for configuration
4. Follow the security guidelines in the README.md
