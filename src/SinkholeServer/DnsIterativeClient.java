package SinkholeServer;

import Infra.DnsOperationsConsts;
import Infra.DnsPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DnsIterativeClient {
    public DnsIterativeClient()
    {
        _receiveData = new byte[DnsOperationsConsts.DnsUdpPacketSize];

        try
        {
            _clientSocket = new DatagramSocket();
        }
        catch (SocketException e)
        {
            System.out.println(String.format("exception on opening client udp socket - {0}", e));
        }
    }

    public DnsPacket GetResponsePacket(DatagramPacket requestPacket, InetAddress rootAddress)
    {
        InetAddress nsServerAddress = rootAddress;

        DatagramPacket packetForRoot = new DatagramPacket(
                requestPacket.getData(),
                requestPacket.getData().length,
                rootAddress,
                DnsOperationsConsts.DnsPort);

        // send query to root.
        trySendResponseUdpPacket(packetForRoot);
        // get response from root.
        DatagramPacket receivePacket = tryReceiveUdpPacket();

        DnsPacket ResponseDnsPacket = new DnsPacket(receivePacket);

        // run on all servers.
        while(!ResponseDnsPacket.IsFinalAnswer()) {
            //send to dns server
            DatagramPacket packetForNextDNS = new DatagramPacket(
                    requestPacket.getData(),
                    requestPacket.getData().length,
                    InetAddress.getByName(ResponseDnsPacket.get_authority()),
                    DnsOperationsConsts.DnsPort);

            trySendResponseUdpPacket(packetForNextDNS);

            // get response from dns servee.
            receivePacket = tryReceiveUdpPacket();

            ResponseDnsPacket = new DnsPacket(receivePacket);
        }

        return ResponseDnsPacket;

        }

    private void trySendResponseUdpPacket(DatagramPacket responsePacket)
    {
        try
        {
            _clientSocket.send(responsePacket);
        } catch (IOException e)
        {
            System.out.println(String.format("Exception occurred while trying to send query packet to dns server. exception = {0}", e));
        }
    }

    private DatagramPacket tryReceiveUdpPacket() {
        DatagramPacket receivePacket = new DatagramPacket(_receiveData, _receiveData.length);

        try
        {
            this._clientSocket.receive(receivePacket);
        }
        catch(IOException e)
        {
            System.out.println(String.format("Exception occured while trying to receive packet from dns server. exception = {0}", e));
        }

        return receivePacket;
    }

    private DatagramSocket _clientSocket;
    private byte[] _receiveData;
    private byte[] _requestData;
    private InetAddress _rootAddress;
    private static final int CMaxIterations = 16;
}
