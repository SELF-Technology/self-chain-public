pub mod blockchain;
pub mod network;
pub mod config;
pub mod core;
pub mod ai;
pub mod crypto;
pub mod consensus;
pub mod serialization;
pub mod grid;
pub mod runtime;
pub mod storage;
pub mod validation;
pub mod security;

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        assert_eq!(2 + 2, 4);
    }
}
