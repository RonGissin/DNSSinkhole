package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.*;

public class DnsIterativeClient {
    public DnsIterativeClient()
    {
        _receiveData = new byte[DnsOperationsConsts.DnsUdpPacketSize];

        try
        {
            _clientSocket = new DatagramSocket(DnsOperationsConsts.DnsClientPort);
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
                nsServerAddress,
                DnsOperationsConsts.DnsClientPort);

        // send query to root.
        trySendQueryUdpPacket(packetForRoot);
        // get response from root.
        DatagramPacket receivePacket = tryReceiveUdpPacket();

        DnsPacket responseDnsPacket = new DnsPacket(receivePacket);

        int iterationNumber = 1;

        // run on all servers.
        while(!responseDnsPacket.IsFinalAnswer() && iterationNumber < CMaxIterations) {
            //send to dns server
            DatagramPacket packetForNextDNS = null;
            try {
                packetForNextDNS = new DatagramPacket(
                        requestPacket.getData(),
                        requestPacket.getData().length,
                        InetAddress.getByName(responseDnsPacket.get_authority()),
                        DnsOperationsConsts.DnsClientPort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            trySendQueryUdpPacket(packetForNextDNS);

            // get response from dns server.
            receivePacket = tryReceiveUdpPacket();

            responseDnsPacket = new DnsPacket(receivePacket);
        }

        // flip necessary bits.
        responseDnsPacket.set_RecursionAvailable(true);
        responseDnsPacket.set_AuthoritativeAnswer(false);

        return responseDnsPacket;
    }

    private void trySendQueryUdpPacket(DatagramPacket responsePacket)
    {
        try
        {
            System.out.println("DnsIterativeClient - sending out datagram to authority/root server.");
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
            System.out.println("DnsIterativeClient - awaiting response from authority server.");
            this._clientSocket.receive(receivePacket);
            System.out.println("DnsIterativeClient - received response from authority server - " + receivePacket.getAddress().toString());
        }
        catch(IOException e)
        {
            System.out.println(String.format("Exception occured while trying to receive packet from dns server. exception = {0}", e));
        }

        return receivePacket;
    }

    private DatagramSocket _clientSocket;
    private byte[] _receiveData;
    private static final int CMaxIterations = 16;
}
