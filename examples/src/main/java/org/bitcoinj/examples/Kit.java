/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.examples;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The following example shows how to use the by bitcoinj provided WalletAppKit.
 * The WalletAppKit class wraps the boilerplate (Peers, BlockChain, BlockStorage, Wallet) needed to set up a new SPV bitcoinj app.
 * <p>
 * In this example we also define a WalletEventListener class with implementors that are called when the wallet changes (for example sending/receiving money)
 */
public class Kit {

    public static void main(String[] args) {

        // First we configure the network we want to use.
        // The available options are:
        // - BitcoinNetwork.MAINNET
        // - BitcoinNetwork.TESTTEST
        // - BitcoinNetwork.SIGNET
        // - BitcoinNetwork.REGTEST
        // While developing your application you probably want to use the Regtest mode and run your local bitcoin network. Run bitcoind with the -regtest flag
        // To test you app with a real network you can use the testnet. The testnet is an alternative bitcoin network that follows the same rules as main network.
        // Coins are worth nothing and you can get coins from a faucet.
        // 
        // For more information have a look at: https://bitcoinj.github.io/testing and https://bitcoin.org/en/developer-examples#testing-applications
        BitcoinNetwork network = BitcoinNetwork.TESTNET;
        Context.propagate(new Context());

        // Initialize and start a WalletAppKit. The kit handles all the boilerplate for us and is the easiest way to get everything up and running.
        // Look at the WalletAppKit documentation and its source to understand what's happening behind the scenes: https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/kits/WalletAppKit.java
        // WalletAppKit extends the Guava AbstractIdleService. Have a look at the introduction to Guava services: https://github.com/google/guava/wiki/ServiceExplained
        WalletAppKit kit = WalletAppKit.launch(network, new File("."), "walletappkit-example", (k) -> {
            // In case you want to connect with your local bitcoind tell the kit to connect to localhost.
            // This is done automatically in reg test mode.
            // k.connectToLocalHost();
        });

        kit.wallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
            System.out.println("-----> coins resceived: " + tx.getTxId());
            System.out.println("received: " + tx.getValue(wallet));
            Address to = kit.wallet().parseAddress("tb1qeww9d68r9xyka203zpzmwmwc94rp8sqfgekhwn");
            Coin value = Coin.parseCoin("0.00001");
            org.bitcoinj.wallet.SendRequest req = org.bitcoinj.wallet.SendRequest.to(to, value);
            req.setFeePerVkb(Coin.valueOf(1100));
            try {
                Wallet.SendResult result = wallet.sendCoins(req);
                System.out.println("coins sent. transaction hash: " + result.transaction().getTxId());
            } catch (InsufficientMoneyException e) {
                System.out.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
                System.out.println("Please send enough money to: " + wallet.currentReceiveAddress().toString());
            }
        });

        kit.wallet().addCoinsSentEventListener((wallet, tx, prevBalance, newBalance) -> System.out.println("coins sent"));

        kit.wallet().addKeyChainEventListener(keys -> System.out.println("new key added"));

        kit.wallet().addScriptsChangeEventListener((wallet, scripts, isAddingScripts) -> System.out.println("new script added"));

        kit.wallet().addTransactionConfidenceEventListener((wallet, tx) -> {
            System.out.println("-----> confidence changed: " + tx.getTxId());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
        });

        // Ready to run. The kit syncs the blockchain and our wallet event listener gets notified when something happens.
        // To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        System.out.println("send money to: " + kit.wallet().currentReceiveAddress().toString());
//        System.out.println("send money to: " + kit.wallet().freshReceiveAddress().toString());

        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //System.out.println("shutting down again");
//        kit.stopAsync();
        kit.awaitTerminated();
    }

}
