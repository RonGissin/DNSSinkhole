package il.ac.idc.cs.sinkhole;

import java.util.HashSet;

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

        RecursiveServer server = new RecursiveServer(DnsOperationsConsts.DnsServerPort, domainEnforcer);

        // start server.
        server.Start();
    }
}
