// Context Manager
// Manages context for AI operations
// Detailed implementation in private repository

use anyhow::Result;
use std::sync::Arc;
use tokio::sync::RwLock;

pub struct ContextManager {
    context: Arc<RwLock<Vec<String>>>,
}

impl ContextManager {
    pub fn new() -> Self {
        Self {
            context: Arc::new(RwLock::new(Vec::new())),
        }
    }
    
    pub async fn add_context(&self, context: String) -> Result<()> {
        let mut ctx = self.context.write().await;
        ctx.push(context);
        Ok(())
    }
    
    pub async fn get_context(&self) -> Result<Vec<String>> {
        let ctx = self.context.read().await;
        Ok(ctx.clone())
    }
    
    pub async fn clear_context(&self) -> Result<()> {
        let mut ctx = self.context.write().await;
        ctx.clear();
        Ok(())
    }
}