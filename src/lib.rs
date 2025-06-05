pub mod visualization;
pub mod benchmark_report;
pub mod benchmark_scenarios;
pub mod benchmark_suite;
pub mod grid;
pub mod monitoring;
pub mod blockchain;

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        assert_eq!(2 + 2, 4);
    }
}
