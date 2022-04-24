# SEC-Project

## Instructions
* To run this project download the source code
* Make sure you have sqlite3 installed. You can install the latest version with ``sudo apt-get install sqlite3``
* Then do ``mvn install`` in the SEC-Project directory 
* And then do ``mvn install`` in the SEC-Communication directory
* To start the servers go to the SEC-Server directory and execute the ``run_servers.sh`` script. You may need to give execution permissions beforehand with ``sudo chmod +x run_servers.sh``. You should adapt the script to use your terminal of choice. Default terminal is ``konsole``.
* To start a server manually go to the SEC-Server directory and do
``mvn compile exec:java -DreplicaNumber=<R> -Dreset=<yes/no>``, where ``<R>`` is the number of the replica, which will determine which port and .jks file it  is going to use
and ``-Dreset=<yes/no>`` determines whether the database should be reset. A possible command is for example, ``mvn compile exec:java -DreplicaNumber=1 -Dreset="no"``, which will result
in a server listening on port 8080, will use the keys on the serverKeys1.jks file and not reset the database.
The formula for the port is ``8079 + replicaNumber`` and for the .jks file is ``serverKeys<R>.jks``.
* To start a client go to the SEC-Client directory and do 
``mvn compile exec:java -DclientNumber=<C> -Df=<F>`` 
where `<C>` is the number associated with the `clientKeys<C>.jks` file and `<F>` indicates the number of faults that will need to be tolerated.
A possible command is for example, ``mvn compile exec:java -DclientNumber=1 -Df=1`` 
which will use `clientKeys1.jks`, work properly, will try to connect to 4 replicas and will have quorum size of 3. The formulas for the number of replicas and quorum size
are respectively ``3f+1`` and ``2f+1``.
* The client has a text command line. It prints the possible commands once started.
* The keys used in the command line are in Base64. There are sample keys to copy and paste in the publickeys.csv file
* Tests can be run using IntelliJ, however 4 servers must already be connected for them to succeed.
* The command line also prints it's own public key from the `clientKeys<N>.jks`
* The maven version is Maven 3.8.4. The Java version is 17.0.2.

