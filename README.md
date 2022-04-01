# SEC-Project

## Instructions
* To run this project download the source code
* Make sure you have sqlite3 installed. You can install the latest version with ``sudo apt-get install sqlite3``
* Then do ``mvn install`` in the SEC-Project directory 
* And then do ``mvn install`` in the SEC-Communication directory
* To start the server go to the SEC-Server directory and do ``mvn compile exec:java``
* To start a client go to the SEC-Client directory and do ``mvn compile exec:java -DclientNumber=<N> -DbadChannel=<yes/no>`` 
where `<N>` is the number associated with the `clientKeys<N>.jks` file, and -DbadChannel indicates if it should use a faulty channel. A possible command is for example
  ``mvn compile exec:java -DclientNumber=1 -DbadChannel=no`` which will use `clientKeys1.jks` and work properly
* The client has a text command line. It prints the possible commands once started.
* The keys used in the command line are in Base64. There are sample keys to copy and paste in the publickeys.csv file
* You can run tests using IntelliJ
* The command line also prints it's own public key from the `clientKeys<N>.jks`
* The maven version is Maven 3.8.4. The Java version is 17.0.2.
