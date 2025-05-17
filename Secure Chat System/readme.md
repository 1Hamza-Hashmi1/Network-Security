summary:
-using AES for encryption 
-KDC share common session key between A, B and C

-assume it is a continuation of "hybrid key distribution protocol", so that means A, B and C already have their own RSA key pairs (both public and private)
     -Clients send their public key to KDC (stored in KDC as a map), and it shares everyone public key to each other

-A, B and C send msgs encrypted in session key and attached with a digital signature
     -when msg received, it verifies the signature using the sender's publicKey
     -the decrypts msg 

+so far securing chat messages with encryption, now remove threat of replay:
-message sent now has seq # attached and then encrypted 
-receiver chks sequence number to ensure it is greater than the last received sequence number from that client.
-seq # starts at 0 and is inc after every msg 
-if seq # is less than or equal to last seq # received, message is rejected

-simulateReplayAttack method resends a captured message with the same sequence number.

Note: after the message is received and signature is verified using the sender's public key, the client decrypt message using session key and then the client compares the sequence number
