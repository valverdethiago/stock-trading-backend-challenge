# Back end take home
## Overview
This is a fictional Stock Trading API project.  
The project currently supports creating an account (with some less than perfect code).
You'll be adding support for making trades, and doing some light refactoring work on the existing code.

### What's a trade?
For the purposes of this exercise, a trade represents the Buying or Selling of a Stock.
A trade includes the following properties:

- symbol (e.g. 'AAPL' for Apple)
- quantity
- side (buy or sell)
- price
- status (SUBMITTED, CANCELLED, COMPLETED, or FAILED)

## Tasks
Make sure to write tests (JUnit 5 or Spock).  Create a branch for the following:

1.  Design the table(s) to store trades made for an account
1.  Create the associated flyway migrations. 
1.  Create a ReSTful API that 
    1. Submits a new trade.
        - Quantity & price must be > 0
    1. Reads existing trades.
    1. Cancels a trade that's still in a SUBMITTED status.
1.  Refactor any poorly structured code you find in the Account related code. 
1.  Fix the error handling so that the API doesn't return any 5xx responses.

### Building the project
    ./gradlew clean build

### Running the project
    ./gradlew bootRun

## Evaluation Criteria
We'll be looking at the following criteria when assessing candidate submissions:
- Project architecture
- Code simplicity and clarity
- Git history, including comments in the PR

**Good luck!**

Thanks. Please take some time to check a few notes I wrote about this exercise [here](./interview.MD)