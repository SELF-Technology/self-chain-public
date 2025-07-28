from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="self-sdk",
    version="0.1.0",
    author="SELF Technology",
    author_email="devs@self.app",
    description="SELF Chain Python SDK - Build AI-native applications on SELF",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/SELF-Technology/self-chain-public",
    project_urls={
        "Bug Tracker": "https://github.com/SELF-Technology/self-chain-public/issues",
        "Documentation": "https://docs.self.app",
    },
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
    ],
    package_dir={"": "src"},
    packages=find_packages(where="src"),
    python_requires=">=3.8",
    install_requires=[
        "requests>=2.28.0",
        "websocket-client>=1.5.0",
        "web3>=6.0.0",
    ],
    extras_require={
        "dev": [
            "pytest>=7.0.0",
            "pytest-asyncio>=0.21.0",
            "black>=23.0.0",
            "flake8>=6.0.0",
            "mypy>=1.0.0",
        ],
    },
)