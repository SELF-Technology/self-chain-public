use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
pub struct AIConfig {
    pub api_key: String,
    pub model: String,
    pub context: String,
    pub max_tokens: u32,
    pub temperature: f32,
    pub top_p: f32,
}

impl Default for AIConfig {
    fn default() -> Self {
        Self {
            api_key: "".to_string(),
            model: "llama2".to_string(),
            context: "".to_string(),
            max_tokens: 2000,
            temperature: 0.7,
            top_p: 0.95,
        }
    }
}
