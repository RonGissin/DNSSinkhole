package Infra;

public class DnsPacket {
    public DnsPacket(byte[] rawDnsData)
    {
        _rawDnsData = rawDnsData;
        _qDomainName = DnsPacketContentUtils.ExtractQDomainName(_rawDnsData);
        _responseIp = DnsPacketContentUtils.ExtractResponseIpAddress(_rawDnsData);
    }

    public String getQDomainName() {
        return _qDomainName;
    }

    private byte[] _rawDnsData;
    private String _qDomainName;
    private String _responseIp;
}
