package project2;

import java.util.*;
import java.security.*;
import javax.crypto.*;

/**
 * @author Ali AJ Parasteh
 * Lab 2 Project 2
 * Student Number 501021960
 */ 
public class RSAUtil {
    
    //INPUTS: None, Static Calls 
    //OUTPUTS: New Key Pair (Public and Private Key)
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator key = KeyPairGenerator.getInstance("RSA");
        key.initialize(1024);
        return key.genKeyPair();
    }
    
    //INPUTS: Plaintext to Encrypt, Public Key of Recipient
    //OUTPUTS: RSA Encrypted Ciphertext 
    public static String encrypt(String plaintext, PublicKey pu)throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pu);
        byte[] bytes = plaintext.getBytes();
        int offset = 0;
        StringBuilder ciphertext = new StringBuilder();
        
        while (offset < bytes.length) {
            int size = Math.min(bytes.length - offset, 117);
            byte[] chunk = cipher.doFinal(bytes, offset, size);
            ciphertext.append(Base64.getEncoder().encodeToString(chunk));
            offset += size;
        }
        return(ciphertext.toString());
    }
    
    //INPUTS: Cipher to Decrypt, Private Key of User
    //OUTPUTS: Decrypted Plaintext
    public static String decrypt(String ciphertext, PrivateKey pr)throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pr);
        StringBuilder plaintext = new StringBuilder();
        int offset = 0;

        while(offset < ciphertext.length()) {
            int size = Math.min(offset + 172, ciphertext.length());
            String chunk = ciphertext.substring(offset, size);
            byte[] bytes = Base64.getDecoder().decode(chunk);
            plaintext.append(new String(cipher.doFinal(bytes)));
            offset += size;
        }
        return (plaintext.toString());
    }
    
    
    
    
    //INPUTS: None, Static Calls
    //OUTPUTS: 16 Byte Nonce to Append to Messages
    public static String generateNonce() {
        SecureRandom rand = new SecureRandom();
        byte[] nonce = new byte[16];
        rand.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }
    
    //INPUTS: Nonce of Sender, Private Key of Recipient
    //OUTPUTS: Message Signature to Be Validated by the Sender's Public Key
    public static String generateSignature(String nonce, PrivateKey pr)throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pr);
        String ciphernonce = Base64.getEncoder().encodeToString(cipher.doFinal(nonce.getBytes()));
        return(ciphernonce);
    }
    
    //INPUTS: Signature and Public Key of Sender
    //OUTPUTS: Unencrypted Signature (for this lab the nonce of the receiver)
    public static String validateSignature(String signature, PublicKey pu)throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pu);
        byte[] plaintext = cipher.doFinal(Base64.getDecoder().decode(signature));
        return(new String(plaintext));
    }
    
    //TESTER MAIN FUNCTION BEFORE MAKING SERVERS
    public static void main(String[] args) throws Exception{
        //GENERATE KEYPAIR A AND B
        KeyPair keyPairA = generateKeyPair();
        PublicKey publicKeyA = keyPairA.getPublic();
        PrivateKey privateKeyA = keyPairA.getPrivate();
        KeyPair keyPairB = generateKeyPair();
        PublicKey publicKeyB = keyPairB.getPublic();
        PrivateKey privateKeyB = keyPairB.getPrivate();
        
        String pubKeyStrA = Base64.getEncoder().encodeToString(publicKeyA.getEncoded());
        String priKeyStrA = Base64.getEncoder().encodeToString(privateKeyA.getEncoded());
        String pubKeyStrB = Base64.getEncoder().encodeToString(publicKeyB.getEncoded());
        String priKeyStrB = Base64.getEncoder().encodeToString(privateKeyB.getEncoded());
        
        System.out.println("Public Key A: " + pubKeyStrA);
        System.out.println("Private Key A: " + priKeyStrA);
        System.out.println("Public Key B: " + pubKeyStrB);
        System.out.println("Private Key B: " + priKeyStrB);
        
        //GENERATE NONCE AND SIGNATURE (ENCRYPT WITH PRIVATE KEY)
        String nonceA = RSAUtil.generateNonce();
        String nonceB = RSAUtil.generateNonce();
        String sigB = RSAUtil.generateSignature(nonceA, privateKeyB);
        String sigA = RSAUtil.generateSignature(nonceB, privateKeyA);
        
        System.out.println("NonceA: " + nonceA);
        System.out.println("NonceB: " + nonceB);
        System.out.println("Signature B 'E(PRB, NA)': " + sigB); 
        System.out.println("Signature A 'E(PRA, NB)': " + sigA); 
        
        //VERIFY SIGNATURE BY DECRYPTING WITH PUBLIC KEY
        String verB = RSAUtil.validateSignature(sigB, publicKeyB);
        String verA = RSAUtil.validateSignature(sigA, publicKeyA);
        System.out.println("Decrypt SigB with Public Key 'E(PRB, NA)': " + verB); 
        System.out.println("Decrypt SigA with Public Key 'E(PRA, NB)': " + verA); 
        
        //ENCRYPTION FOR MESSAGE SENDING (ENCRYPT WITH RECIPIENT'S PUBLIC KEY)
        String eMsgB = RSAUtil.encrypt(sigB, publicKeyA);
        String eMsgA = RSAUtil.encrypt(sigA, publicKeyB);
        String dMsgB = RSAUtil.decrypt(eMsgB, privateKeyA);
        String dMsgA = RSAUtil.decrypt(eMsgA, privateKeyB);
        System.out.println("Encrypted Message 'E(PUA, E(PRB, NA))': " + eMsgB); 
        System.out.println("Encrypted Message 'E(PUB, E(PRA, NB))': " + eMsgA); 
        System.out.println("Decrypted with Private Key 'E(PUA, E(PRB, NA))': " + dMsgB); 
        System.out.println("Decrypted with Private Key 'E(PUB, E(PRA, NB))': " + dMsgA); 
        
        System.out.println("\n\nTest of message sequence: \nIDA||NA\nE(PUA, E(PRB, NA))||NB\nE(PUB, E(PRA, NB))"); 
        String message1="Alice"+" "+ nonceA;
        System.out.println("Alice Sends ID and Nonce: " + message1 ); 
        String[] split = message1.split(" ");
        String ID = split[0];
        String nonce = split[1];
        sigB = RSAUtil.generateSignature(nonce, privateKeyB);
        System.out.println("Bob Recieves Nonce: " + nonce + " From " + split[0]); 
        System.out.println("Bob Encrypts Nonce: " + sigB + " \nand Sends to " + split[0]); 
 
    }
}
