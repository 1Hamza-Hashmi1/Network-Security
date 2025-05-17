symmetric key-based authenticated key exchange
-a shared key (SHARED_KEY_STRING) is used for auth (used to encrypt login credintial between client and server)
-then server generat a Master Secret (generateAESKey / shared symmetric key) and sends to client using shared key
	-Master Secret helps both dereive a encrytion key and a MAC key
-Transactions are encrypted using the encrytion key , HMAC us calculated using the MAC key

-is a combo of Kerberos + TLS 

-server is always online
-client is started up and connects to server
-client puts in credential and either logins or signs up
     -credentials are sent to server using a symmetric key (assumed that client and server already have it)
-once credentials are verified, server generates a new Master Secret (symmetric AES key) and sends to client encrypted in symmetric key
-client and server decrypts Master Secret using shared key
     -Master Secret consists of an encryption key + MAC key
-the client either does withdraw, deposit or balance which is sent to server side encrypted in encryption key and and HMAC is generated
,both of which are sent to server
-on server side HMAC is verified using HMAC
     -if HMAC is correct, then decrypts message and process 1 of the 3 actions

Simulate MAC Failure
-message is still encrypted and sent normally but uses a fake MAC key to generate HMAC
-when server computes HMAC, it does not match and is invalid
