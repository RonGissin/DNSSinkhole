package il.ac.idc.cs.sinkhole;

import java.util.HashSet;

/**
 * The entry point for the dns recursive sinkhole server project.
 */
public class SinkholeServer {
    public static void main(String[] args) {
        // load block list.
        BlockListLoader blockListLoader = new BlockListLoader();
        String blockListFilePath;
        HashSet<String> blockList = new HashSet<>();

        if(args.length != 0)
        {
            blockListFilePath = args[0];
            blockList = blockListLoader.Load(blockListFilePath);
        }

        DomainEnforcer domainEnforcer = new DomainEnforcer(blockList);

        RecursiveServer server = new RecursiveServer(DnsConsts.DnsServerPort, domainEnforcer);

        // start server.
        server.Start();
    }
}
