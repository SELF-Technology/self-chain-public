# SELF SDK for Python

Build AI-native applications on SELF Chain with Python.

## Installation

```bash
pip install self-sdk
```

## Quick Start

```python
from self_sdk import SelfClient

# Initialize client
client = SelfClient(api_key="your-api-key")

# Get blockchain status
status = client.get_status()
print(f"Current height: {status['height']}")

# Get latest block
block = client.get_latest_block()
print(f"Latest block: {block['hash']}")
```

## Documentation

Full documentation available at [docs.self.app](https://docs.self.app)

## Status

This SDK is currently in development. See our [Project Status](https://docs.self.app/project-status) for more information.