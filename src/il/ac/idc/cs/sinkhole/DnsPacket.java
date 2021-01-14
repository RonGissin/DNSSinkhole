package il.ac.idc.cs.sinkhole;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that defines a dns packet that resides inside a udp segment.
 */
public class DnsPacket {
    /**
     * Creates a new instance of dns packet, takes all necessary information
     * from the given udp packet raw data.
     * @param udpPacket - the udp packet in which the dns data resides.
     */
    public DnsPacket(DatagramPacket udpPacket)
    {
        // get data.
        _rawDnsData = udpPacket.getData();

        // get answer count.
        _ansCount = (_rawDnsData[DnsConsts.CAnsCountByteIndex] << CByte)
                + _rawDnsData[DnsConsts.CAnsCountByteIndex + 1];

        // get authority count.
        _nsCount = (_rawDnsData[DnsConsts.CNsCountByteIndex] << CByte)
                + _rawDnsData[DnsConsts.CNsCountByteIndex + 1];

        // get response code.
        _responseCode = _rawDnsData[DnsConsts.CRCodeByteIndex] & 15;

        // get question name.
        _packetIdx = DnsConsts.DnsHeaderLength;
        _questionName = getHostName();

        // skip question's QTYPE and QCLASS.
        _packetIdx += DnsConsts.CRRTypeSize + DnsConsts.CRRClassSize;

        skipAnswerSection();
        _firstAuthorityName = getFirstAuthorityServerAndSkipRest();

        // remove additional RRs.
        _rawDnsData = Arrays.copyOfRange(_rawDnsData, 0, _packetIdx);

        SetZeroAdditionalRecords();
    }

    /**
     * Gets the raw dns data.
     * @return a byte array of dns data.
     */
    public byte[] get_Data()
    {
        return _rawDnsData;
    }

    /**
     * Gets the question domain name.
     * @return the question domain name.
     */
    public String get_QDomainName()
    {
        return _questionName;
    }

    /**
     * Sets the QR flag.
     * @param shouldBeResponse - boolean flag to determine what bit to write.
     */
    public void set_RequestResponse(boolean shouldBeResponse)
    {
        if(shouldBeResponse)
        {
            _rawDnsData[DnsConsts.CQRFlagByteIndex] =
                    (byte)((castByteToUnsignedInt(_rawDnsData[DnsConsts.CQRFlagByteIndex]) & 127) + 128);
        }
        else
        {
            _rawDnsData[DnsConsts.CQRFlagByteIndex]
                    = (byte)(castByteToUnsignedInt(_rawDnsData[DnsConsts.CQRFlagByteIndex]) & 127);
        }
    }

    /**
     * Sets the RA flag.
     * @param activate - boolean flag to determine what bit to write.
     */
    public void set_RecursionAvailable(boolean activate)
    {
        if(activate)
        {
            _rawDnsData[DnsConsts.CRCodeByteIndex] =
                    (byte)((castByteToUnsignedInt(_rawDnsData[DnsConsts.CRCodeByteIndex]) & 127) + 128);
        }
        else
        {
            _rawDnsData[DnsConsts.CRCodeByteIndex] =
                    (byte)(castByteToUnsignedInt(_rawDnsData[DnsConsts.CRCodeByteIndex]) & 127);
        }
    }

    /**
     * Sets the AA flag.
     * @param isAuthoritative - boolean flag to determine what bit to write.
     */
    public void set_AuthoritativeAnswer(boolean isAuthoritative)
    {
        if(isAuthoritative)
        {
            _rawDnsData[DnsConsts.CQRFlagByteIndex] =
                    (byte)((castByteToUnsignedInt(_rawDnsData[DnsConsts.CQRFlagByteIndex]) & 251) + 4);
        }
        else
        {
            _rawDnsData[DnsConsts.CQRFlagByteIndex] =
                    (byte)(castByteToUnsignedInt(_rawDnsData[DnsConsts.CQRFlagByteIndex]) & 251);
        }
    }


    /**
     * Sets the RCode to NXDomain.
     */
    public void set_RCodeToNXDomain()
    {
        _rawDnsData[DnsConsts.CRCodeByteIndex] =
                (byte)((castByteToUnsignedInt(_rawDnsData[DnsConsts.CRCodeByteIndex]) & 240) + 3);
    }

    /**
     * Returns true if this packet is a final answer for the client.
     * @return true, if this is a final answer. false, otherwise.
     */
    public boolean IsFinalAnswer()
    {
        return (_ansCount > 0 || _responseCode != 0 || _firstAuthorityName == null) & !isQuery();
    }

    /**
     * Gets the authority server given in this packet data.
     * If there is no authority, the method returns null.
     * @return the authority name, or null.
     */
    public String get_authority() {
        return _firstAuthorityName;
    }

    private String getFirstAuthorityServerAndSkipRest()
    {
        if (_nsCount == 0) return null;

        // skip authority name.
        getHostName();

        // skip TYPE, CLASS, TTL, DATALength.
        _packetIdx += DnsConsts.CRRTypeSize + DnsConsts.CRRClassSize + DnsConsts.CRRTtlSize + DnsConsts.CRdLengthSize;

        String nsName = getHostName();

        // skip other authorities.
        for(int i = 0; i < _nsCount - 1; i++)
        {
            // skip authority name.
            getHostName();

            // skip TYPE, CLASS, TTL, DATALength.
            _packetIdx += DnsConsts.CRRTypeSize + DnsConsts.CRRClassSize + DnsConsts.CRRTtlSize + DnsConsts.CRdLengthSize;

            // skip RRData.
            getHostName();
        }

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
            _packetIdx += DnsConsts.CRRTypeSize + DnsConsts.CRRClassSize + DnsConsts.CRRTtlSize;

            // read RDLENGTH.
            int rdLength = (_rawDnsData[_packetIdx] << CByte) + _rawDnsData[_packetIdx + 1];
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
                location = ((current - CReadingPointerThreshold) << CByte) + _rawDnsData[location + 1];
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


    private void SetZeroAdditionalRecords()
    {
        // set the two bytes of additional records to zero.
        _rawDnsData[10] = 0;
        _rawDnsData[11] = 0;
    }

    private boolean isQuery()
    {
        if((_rawDnsData[DnsConsts.CQRFlagByteIndex] & 128) == 0){
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
    private static final int CByte = 8;
    private byte[] _rawDnsData;
    private int _responseCode;
    private int _ansCount;
    private int _nsCount;
    private String _questionName;
    private int _packetIdx;
    private String _firstAuthorityName;
}
