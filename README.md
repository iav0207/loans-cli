Loan Repayments Calculator
--
[![Build Status](https://travis-ci.org/iav0207/loans-cli.svg?branch=master)](https://travis-ci.org/iav0207/loans-cli)

CLI application for calculating effective rate and repayment amount
for compound loan, i.e. borrowed from a number of lenders under different rates.

### Specification

Having a CSV file containing a list of all the offers being made by the lenders within the system,
calculate the best rate possible for the prospective borrower requesting a 36 month loan of some specified amount.

See [example CSV](example/market.csv)

* The program should provide the borrower with the details
of the monthly repayment amount and the total repayment amount.
* Borrowers should be able to request a loan of any £100 increment between £1000 and £15000 inclusive.
* If the market does not have sufficient offers from lenders to satisfy the loan
then the system should inform the borrower that it is not possible to provide a quote at that time.

### Assumptions

1. The loan is compounded monthly.
1. Interest rate cannot be negative.

### Build & Run

1. Build: `mvn clean install`
1. Run: `java -jar target/loans-1.0-SNAPSHOT.jar [args]` or `run.sh [args]`

### Command Line Interface

The CLI was made self explanatory, with help:
```
$ loans.sh -h
Usage: task.loans.cli.LoansCLI [options] Input file (CSV)
  Options:
  * -a, --amount
      Loan amount (decimal)
    -h, --help
      Display this page.
    -l, --line-skip
      Skip first line (header row) in CSV
      Default: false
    -s, --sep
      Custom CSV cells separator
```
For example to calculate repayments for £2000 loan using example CSV file execute

```loans.sh example/market.csv -l -a 2000```

`-l` (line skip option) is used since the file contains CSV header row

### Technical Info

Language: Java 8.

There were some libs used to avoid reinventing wheels and make the code cleaner.

* [JCommander](https://github.com/cbeust/jcommander) for pretty CLI
* [OpenCSV](http://mvnrepository.com/artifact/net.sf.opencsv/opencsv) for handy CSV reading
* [StreamEx](https://github.com/amaembo/streamex) for extended stream API
* Utils: [Apache commons-io](http://mvnrepository.com/artifact/org.apache.commons/commons-io/)
[Apache commons-lang3](http://mvnrepository.com/artifact/org.apache.commons/commons-lang3),
[JSR-305 annotations](http://mvnrepository.com/artifact/com.google.code.findbugs/jsr305)
* Testing: [TestNG](http://testng.org/doc/) for data-driven unit tests, [Hamcrest](http://hamcrest.org/) matchers.
No mocking framework was needed as the application structure happened to be extremely simple with no deep dependencies
between components.
