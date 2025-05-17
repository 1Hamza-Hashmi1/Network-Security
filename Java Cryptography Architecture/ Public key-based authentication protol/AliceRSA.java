package project2;

import project2.RSAUtil;
import java.io.*;
import java.util.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class AliceRSA {
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Connected to Bob..."); 
        System.out.println("\nMessage sequence: \nIDA||NA\nE(PUA, E(PRB, NA))||NB\nE(PUB, E(PRA, NB))\n"); 
        
        //KeyPair and Nonce Generation
        KeyPair keyPairA = RSAUtil.generateKeyPair();
        PublicKey publicKeyA = keyPairA.getPublic();
        PrivateKey privateKeyA = keyPairA.getPrivate();
        String pubKeyStrA = Base64.getEncoder().encodeToString(publicKeyA.getEncoded());
        String priKeyStrA = Base64.getEncoder().encodeToString(privateKeyA.getEncoded());
        String nonceA = RSAUtil.generateNonce();
        System.out.println("Public Key A: " + pubKeyStrA);
        System.out.println("Private Key A: " + priKeyStrA); 
        System.out.println("NonceA: " + nonceA);
        String Message1 = "Alice " + nonceA + " " + pubKeyStrA; 
        
        //MESSAGE1
        System.out.println("Sending Message 1: " + Message1);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(Message1);
        
        //MESSAGE2
        System.out.println("Waiting for Message 2: ");
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        String Message2 = (String) in.readObject(); 
        String[] split = Message2.split(" ");
        String Emsg = split[0];
        String nonceB = split[1]; 
        String pubKeyStrB = split[2];
        System.out.println("Received Encrypted Message 2 from Bob: " + Emsg + " " + nonceB + "\nPublic Key B: " + pubKeyStrB);
        
        //Get Public Key of B From Base64 representation.
        byte[] bytes = Base64.getDecoder().decode(pubKeyStrB);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        keyFactory.generatePublic(keySpec);
        PublicKey publicKeyB = keyFactory.generatePublic(keySpec);
        
        //Decrypt using own private key, then decrypt with public key of B
        String Dmsg = RSAUtil.decrypt(Emsg, privateKeyA);
        String Dsig = RSAUtil.validateSignature(Dmsg, publicKeyB);
        System.out.println("Decryption using my Private Key: " + Dmsg);
        System.out.println("Decryption using Bob's Public Key: " + Dsig);
        
        //MESSAGE3
        String sigA = RSAUtil.generateSignature(nonceB, privateKeyA);
        String Message3 = RSAUtil.encrypt(sigA, publicKeyB);
        System.out.println("Signature E(PRA, NB): " + sigA);
        System.out.println("Sending Message 3 E(PUB, E(PRA, NB)): " + Message3);
        out.writeObject(Message3);
    
    }
}
