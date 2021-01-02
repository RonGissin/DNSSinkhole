package Infra;

import java.util.HashSet;

/**
 * Class for enforcing dns domains, and rejecting the ones listed in
 * a given block list.
 */
public class DomainEnforcer {
    public DomainEnforcer(HashSet<String> enforceList){
        this._enforceList = enforceList;
    }

    /**
     * Determines whether a specific domain is allowed,
     * by checking in the block list.
     * @param domain - the domain to check.
     * @return True, if the domain is allowed.
     */
    public boolean IsAllowed(String domain)
    {
        return !_enforceList.contains(domain);
    }

    private HashSet<String> _enforceList;
}
