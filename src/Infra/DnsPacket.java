package Infra;

public class DnsPacket {

    public String QDomainName;
    public String ResponseIp;

    public DnsPacket(String qDomainName, String responseIp)
    {
        QDomainName = qDomainName;
        ResponseIp = responseIp;
    }
}
