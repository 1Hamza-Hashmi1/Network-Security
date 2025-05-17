-ip addr is the same bc its running on the same machine
-to tell the difference of the client, i display the port numbers

-only server was changed:
-client handler class was added to allow multiple clients which gives each client its own thread
-while loop (line 21) was added to continually accept more clients (allows server to continue listening)
-override makes sure that communication with each client happens independently of other client
