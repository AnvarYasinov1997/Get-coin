package com.getIn.getCoin.blockChain;

import javafx.util.Pair;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    private final PublicKey publicKey;

    private final PrivateKey privateKey;

    private final Map<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet(final String publicKey,
                  final String privateKey) {
        this.publicKey = BlockChainUtils.decodePublicKey(BlockChainUtils.getKeyBytesFromString(publicKey));
        this.privateKey = BlockChainUtils.decodePrivateKey(BlockChainUtils.getKeyBytesFromString(privateKey));
    }

    public Wallet(final PublicKey publicKey,
                  final PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static Pair<PublicKey, PrivateKey> createNewWallet() {
        final KeyPair keyPair = BlockChainUtils.getKeyPairGenerator();
        return new Pair<>(keyPair.getPublic(), keyPair.getPrivate());
    }

    public Long getBalance() {
        Long total = 0L;
        for (Map.Entry<String, TransactionOutput> item : BlockChainImpl.UTXOs.entrySet()) {
            final TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(this.publicKey)) {
                this.UTXOs.put(UTXO.getId(), UTXO);
                total += UTXO.getAmount();
            }
        }
        return total;
    }

    public Transaction generateTransaction(final PublicKey recipient,
                                           final PublicKey processer,
                                           final Long amount,
                                           final Long commissionAmount) {
        if (getBalance() < amount) {
            System.out.println("> Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        final List<TransactionInput> inputs = new ArrayList<>();

        Long total = 0L;
        for (final Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getAmount();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > amount + commissionAmount) break;
        }

        final Transaction newTransaction = new Transaction(this.publicKey, recipient, processer, amount, commissionAmount, inputs);
        newTransaction.generateSignature(privateKey);

        for (final TransactionInput input : inputs) {
            UTXOs.remove(input.getTransactionOutputId());
        }

        return newTransaction;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
