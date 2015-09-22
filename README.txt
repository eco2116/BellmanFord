Evan O'Connor (eco2116)
Programming Assignment 2: Bellman Ford
CSEE 4119

--

Compiling:

Unzip zip file eco2116_java.zip
Enter eco2116_java directory
Type make

--

Running program:

To run the client with a config file "client0.txt", run the following command:
java Client client0.txt

--

Design Details:

Most of the implementation of the Bellman Ford algorithm happens in the Client class. I use the main thread
to accept user input as commands, and start a listener thread to wait for messages from other clients, and a
Timer thread which handles the timeout "pulses". The DistanceVector class handles some of the work of link change,
the Node class handles details about IP/port numbers, and the NodeWeight class is used to keep track of the weights
of links when links are linked down (isDead is set to true when a link goes down, so we can still hold the value
of the weight if the link is linked up again). I created a superclass Message, which has subclasses, ApplicationMessage
(used for file transfer), LinkMessage (used for link up, link down, link change cost), and NetworkMessage (used for
route updates with Distance vectors). The DataPacket class allows me to attach sender information to a message.
Finally, I put all of the work of transferring and forwarding files in the FileHelper class, which is utilized by
the Client class in the actual process of sending files across the distributed network.

--

Commands:

>LINKDOWN <ip> <port>
destroy an existing link by setting the cost to infinity (Double.MAX_VALUE), sets NodeWeight to isDead=true

>LINKUP <ip> <port>
restore a link that was previous linked down, sets the NodeWeight to isDead=false

>CHAGNECOST <ip> <port> <cost>
Changes the weight of a given link

>SHOWRT
Displays the current routing table of the client who issued the command

>CLOSE
Closes down the current client. Other nodes will recognize this after 3*TIMEOUT

>TRANSFER <filename> <destination_ip> <port>
Sends a file through the hops given by the current routing table.



