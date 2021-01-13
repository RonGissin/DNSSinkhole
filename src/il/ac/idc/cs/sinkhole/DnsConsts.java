package il.ac.idc.cs.sinkhole;

public final class DnsConsts
{
    public static final int DnsServerPort = 5300;
    public static final int DnsClientPort = 53;
    public static final int DnsHeaderLength = 12;
    public static final int DnsUdpPacketSize = 1024;
    public static final short CRRTypeSize = 2;
    public static final short CRRClassSize = 2;
    public static final short CRRTtlSize = 4;
    public static final short CRdLengthSize = 2;
    public static final short CQRFlagByteIndex = 2;
    public static final short CRCodeByteIndex = 3;
    public static final short CAnsCountByteIndex = 6;
    public static final short CNsCountByteIndex = 8;

    private DnsConsts()
    {
    }
}
