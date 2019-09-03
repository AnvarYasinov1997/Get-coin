package com.getIn.getCoin.getCoin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import kotlin.text.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class BlockChainUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> String serializeObjectToString(final T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static <K, V>HashMap<K, V> serializeStringToHashMap(final String data, final TypeReference typeReference) {
        try {
            return objectMapper.readValue(data, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static <T> T serializeStringToObject(final String data, final Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String getJson(final Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    public static String getDifficultyString(final Integer difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getHash(final String data) {
        try {
            final byte[] hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes(Charsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static KeyPair getKeyPairGenerator() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            final ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            return keyGen.generateKeyPair();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String getStringFromKey(final Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static byte[] getKeyBytesFromString(final String key) {
        return Base64.getDecoder().decode(key);
    }

    public static PublicKey decodePublicKey(final byte[] publicKeyBytes) {
        try {
            final KeyFactory ecKeyFac = KeyFactory.getInstance("ECDSA", "BC");
            final X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            return ecKeyFac.generatePublic(x509EncodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static PrivateKey decodePrivateKey(final byte[] privateKeyBytes) {
        try {
            final KeyFactory ecKeyFac = KeyFactory.getInstance("ECDSA", "BC");
            final PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return ecKeyFac.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] applyECDSASig(final PrivateKey privateKey, final String input) {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyECDSASig(final PublicKey publicKey, final String data, final byte[] signature) {
        try {
            final Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte hash1 : hash) {
            final String hex = Integer.toHexString(0xff & hash1);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getFileContent(final String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static List<File> getBlocksFromDir(final String dir) {
        final File file = new File(dir);
        if (!file.exists()) throw new RuntimeException("File is not found");
        final File[] files = Objects.requireNonNull(file.listFiles());
        return Arrays.asList(files);
    }

    public static String getMerkleRoot(final List<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<>();

        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTransactionId());
        }

        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(getHash(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

}
