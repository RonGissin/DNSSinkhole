package SinkholeServer;

import java.io.IOException;
import java.net.*;
import Infra.*;

/**
 * Class that defines the server object that runs the entire flow of dns requests and responses
 */
class SinkholeServer {
    /**
     * constructor.
     * @param listenPort - the port to listen on.
     */
    public SinkholeServer(int listenPort, DomainEnforcer domainEnforcer)
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
        byte[] sendData;

        tryInitServerSocket();

        while (true) {
            System.out.println("SinkholeServer - listening for udp packets.");
            // receive datagram packet.
            DatagramPacket requestUdpPacket = tryReceiveUdpPacket();
            System.out.println("SinkholeServer - received udp packet from client.");
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

            trySendResponseUdpPacket(responseUdpPacket);
        }
    }

    private void trySendResponseUdpPacket(DatagramPacket responsePacket)
    {
        try
        {
            _serverSocket.send(responsePacket);
        } catch (IOException e)
        {
            System.out.println(String.format("Exception occurred while trying to send response packet to client. exception = {0}", e));
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
            System.out.println(String.format("Exception occurred while trying to fetch ip address of root. exception = {0}", e));
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

        System.out.println("SinkholeServer - Responding with bad domain.");
        trySendResponseUdpPacket(udpFinalResponseToClient);
    }

    private DatagramPacket tryReceiveUdpPacket() {
        byte[] receiveData = new byte[DnsOperationsConsts.DnsUdpPacketSize];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try
        {
            this._serverSocket.receive(receivePacket);
        }
        catch(IOException e)
        {
            System.out.println(String.format("Exception occured while trying to receive packet. exception = {0}", e));
        }

        return receivePacket;
    }

    private void tryInitServerSocket()
    {
        try
        {
            this._serverSocket = new DatagramSocket(_listenPort);
            System.out.println("SinkholeServer - Server started successfully.");
        }
        catch (SocketException e)
        {
            System.out.println(String.format("Exception occured initializing server socket. exception = {0}", e));
        }
    }

    private DatagramSocket _serverSocket;
    private final DomainEnforcer _domainEnforcer;
    private final RootDnsServerAddressProvider _rootProvider;
    private final DnsIterativeClient _iterativeClient;
    private final int _listenPort;
}