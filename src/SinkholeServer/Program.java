package SinkholeServer;

import Infra.BlockListLoader;
import Infra.DnsOperationsConsts;
import Infra.DomainEnforcer;

import java.util.HashSet;

public class Program {
    public static void main(String[] args) {
        // load block list.
        BlockListLoader blockListLoader = new BlockListLoader();
        HashSet<String> blockList = blockListLoader.Load();

        DomainEnforcer domainEnforcer = new DomainEnforcer(blockList);

        SinkholeServer server = new SinkholeServer(53, domainEnforcer);

        // start server.
        server.Start();
    }
}
