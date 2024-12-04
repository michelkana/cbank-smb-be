# Corporate Banking App Backend

This project demonstrates Spring Boot as an API for interfacing Azure Cosmo DB, holding cash management data (bank account kyc data, incoming and outgoing transactions).

## Key Classes

- **AccountSettings.java**: Cosmo DB connection setup
- **Account.java**: Main model to hold know your customer information
- **Transactions.java**: Main model to hold bank account transactions
- **Accounts.java**: mocking class to generate test DB data
- **CashController.java**: MVC controller to handle HTTP requests from clients

## Cloud

- **The Azure App Service** is used to host and manage the app
- **The Azure Cosmos DB** is used to hold banking data in NoSQL format
- **The Azure Container Registry** is store dockerized image of the app, and deploy to Azure App Service

## Testing

- **HelloControllerTest.java**: Unit tests for HTTP service availability

## Usage

The App is dockerrized and deployed as an Azure Web App https://cbank-smb-app.azurewebsites.net/cash





