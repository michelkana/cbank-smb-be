// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.springboot;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.example.springboot.AccountSettings;
import com.example.springboot.Accounts;
import com.example.springboot.Account;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SyncMain {

    private CosmosClient client;

    private final String databaseName = "TreasuryManagement";
    private final String containerName = "Cash";

    private CosmosDatabase database;
    private CosmosContainer container;

    public void close() {
        client.close();
    }

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main1(String[] args) {
        SyncMain p = new SyncMain();

        try {
            p.getStartedDemo();
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            p.close();
        }
        System.exit(0);
    }

    //  </Main>

    private void getStartedDemo() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
            .endpoint(AccountSettings.HOST)
            .key(AccountSettings.MASTER_KEY)
            .preferredRegions(preferredRegions)
            .userAgentSuffix("CosmosDBJavaQuickstart")
            .consistencyLevel(ConsistencyLevel.EVENTUAL)
            .buildClient();

        createDatabaseIfNotExists();
        createContainerIfNotExists();
        scaleContainer();

        //  Setup family items to create
        ArrayList<Account> accountsToCreate = new ArrayList<>();
        accountsToCreate.add(Accounts.getJohnAccount());

        createAccounts(accountsToCreate);

        System.out.println("Reading items.");
        readItems(accountsToCreate);

        System.out.println("Querying items.");
        queryItems();
    }

    private void createDatabaseIfNotExists() throws Exception {
        System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerName, "/partitionKey");

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

        System.out.println("Checking container " + container.getId() + " completed!\n");
    }
    
    private void scaleContainer() throws Exception {
        System.out.println("Scaling container " + containerName + ".");

        try {
            // You can scale the throughput (RU/s) of your container up and down to meet the needs of the workload. Learn more: https://aka.ms/cosmos-request-units
            ThroughputProperties currentThroughput = container.readThroughput().getProperties();
            int newThroughput = currentThroughput.getManualThroughput() + 100;
            container.replaceThroughput(ThroughputProperties.createManualThroughput(newThroughput));
            System.out.println("Scaled container to " + newThroughput + " completed!\n");
        } catch (CosmosException e) {
            if (e.getStatusCode() == 400)
            {
                System.err.println("Cannot read container throuthput.");
                System.err.println(e.getMessage());
            }
            else
            {
                throw e;
            }
        }
    }

    private void createAccounts(List<Account> accounts) throws Exception {
        double totalRequestCharge = 0;
        for (Account account : accounts) {

            //  Create item using container that we created using sync client

            //  Using appropriate partition key improves the performance of database operations
            CosmosItemResponse item = container.createItem(account, new PartitionKey(account.getPartitionKey()), new CosmosItemRequestOptions());

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            System.out.println(String.format("Created item with request charge of %.2f within" +
                    " duration %s",
                item.getRequestCharge(), item.getDuration()));
            totalRequestCharge += item.getRequestCharge();
        }
        System.out.println(String.format("Created %d items with total request " +
                "charge of %.2f",
            accounts.size(),
            totalRequestCharge));
    }

    private void readItems(ArrayList<Account> accountsToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
        accountsToCreate.forEach(account -> {
            try {
                CosmosItemResponse<Account> item = container.readItem(account.getId(), new PartitionKey(account.getPartitionKey()), Account.class);
                double requestCharge = item.getRequestCharge();
                Duration requestLatency = item.getDuration();
                System.out.println(String.format("Item successfully read with id %s with a charge of %.2f and within duration %s",
                        item.getItem().getId(), requestCharge, requestLatency));
            } catch (CosmosException e) {
                e.printStackTrace();
                System.err.println(String.format("Read Item failed with %s", e));
            }
        });
    }

    private void queryItems() {
        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);
        
        CosmosPagedIterable<Account> accountsPagedIterable = container.queryItems(
            "SELECT * FROM Account WHERE Account.partitionKey IN ('John')", queryOptions, Account.class);

            accountsPagedIterable.iterableByPage(preferredPageSize).forEach(cosmosItemPropertiesFeedResponse -> {
            System.out.println("Got a page of query result with " +
                    cosmosItemPropertiesFeedResponse.getResults().size() + " items(s)"
                    + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            System.out.println("Item Ids " + cosmosItemPropertiesFeedResponse
                    .getResults()
                    .stream()
                    .map(Account::getId)
                    .collect(Collectors.toList()));
        });
    }
}
