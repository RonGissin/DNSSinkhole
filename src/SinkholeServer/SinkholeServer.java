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
        this._listenPort = listenPort;
        this._serverSocket = null;
        this._domainEnforcer = domainEnforcer;
        this._rootProvider = new RootDnsServerProvider();
        this._dnsPacketFactory = new DnsPacketFactory();
    }

    /**
     * Start the server.
     */
    public void Start()
    {
        byte[] requestData = new byte[DnsOperationsConsts.DnsUdpPacketSize];
        byte[] sendData;

        tryInitServerSocket();

        while (true) {
            // receive datagram packet.
            DatagramPacket receiveDatagram = new DatagramPacket(requestData, requestData.length);
            tryReceivePacket(receiveDatagram);

            // extract dns data.
            byte[] rawDnsData = receiveDatagram.getData();

            DnsPacket dnsPacket = _dnsPacketFactory.Create(rawDnsData);

            // whitelist the dns domain.
            if(_domainEnforcer.IsAllowed(dnsPacket.QDomainName))
            {
                RespondWithBadDomain();
                continue;
            }

            // get random root dns server ip.
            InetAddress rootServer = tryGetRootIpAddress();

            // TODO: Create new DNSClient here that will manage interaction with auth servers.
            // send datagram packet to the root.
            DatagramPacket sendRootDatagram = new DatagramPacket(
                                                    requestData,
                                                    requestData.length,
                                                    rootServer,
                                                    DnsOperationsConsts.DnsPort);

//            InetAddress IPAddress = receiveDatagram.getAddress();
//            int port = receiveDatagram.getPort();
//            String capitalizedSentence = sentence.toUpperCase();
//
//            sendData = capitalizedSentence.getBytes();
//
//            DatagramPacket sendPacket =
//                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
//            _serverSocket.send(sendPacket);
        }
    }

    private InetAddress tryGetRootIpAddress()
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

    private void RespondWithBadDomain()
    {

    }

    private void tryReceivePacket(DatagramPacket receivePacket) {
        try
        {
            this._serverSocket.receive(receivePacket);
        }
        catch(IOException e)
        {
            System.out.println(String.format("Exception occured while trying to receive packet. exception = {0}", e));
        }
    }

    private void tryInitServerSocket()
    {
        try
        {
            this._serverSocket = new DatagramSocket(9876);

        }
        catch (SocketException e)
        {
            System.out.println(String.format("Exception occured initializing server socket. exception = {0}", e));
        }
    }

    private DatagramSocket _serverSocket;
    private DomainEnforcer _domainEnforcer;
    private RootDnsServerProvider _rootProvider;
    private DnsPacketFactory _dnsPacketFactory;
    private int _listenPort;
}