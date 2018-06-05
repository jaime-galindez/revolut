# revolut

## Assumptions:

1. The validation if the current user has rights to operate on origin account has been already done by the calling system
2. There is only one instance of the system. i.e. local synchronization can be applied
3. All accounts are based on the same currency, soi there is not need to do the conversion of one currency to other (to keep the system simple)
4. All accounts involved in a transfer belongs to the same organization. i.e. The system knows all the accounts
5. The system is blocking. i.e. The response of the Rest API will say if the transfer has been success or failed because of insufficient funds. Other alternative would be to base the system in transactions that are executed in order asynchronously
6. The expected workload can be assumed by the system

## Running

To run the application, execute the class com.revolut.transfer.Main

## API

The main method can be run with:

POST http://localhost:8080/account/{originAccountId}/transfer/{destinationAccountId}/{amount}

I have created other "helper" methods for testing purposes:

GET http://localhost:8080/account - Returns all the existing account of the system
GET http://localhost:8080/account/{id} - Returns the account identified by {id}
PUT http://localhost:8080/account - Creates a new account, use something like { "amount": 2350.56} as payload. It returns the created account with the db id: {"id": 1, "amount": 2350.56}
POST http://localhost:8080/account/clean - Cleans all the database  

## DB creation

I have used hibernate.hbm2ddl.auto = update to create automatically the DB

## http status code

The system will return the following codes:

200 - OK - method was run successfully
404 - Not Found - Some entity was not found
400 - Bad Request - Some parameter was incorrect
406 - Not Acceptable - if some requirement was not respected i.e. trying to withdraw a bigger amount than the origin account amount
500 - Internal Server Error - Unexpected errors

## Testing

In src/test/java you can find the unit tests
In src/it/java you can find integration tests. They need the server to be running (manually) in order to be run

## Improvements

Because of lack of time, the dependencies are hardcoded (there is not a DI system). The Entity Manager is created in the AccountManager and set to the repositories. It would be better to use a @PersistenceContext in a CDI environment.
The AccountManager is used as a Singleton, it's mandatory to use only one instance in the system, specifically, the only one instance is for the AccountLockManager. With CDI, it can be used @ApplicationScoped instead
