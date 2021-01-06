package Infra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DnsContentProvider {
    public DnsContentProvider(DnsPacket dnsPacket)
    {
        // get data.
        _rawDnsData = dnsPacket.getData();

        // get answer count.
        _ansCount = (_rawDnsData[6] << 8) + _rawDnsData[7];

        // get authority count.
        _nsCount = (_rawDnsData[8] << 8) + _rawDnsData[9];

        // get response code.
        _responseCode = _rawDnsData[3] & 15;

        // get question name.
        _packetIdx = 12;
        _questionName = getHostName();

        skipAnswerSection();

        _authority = getFirstAuthorityServer();

        _isFinalAnswer = IsFinalAnswer();
    }


    public String get_questionName() {
        System.out.println(_questionName);

        return _questionName;
    }

    public String get_authority() {
        return _authority;
    }

    public boolean IsFinalAnswer()
    {
        return _ansCount > 0 || _responseCode != 0 || _authority == null;
    }

    private String getFirstAuthorityServer()
    {
        if (_nsCount == 0) return null;

        // skip NAME.
        int current = _rawDnsData[_packetIdx];
        while (current != 0 && current < CReadingPointerThreshold)
        {
            _packetIdx++;
            current = _rawDnsData[_packetIdx];
        }

        _packetIdx++;

        if(current != 0)
        {
            _packetIdx++;
        }

        // skip TYPE, CLASS and TTL.
        _packetIdx += 8;

        // skip rdLength
        _packetIdx += 2;

        return getHostName();
    }

    private void skipAnswerSection()
    {
        if (_ansCount == 0) return;

        for(int i = 0; i < _ansCount; i++)
        {
            // skip QTYPE and QCLASS.
            _packetIdx += 2;

            // skip NAME.
            int current = _rawDnsData[_packetIdx];
            while (current != 0 && current < CReadingPointerThreshold)
            {
                _packetIdx++;
                current = _rawDnsData[_packetIdx];
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

        // a list to hold the current label content being read.
        List<Character> labelContent = new ArrayList<>();
        boolean isReadingLengthPart = true;
        int currentLabelContentLength = 0;
        int currentByteInLabelContent = 1;

        int current = _rawDnsData[location];

        while(current != 0)
        {
            if(current >= CReadingPointerThreshold) // jump to pointer.
            {
                // jump to pointer.
                location = ((current - CReadingPointerThreshold) << 8) + _rawDnsData[location + 1];
                isReadingLengthPart = true;
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
                _packetIdx++;
            }

            current = _rawDnsData[location];
        }

        // trim the last period.
        domainName.deleteCharAt(domainName.length() - 1);

        return domainName.toString();
    }

    private static void appendLabelToDomainName(StringBuilder domainName, List<Character> labelContent)
    {
        for (char c: labelContent)
        {
            domainName.append(c);
        }
        domainName.append('.');

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
