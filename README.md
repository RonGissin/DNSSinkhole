# DNSSinkhole

## Names 
Ron Gissin - 313544298\
Aryeh Klein - 323746016

## Project files and their purpose

### SinkholeServer

The program's entrypoint.
orchestrates the dns sinkhole server end to end flow,
starting by listening for requests on port 5300, 
and returning responses to clients.

### RecursiveServer

This file defines a class that takes care of processing
a request from a client (port 5300) up until returning
a final response.

### DnsIterativeClient

This file defines a class that handles the communication
with intermediate dns servers such as the root, the authority servers, etc..
It serves as a client to those intermediary servers.

### DnsPacket

This file defines a class that encapsulates properties 
of a single dns packet.
The constructor is given the raw dns packet data, 
and extracts all te relevant properties.

### DnsConsts

This class is a pseudo static class (no static classes in java..)
that holds constants relating to dns operations and data.

### BlockListLoader

This file defines a class that loads a block list of 
bad domains from a given file (the format is - domain per line)
and loads it to a HashSet.

### DomainEnforcer

This file defines a class that enforces/validates 
that a given domain is approved for processing by the server.
Works in context of a given block list file.

### RootDnsServerAddressProvider

This file defines a class that provides the ip address
of a random root dns server.