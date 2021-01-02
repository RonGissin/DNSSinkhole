package BlockList;

import java.util.HashSet;

public class BlockListEnforcer {
    public BlockListEnforcer(HashSet<String> enforceList){
        this.enforceList = enforceList;
    }

    public boolean IsAllowed(String domain)
    {
        return !this.enforceList.contains(domain);
    }

    private HashSet<String> enforceList;
}
