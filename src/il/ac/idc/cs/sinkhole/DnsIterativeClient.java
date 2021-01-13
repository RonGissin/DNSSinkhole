package il.ac.idc.cs.sinkhole;

import java.io.IOException;
import java.net.*;

public class DnsIterativeClient {
    public DnsIterativeClient()
    {
        _receiveData = new byte[DnsConsts.DnsUdpPacketSize];

        try
        {
            _clientSocket = new DatagramSocket(DnsConsts.DnsClientPort);
        }
        catch (SocketException e)
        {
            System.err.printf("exception on opening client udp socket - {0}", e);
        }
    }

    public DnsPacket GetResponsePacket(DatagramPacket requestPacket, InetAddress rootAddress)
    {
        DnsPacket dnsOriginalRequest = new DnsPacket(requestPacket);

        DatagramPacket udpPacketForRoot = new DatagramPacket(
                dnsOriginalRequest.get_Data(),
                dnsOriginalRequest.get_Data().length,
                rootAddress,
                DnsConsts.DnsClientPort);

        // send query to root.
        trySendQueryUdpPacket(udpPacketForRoot);
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
                        dnsOriginalRequest.get_Data(),
                        dnsOriginalRequest.get_Data().length,
                        InetAddress.getByName(responseDnsPacket.get_authority()),
                        DnsConsts.DnsClientPort);
            } catch (UnknownHostException e) {
                System.err.printf("An unknown host exception occurred when retrieving the auth server ip. error={0}", e);
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
        }
        catch (IOException e)
        {
            System.err.printf("Exception occurred while trying to send query packet to dns server. exception = {0}", e);
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
            System.err.printf("Exception occured while trying to receive packet from dns server. exception = {0}", e);
        }

        return receivePacket;
    }

    private DatagramSocket _clientSocket;
    private byte[] _receiveData;
    private static final int CMaxIterations = 16;
}
