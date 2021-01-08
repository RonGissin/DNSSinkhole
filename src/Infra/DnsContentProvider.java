package Infra;

import java.util.ArrayList;
import java.util.List;

public class DnsContentProvider {
    public DnsContentProvider(DnsPacket dnsPacket)
    {
        // get data.
        _rawDnsData = dnsPacket.get_Data();

        // get answer count.
        _ansCount = (_rawDnsData[6] << 8) + _rawDnsData[7];

        // get authority count.
        _nsCount = (_rawDnsData[8] << 8) + _rawDnsData[9];

        // get response code.
        _responseCode = _rawDnsData[3] & 15;

        // get question name.
        _packetIdx = 12;
        _questionName = getHostName();

        // skip question's QTYPE and QCLASS.
        _packetIdx += 4;

        skipAnswerSection();

        _authority = getFirstAuthorityServer();

        _isFinalAnswer = IsFinalAnswer();
    }


    public String GetPacketQuestionName() {
        System.out.println("DnsContentProvider - returning question name - " + _questionName);

        return _questionName;
    }

    public String GetPacketAuthority() {
        return _authority;
    }

    public boolean IsFinalAnswer()
    {
        return (_ansCount > 0 || _responseCode != 0 || _authority == null) & !isQuery();
    }

    public void SetRecursionAvailable(boolean activatate)
    {
        if(activatate)
        {
            _rawDnsData[3] = (byte)((int)_rawDnsData[3] | 128);
        }
        else
        {
            _rawDnsData[3] = (byte)((int)_rawDnsData[3] & 127);
        }
    }

    public void SetQueryResponse(boolean shouldBeResponse)
    {
        if(shouldBeResponse)
        {
            _rawDnsData[2] = (byte)((int)_rawDnsData[2] | 128);
        }
        else
        {
            _rawDnsData[2] = (byte)((int)_rawDnsData[2] & 127);
        }
    }

    public void SetAuthoritativeAnswer(boolean isAuthoritative)
    {
        if(isAuthoritative)
        {
            _rawDnsData[2] = (byte)((int)_rawDnsData[2] | 4);
        }
        else
        {
            _rawDnsData[2] = (byte)((int)_rawDnsData[2] & 251);
        }
    }

    public void SetRCodeToNXDomain()
    {
        _rawDnsData[3] = (byte)((int)_rawDnsData[3] & 3);
    }

    private String getFirstAuthorityServer()
    {
        if (_nsCount == 0) return null;

        String authorityName = getHostName();

        // skip TYPE, CLASS, TTL, DATALength.
        _packetIdx += 10;

        String nsName = getHostName();

        return nsName;
    }

    private void skipAnswerSection()
    {
        if (_ansCount == 0) return;

        for(int i = 0; i < _ansCount; i++)
        {
            // skip NAME.
            int current = castByteToUnsignedInt(_rawDnsData[_packetIdx]);
            while (current != 0 && current < CReadingPointerThreshold)
            {
                _packetIdx++;
                current = castByteToUnsignedInt(_rawDnsData[_packetIdx]);
            }

            _packetIdx++;

            if(current != 0)
            {
                _packetIdx++;
            }

            // skip TYPE, CLASS and TTL.
            _packetIdx += 8;

            // read RDLENGTH.
            int rdLength = (_rawDnsData[_packetIdx] << 8) + _rawDnsData[_packetIdx + 1];
            _packetIdx += rdLength + 2;
        }
    }

    private String getHostName()
    {
        StringBuilder domainName = new StringBuilder();
        int location = _packetIdx;
        boolean jumpOccurred = false;

        // a list to hold the current label content being read.
        List<Character> labelContent = new ArrayList<>();
        boolean isReadingLengthPart = true;
        int currentLabelContentLength = 0;
        int currentByteInLabelContent = 1;

        int current = castByteToUnsignedInt(_rawDnsData[location]);

        while(current != 0)
        {
            if(current >= CReadingPointerThreshold) // jump to pointer.
            {
                // jump to pointer.
                location = ((current - CReadingPointerThreshold) << 8) + _rawDnsData[location + 1];
                isReadingLengthPart = true;
                jumpOccurred = true;
            }
            else
            {
                if(isReadingLengthPart)
                {
                    currentLabelContentLength = current;
                    isReadingLengthPart = false;
                }
                else // reading content.
                {
                    // if just read last byte of content for this label.
                    if(currentByteInLabelContent == currentLabelContentLength)
                    {
                        // add byte to content so far.
                        isReadingLengthPart = true;
                        currentByteInLabelContent = 1;
                        labelContent.add((char)current);
                        appendLabelToDomainName(domainName, labelContent);
                        // clear content for next label.
                        labelContent.clear();
                    }
                    else // reading some byte in the middle.
                    {
                        currentByteInLabelContent++;
                        labelContent.add((char)current);
                    }
                }

                // move to next byte.
                location++;
                if(!jumpOccurred) _packetIdx++;
            }

            current = castByteToUnsignedInt(_rawDnsData[location]);
        }

        if(jumpOccurred)
        {
            // move ahead of jump length label.
            _packetIdx += 2;
        }
        else
        {
            // move from 0 octet to the next section.
            _packetIdx++;
        }

        // trim the last period.
        domainName.deleteCharAt(domainName.length() - 1);

        return domainName.toString();
    }

    private boolean isQuery()
    {
        if((_rawDnsData[2] & 128) == 0){
            return true;
        }

        return false;
    }

    private static void appendLabelToDomainName(StringBuilder domainName, List<Character> labelContent)
    {
        for (char c: labelContent)
        {
            domainName.append(c);
        }
        domainName.append('.');
    }

    private int castByteToUnsignedInt(byte byteToCast)
    {
        return (int)byteToCast & 0xff;
    }

    private static final int CReadingPointerThreshold = 192;
    private byte[] _rawDnsData;
    private int _responseCode;
    private int _ansCount;
    private int _nsCount;
    private String _questionName;
    private String _authority;
    private boolean _isFinalAnswer;
    private int _packetIdx;



}
