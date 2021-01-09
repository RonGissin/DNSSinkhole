package Infra;

import java.net.DatagramPacket;

public class DnsPacket {
    public DnsPacket(DatagramPacket udpPacket)
    {
        _rawDnsData = udpPacket.getData();
        _provider = new DnsContentProvider(this);

        _qDomainName = _provider.GetPacketQuestionName();
        _isAnswer = _provider.IsFinalAnswer();
        _firstAuthorityName = _provider.GetPacketAuthority();
    }

    public byte[] get_Data()
    {
        return _rawDnsData;
    }

    public String get_QDomainName()
    {
        return _qDomainName;
    }

    public void set_RequestResponse(boolean shouldBeResponse)
    {
        _provider.SetQueryResponse(shouldBeResponse);
    }

    public void set_RecursionAvailable(boolean activate)
    {
        _provider.SetRecursionAvailable(activate);
    }

    public void set_AuthoritativeAnswer(boolean activate)
    {
        _provider.SetRecursionAvailable(activate);
    }

    public void set_RCodeToNXDomain()
    {
        _provider.SetRCodeToNXDomain();
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
