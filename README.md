# SEC-Project

## Instructions
* To run this project download the source code
* Then do ``mvn install`` in the SEC-Project directory 
* And then do ``mvn install`` in the SEC-Communication directory
* To start the server go to the SEC-Server directory and do ``mvn compile exec:java``
* To start a client go to the SEC-Client directory and do ``mvn compile exec:java -DclientNumber=<N>`` 
where `<N>` is the number associated with the `clientKeys<N>.jks` file. A possible command is for example
  ``mvn compile exec:java -DclientNumber=1`` which will use clientKeys1.jks
* The client has a text command line. It prints the possible commands once started.
* The keys used in the command line are in Base64. There are sample keys to copy and paste in the publickeys.csv file