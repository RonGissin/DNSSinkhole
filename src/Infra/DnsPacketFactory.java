package Infra;

import java.util.ArrayList;
import java.util.List;

public class DnsPacketFactory {
    public DnsPacketFactory()
    {
    }

    public DnsPacket Create(byte[] rawDnsData)
    {
        return new DnsPacket(extractDomainName(), null);
    }

    private String extractDomainName() {
        StringBuilder domainName = new StringBuilder();

        // skip the header section.
        int location = CHeaderSectionLength;


        // a list to hold the current label content being read.
        List<Character> labelContent = new ArrayList<>();
        boolean isReadingLengthPart = true;
        int currentLabelContentLength = 0;
        int currentByteInContentLength = 0;

        int current = _rawDnsData[location];
        location++;

        while(current != 0)
        {
            if(current >= CReadingPointerThreshold) // jump to pointer.
            {
                // jump to pointer.
                location = _rawDnsData[current - CReadingPointerThreshold];
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
                    if(currentByteInContentLength == currentLabelContentLength)
                    {
                        // add byte to content so far.
                        isReadingLengthPart = true;
                        currentByteInContentLength = 0;
                        labelContent.add((char)current);
                        appendLabelToDomainName(domainName, labelContent);
                        // clear content for next label.
                        labelContent.clear();
                    }
                    else // reading some byte in the middle.
                    {
                        currentByteInContentLength++;
                        labelContent.add((char)current);
                    }
                }

                // move to next byte.
                location++;
            }

            current = _rawDnsData[location];
        }

        // trim last period.
        domainName.deleteCharAt(domainName.length() - 1);

        return domainName.toString();
    }

    private void appendLabelToDomainName(StringBuilder domainName, List<Character> labelContent)
    {
        domainName.append(labelContent.toArray());
        domainName.append('.');
    }

    private byte[] _rawDnsData;
    private static final int CHeaderSectionLength  = 12;
    private static final int CReadingPointerThreshold = 192;
}
