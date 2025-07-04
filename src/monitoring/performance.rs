use std::sync::Arc;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SystemInfo {
    pub cpu: CpuInfo,
    pub memory: MemoryInfo,
    pub storage: StorageInfo,
    pub gpu: Option<GpuInfo>,
}

impl Default for SystemInfo {
    fn default() -> Self {
        Self {
            cpu: CpuInfo::default(),
            memory: MemoryInfo::default(),
            storage: StorageInfo::default(),
            gpu: None,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CpuInfo {
    pub cores: u32,
    pub frequency: u64,
    pub load: f64,
    pub temperature: f64,
}

impl Default for CpuInfo {
    fn default() -> Self {
        Self {
            cores: 0,
            frequency: 0,
            load: 0.0,
            temperature: 0.0,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MemoryInfo {
    pub total: u64,
    pub used: u64,
    pub available: u64,
    pub swap_total: u64,
    pub swap_used: u64,
}

impl Default for MemoryInfo {
    fn default() -> Self {
        Self {
            total: 0,
            used: 0,
            available: 0,
            swap_total: 0,
            swap_used: 0,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct StorageInfo {
    pub total: u64,
    pub used: u64,
    pub available: u64,
    pub read_speed: u64,
    pub write_speed: u64,
}

impl Default for StorageInfo {
    fn default() -> Self {
        Self {
            total: 0,
            used: 0,
            available: 0,
            read_speed: 0,
            write_speed: 0,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GpuInfo {
    pub cores: u64,
    pub memory: u64,
    pub usage: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResourceUtilization {
    pub cpu: f64,
    pub memory: f64,
    pub storage: f64,
    pub gpu: Option<f64>,
}

pub struct PerformanceMonitor {
    pub system_info: Arc<SystemInfo>,
}

impl PerformanceMonitor {
    pub fn new(system_info: Arc<SystemInfo>) -> Self {
        Self {
            system_info,
        }
    }

    pub fn get_resource_utilization(&self) -> ResourceUtilization {
        ResourceUtilization {
            cpu: self.system_info.cpu.load,
            memory: (self.system_info.memory.used as f64 / self.system_info.memory.total as f64) * 100.0,
            storage: (self.system_info.storage.used as f64 / self.system_info.storage.total as f64) * 100.0,
            gpu: self.system_info.gpu.as_ref().map(|gpu| gpu.usage),
        }
    }
}
