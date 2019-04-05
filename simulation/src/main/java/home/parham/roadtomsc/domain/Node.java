package home.parham.roadtomsc.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Node represents physical node in the problem space
 */
public class Node {

    /**
     * cores indicate number of CPU cores
     */
    private int cores;

    /**
     * ram indicate amount of memory in GB
     */
    private int ram;

    /**
     * vnfmSupport indicates that the physical node can supports VNFM placement
     */
    private boolean vnfmSupport;

    /**
     * notManagerNodes is a set of physical nodes that can not manage this physical nodes
     */
    private Set<Integer> notManagerNodes;

    public Node(int cores, int ram, boolean vnfmSupport, Set<Integer> notManagerNodes) {
        this.cores = cores;
        this.ram = ram;
        this.vnfmSupport = vnfmSupport;
        this.notManagerNodes = notManagerNodes;
    }

    public Node(int cores, int ram, boolean vnfmSupport) {
        this(cores, ram, vnfmSupport, new HashSet<Integer>());
    }

    public Node(int cores, int ram) {
        this(cores, ram, true, new HashSet<Integer>());
    }

    /**
     * @return amount of node's memory in GB
     */
    public int getRam() {
        return ram;
    }

    /**
     * @return number of node's CPU cores
     */
    public int getCores() {
        return cores;
    }

    public boolean isVnfmSupport() {
        return vnfmSupport;
    }

    public Set<Integer> getNotManagerNodes() {
        return notManagerNodes;
    }
}
