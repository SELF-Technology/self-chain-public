// Grid module
// Handles distributed grid computing functionality

use std::collections::HashMap;
use anyhow::Result;

pub struct GridNode {
    pub id: String,
    pub resources: HashMap<String, u64>,
}

impl GridNode {
    pub fn new(id: String) -> Self {
        Self {
            id,
            resources: HashMap::new(),
        }
    }
    
    pub fn add_resource(&mut self, name: String, capacity: u64) {
        self.resources.insert(name, capacity);
    }
}

pub struct Grid {
    nodes: HashMap<String, GridNode>,
}

impl Grid {
    pub fn new() -> Self {
        Self {
            nodes: HashMap::new(),
        }
    }
    
    pub fn add_node(&mut self, node: GridNode) {
        self.nodes.insert(node.id.clone(), node);
    }
    
    pub fn get_node(&self, id: &str) -> Option<&GridNode> {
        self.nodes.get(id)
    }
}