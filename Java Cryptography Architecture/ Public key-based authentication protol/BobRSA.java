package project2;

import project2.RSAUtil;
import java.io.*;
import java.util.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class BobRSA {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Bob (Server) is running... Waiting for Client to connect.");
        Socket socket = serverSocket.accept();
        System.out.println("\nMessage sequence: \nIDA||NA\nE(PUA, E(PRB, NA))||NB\nE(PUB, E(PRA, NB))\n"); 
        
        //KeyPair and Nonce Generation
        KeyPair keyPairB = RSAUtil.generateKeyPair();
        PublicKey publicKeyB = keyPairB.getPublic();
        PrivateKey privateKeyB = keyPairB.getPrivate();
        String pubKeyStrB = Base64.getEncoder().encodeToString(publicKeyB.getEncoded());
        String priKeyStrB = Base64.getEncoder().encodeToString(privateKeyB.getEncoded());
        String nonceB = RSAUtil.generateNonce();
        System.out.println("Public Key B: " + pubKeyStrB);
        System.out.println("Private Key B: " + priKeyStrB);
        System.out.println("NonceB: " + nonceB);

        //MESSAGE1
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        String Message1 = (String) in.readObject();
        String[] split = Message1.split(" ");
        String ID = split[0];
        String nonceA = split[1]; 
        String pubKeyStrA = split[2];
        System.out.println("Received Message 1 from ID: " + ID + " with Nonce: " + nonceA + "\nPublic Key " + ID +": " + pubKeyStrA);
        
        //Get Public Key of A From Base64 representation.
        byte[] bytes = Base64.getDecoder().decode(pubKeyStrA);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        keyFactory.generatePublic(keySpec);
        PublicKey publicKeyA = keyFactory.generatePublic(keySpec);
        
        //MESSAGE2
        String sigB = RSAUtil.generateSignature(nonceA, privateKeyB);
        String Message2 = RSAUtil.encrypt(sigB, publicKeyA) + " " +nonceB + " " + pubKeyStrB;
        System.out.println("Signature E(PRB, NA): " + sigB);
        System.out.println("Sending Message 2 E(PUA, E(PRB, NA))||NB : " + Message2);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(Message2);
       
        //MESSAGE3
        String Message3 = (String) in.readObject();
        String Dmsg = RSAUtil.decrypt(Message3, privateKeyB);
        String Dsig = RSAUtil.validateSignature(Dmsg, publicKeyA);
        System.out.println("Received Encrypted Message 3 from " + ID + ": " + Message3);
        System.out.println("Decryption using my Private Key: " + Dmsg);
        System.out.println("Decryption using Alice's Public Key: " + Dsig); 
        
    }
}
