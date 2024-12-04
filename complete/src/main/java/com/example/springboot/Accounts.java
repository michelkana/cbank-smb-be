// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.springboot;

public class Accounts {
 
    public static Account getJohnAccount() {
        Account johnAccount = new Account();
        johnAccount.setId("John-" + System.currentTimeMillis());
        johnAccount.setOwner("John Smith");
        johnAccount.setPartitionKey("John");
        johnAccount.setIBAN("DExxxxx");

        Transaction transaction1 = new Transaction();
        transaction1.setDate("2024-12-01");
        transaction1.setAmount(123.20);
        transaction1.setCategory("Internet Selling");
        transaction1.setReference("INV09862");
        transaction1.setIBAN("DE12356");
        transaction1.setCounterparty("Rapida Gmbh");

        Transaction transaction2 = new Transaction();
        transaction2.setDate("2024-12-01");
        transaction2.setAmount(4357.98);
        transaction2.setCategory("Shop Selling");
        transaction2.setReference("INV00913");
        transaction2.setIBAN("DE8973");
        transaction2.setCounterparty("Mop AG");

        Transaction transaction3 = new Transaction();
        transaction3.setDate("2024-12-03");
        transaction3.setAmount(-898.09);
        transaction3.setCategory("Credit Card Payment");
        transaction3.setReference("VISA987");
        transaction3.setIBAN("DE7622");
        transaction3.setCounterparty("VISA CBANK");

        Transaction transaction4 = new Transaction();
        transaction4.setDate("2024-12-04");
        transaction4.setAmount(-19.23);
        transaction4.setCategory("Bank Fees");
        transaction4.setReference("CBANK GIRO 6722");
        transaction4.setIBAN("DE98980");
        transaction4.setCounterparty("GIRO CBANK");

        Transaction transaction5 = new Transaction();
        transaction5.setDate("2024-12-04");
        transaction5.setAmount(219.01);
        transaction5.setCategory("Store Selling");
        transaction5.setReference("INV6729");
        transaction5.setIBAN("DE9873");
        transaction5.setCounterparty("Bedna AG");

        johnAccount.setTransactions(new Transaction[] { transaction1, transaction2, transaction3, transaction4, transaction5 });

        return johnAccount;
    }

}
