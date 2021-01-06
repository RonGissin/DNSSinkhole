package Infra;

import java.net.DatagramPacket;

public class DnsPacket {
    public DnsPacket(DatagramPacket udpPacket)
    {
        _rawDnsData = udpPacket.getData();
        _provider = new DnsContentProvider(this);

        _qDomainName = _provider.get_questionName();
        _isAnswer = _provider.IsFinalAnswer();
        _firstAuthorityName = _provider.get_authority();
    }

    public byte[] getData()
    {
        return _rawDnsData;
    }

    public String getQDomainName()
    {
        return _qDomainName;
    }

    public boolean IsFinalAnswer()
    {
        return _isAnswer;
    }

    public String get_authority() {
        return _firstAuthorityName;
    }

    private byte[] _rawDnsData;
    private String _qDomainName;
    private boolean _isAnswer;
    private DnsContentProvider _provider;
    private String _firstAuthorityName;
}
