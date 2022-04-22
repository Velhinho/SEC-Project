# SEC-Project

## Instructions
* To run this project download the source code
* Make sure you have sqlite3 installed. You can install the latest version with ``sudo apt-get install sqlite3``
* Then do ``mvn install`` in the SEC-Project directory 
* And then do ``mvn install`` in the SEC-Communication directory
* To start the servers go to the SEC-Server directory and execute the ``run_servers.sh`` script. You may need to give execution permissions beforehand with ``sudo chmod +x run_servers.sh``. You should adapt the script to use your terminal of choice. Default terminal is ``konsole``.
* To start a client go to the SEC-Client directory and do 
``mvn compile exec:java -DclientNumber=<C> -DbadChannel=<yes/no> -DquorumSize=<Q> -DnumberReplicas=<R>`` 
where `<C>` is the number associated with the `clientKeys<C>.jks` file, 
-DbadChannel indicates if it should use a faulty channel,
<Q> is the size of the quorum, and
<R> is the number of server replicas,
A possible command is for example, ``mvn compile exec:java -DclientNumber=1 -DbadChannel=no -DquorumSize=3 -DnumberReplicas=4`` 
which will use `clientKeys1.jks`, work properly, and use a quorum size of 3 for 4 replicas.
* The client has a text command line. It prints the possible commands once started.
* The keys used in the command line are in Base64. There are sample keys to copy and paste in the publickeys.csv file
* Tests can be run using IntelliJ
* The command line also prints it's own public key from the `clientKeys<N>.jks`
* The maven version is Maven 3.8.4. The Java version is 17.0.2.
