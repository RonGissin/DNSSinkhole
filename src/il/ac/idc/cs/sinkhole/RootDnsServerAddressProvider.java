package il.ac.idc.cs.sinkhole;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class that provides a root server's ip address.
 */
public class RootDnsServerAddressProvider {
    public RootDnsServerAddressProvider()
    {
        initDomainToIpMapping();
    }

    /**
     * Gets a random root server's ip address (InetAddress).
     * @return an InetAddress of a random root.
     * @throws UnknownHostException in case of failure of resolving the root ip.
     */
    public InetAddress GetRootIpAddress() throws UnknownHostException
    {
        int randIdx = new Random().nextInt(_rootServerHostnames.size());

        return InetAddress.getByName(_rootServerHostnames.get(randIdx));
    }

    private void initDomainToIpMapping()
    {
        _rootServerHostnames = new ArrayList<>() {{
            add("a.root-servers.net");
            add("b.root-servers.net");
            add("c.root-servers.net");
            add("d.root-servers.net");
            add("e.root-servers.net");
            add("f.root-servers.net");
            add("g.root-servers.net");
            add("h.root-servers.net");
            add("i.root-servers.net");
            add("j.root-servers.net");
            add("k.root-servers.net");
            add("l.root-servers.net");
            add("m.root-servers.net");
        }};
    }

    private List<String> _rootServerHostnames;
}
