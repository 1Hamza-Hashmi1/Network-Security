Project 1
-using AES - encryption algorithim, uses 1 shared key
-B opens socket connection and A connects
-A sends her ID to B with a noce value attatched
-B encrypts the msg A sent with AES and sends to A with B's own generated nonce
-A decrypts msg, and will find that her nonce value is in msg. Thus Alice can authincate B
-A sneds encrypted msg to B and when B decrypts he gets his nonce value back, B authenticate A

--------------------------------------------------

Project 3
-gen RSA key pairs
-Signing a message using A PR
-Verifying the signature A PU

-make a generator
-then define key size you want
-then generate the key pair
-extract PU and PR from key pair

-alice makes a message and the timestamp is attatched to it

-alice signs the messgae with her private key

purpose:
-to fend of agianst replay atks, a nonce vcalue, sequence numbe or a timestamp should be attatched to the msg 
-this is done bacuse if a hacker got a hold of the msg that was being sent and changed the msg, the nonce value, seq # or timestamp would chage
-we used a timestamp for simplicity and attatch that to the msg that alice is sneding
	-if the msg get tampered, then the timestamp value changes 
	-if the msg is too old, then on bobs side he will know that the msg had been tampered with as he should have recived it within sec
-on bobs side 
	-it chks the nonce and chks the timestamp, and chks to see if valid


case1: signature is valid
-no changes
case2: message is tampered
-line 53 A - out.println("Hello Bob, this is Eve!|" + nonce);
	  or out.println(messageWithNonce + "1");
case3: nonce is too old
-line 27 A - System.currentTimeMillis() - 600001; // Use an old timestamp (10 min old)
