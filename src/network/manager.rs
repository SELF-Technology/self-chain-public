use std::sync::{Arc, RwLock};
use tokio::sync::mpsc;
use tokio::time::{sleep, Duration};
use crate::network::node::{NetworkNode, NetworkMessage};
use crate::blockchain::blockchain::Blockchain;
use crate::core::config::NetworkConfig;

pub struct NetworkManager {
    node: Arc<NetworkNode>,
    blockchain: Arc<Blockchain>,
    message_sender: mpsc::Sender<NetworkMessage>,
    message_receiver: mpsc::Receiver<NetworkMessage>,
    pending_blocks: Arc<RwLock<Vec<Block>>>,
    pending_transactions: Arc<RwLock<Vec<Transaction>>>,
}

impl NetworkManager {
    pub fn new(
        config: NetworkConfig,
        blockchain: Arc<Blockchain>,
    ) -> Self {
        let node = Arc::new(NetworkNode::new(config));
        let (message_sender, message_receiver) = mpsc::channel(100);

        Self {
            node,
            blockchain,
            message_sender,
            message_receiver,
            pending_blocks: Arc::new(RwLock::new(Vec::new())),
            pending_transactions: Arc::new(RwLock::new(Vec::new())),
        }
    }

    pub async fn start(&self) {
        // Start network node
        tokio::spawn(self.node.start());

        // Start message handler
        tokio::spawn(self.handle_messages());

        // Start peer discovery
        tokio::spawn(self.peer_discovery());

        // Start block synchronization
        tokio::spawn(self.block_synchronization());
    }

    async fn handle_messages(&self) {
        while let Some(message) = self.node.get_message().await {
            match message {
                NetworkMessage::NewBlock(block) => {
                    self.handle_new_block(block).await;
                }
                NetworkMessage::Transaction(tx) => {
                    self.handle_transaction(tx).await;
                }
                NetworkMessage::GetBlocks => {
                    self.send_blocks().await;
                }
                NetworkMessage::Blocks(blocks) => {
                    self.handle_blocks(blocks).await;
                }
                NetworkMessage::Ping => {
                    self.node.broadcast_message(NetworkMessage::Pong).await;
                }
                NetworkMessage::Pong => {
                    // Handle pong response
                }
            }
        }
    }

    async fn handle_new_block(&self, block: Block) {
        // Add to pending blocks
        self.pending_blocks.write().await.push(block);
    }

    async fn handle_transaction(&self, tx: Transaction) {
        // Add to pending transactions
        self.pending_transactions.write().await.push(tx);
    }

    async fn send_blocks(&self) {
        let blocks = self.blockchain.chain.clone();
        self.node.broadcast_message(NetworkMessage::Blocks(blocks)).await;
    }

    async fn handle_blocks(&self, blocks: Vec<Block>) {
        // Process received blocks
        for block in blocks {
            if !self.blockchain.chain.contains(&block) {
                self.blockchain.chain.push(block);
            }
        }
    }

    async fn peer_discovery(&self) {
        loop {
            sleep(Duration::from_secs(30)).await;
            
            for peer in &self.node.config.peers {
                self.node.connect_to_peer(peer.parse().unwrap()).await;
            }
        }
    }

    async fn block_synchronization(&self) {
        loop {
            sleep(Duration::from_secs(5)).await;
            
            // Process pending blocks
            let mut pending_blocks = self.pending_blocks.write().await;
            while let Some(block) = pending_blocks.pop() {
                if self.blockchain.validate_chain() {
                    self.blockchain.chain.push(block);
                }
            }

            // Process pending transactions
            let mut pending_transactions = self.pending_transactions.write().await;
            while let Some(tx) = pending_transactions.pop() {
                if self.blockchain.add_transaction(tx) {
                    self.blockchain.mine_block().await;
                }
            }
        }
    }
}
