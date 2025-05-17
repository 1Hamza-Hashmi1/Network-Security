The server (KDC) then generated a session key KAB and separately send it to A and B

Need to:
(1) Generate RSA public/private key pairs for KDC, A and B
(2) Encrypt and decrypt messages using RSA
(3) Pick up IDs, KA, KB, KAB and nonces by yourself.


changes:
-A wants to talk to B so it makes a connection with KDC and KDC makes a KAB(session key)
-kdc encrypts KAB with KA and sends it to A AND then opens a connection with B, encrypts KAB with B and sends that to B
-A decrypts msg with her private key and B decrypts msg with his private key
-now they both have KAB which allows both of them to talk

-A encrypts msg with session key, send to B, and B decrypts with session key


Demo Q and A
-Vulnerable bc no authentication mechanism between Alice and Bob after receiving session key (KAB)
-atker could impersonate B, request a session key, and trick Alice into communicating with atker. A encrypts with KAB and atker decrypts with KAB

-replay possible with nonce or timestamp used
