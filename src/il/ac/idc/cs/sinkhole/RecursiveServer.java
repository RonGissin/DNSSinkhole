package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.*;

/**
 * Class that defines the server object that runs the entire flow of dns requests and responses
 */
class RecursiveServer {
    /**
     * Creates a new recursive dns server, that will listen on given port,
     * and use given domain enforcer to block certain domain requests.
     * @param listenPort - the port to listen on.
     * @param domainEnforcer - the enforcer of domains.
     */
    public RecursiveServer(int listenPort, DomainEnforcer domainEnforcer)
    {
        _listenPort = listenPort;
        _serverSocket = null;
        _domainEnforcer = domainEnforcer;
        _rootProvider = new RootDnsServerAddressProvider();
        _iterativeClient = new DnsIterativeClient();
    }

    /**
     * Start the server.
     */
    public void Start()
    {
        tryInitServerSocket();

        while (true) {
            System.out.println("RecursiveServer - listening for udp packets.");
            // receive datagram packet.
            DatagramPacket requestUdpPacket = tryReceiveUdpPacket();
            System.out.println("RecursiveServer - received udp packet from client.");
            DnsPacket requestDnsPacket = new DnsPacket(requestUdpPacket);

            // whitelist the dns domain.
            if(!_domainEnforcer.IsAllowed(requestDnsPacket.get_QDomainName()))
            {
                respondWithBadDomain(requestDnsPacket, requestUdpPacket.getAddress(), requestUdpPacket.getPort());
                continue;
            }

            // get random root dns server ip.
            InetAddress rootServerAddress = tryGetRootServerAddress();

            DnsPacket responseDnsPacket = _iterativeClient.GetResponsePacket(requestUdpPacket, rootServerAddress);

            DatagramPacket responseUdpPacket = new DatagramPacket(
                    responseDnsPacket.get_Data(),
                    responseDnsPacket.get_Data().length,
                    requestUdpPacket.getAddress(),
                    requestUdpPacket.getPort());

            System.out.println("RecursiveServer - returning final answer to client.");
            trySendResponseUdpPacket(responseUdpPacket);
        }
    }

    private void trySendResponseUdpPacket(DatagramPacket responsePacket)
    {
        try
        {
            _serverSocket.send(responsePacket);
        }
        catch (IOException e)
        {
            System.err.printf("Exception occurred while trying to send response packet to client. exception = %s", e);
        }
    }

    private InetAddress tryGetRootServerAddress()
    {
        InetAddress rootServer = null;

        try
        {
            rootServer = _rootProvider.GetRootIpAddress();
        }
        catch(UnknownHostException e)
        {
            System.err.printf("Exception occurred while trying to fetch ip address of root. exception = %s", e);
        }

        return rootServer;
    }

    private void respondWithBadDomain(DnsPacket packetToModifyAndReturn, InetAddress clientAddress, int clientPort)
    {
        packetToModifyAndReturn.set_RCodeToNXDomain();
        packetToModifyAndReturn.set_RequestResponse(true);
        packetToModifyAndReturn.set_RecursionAvailable(true);

        DatagramPacket udpFinalResponseToClient = new DatagramPacket(
                packetToModifyAndReturn.get_Data(),
                packetToModifyAndReturn.get_Data().length,
                clientAddress,
                clientPort);

        System.out.println("RecursiveServer - Responding with bad domain.");
        trySendResponseUdpPacket(udpFinalResponseToClient);
    }

    private DatagramPacket tryReceiveUdpPacket() {
        byte[] receiveData = new byte[DnsConsts.DnsUdpPacketSize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try
        {
            this._serverSocket.receive(receivePacket);
        }
        catch(IOException e)
        {
            System.err.printf("Exception occured while trying to receive packet. exception = %s", e);
        }

        return receivePacket;
    }

    private void tryInitServerSocket()
    {
        try
        {
            this._serverSocket = new DatagramSocket(_listenPort);
            System.out.printf("RecursiveServer - Server started listening on port %s.\r\n", _listenPort);
        }
        catch (SocketException e)
        {
            System.err.printf("Exception occurred initializing server socket. exception = %s", e);
        }
    }

    private DatagramSocket _serverSocket;
    private final DomainEnforcer _domainEnforcer;
    private final RootDnsServerAddressProvider _rootProvider;
    private final DnsIterativeClient _iterativeClient;
    private final int _listenPort;
}