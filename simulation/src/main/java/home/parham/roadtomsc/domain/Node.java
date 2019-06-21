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
     * vnfSupport indicates that the physical node can supports VNF placement
     */
    private boolean vnfSupport;

    /**
     * notManagerNodes is a set of physical nodes that can not manage this physical nodes
     */
    private Set<Integer> notManagerNodes;

    public Node(int cores, int ram, boolean vnfSupport, Set<Integer> notManagerNodes) {
        this.cores = cores;
        this.ram = ram;
        this.vnfSupport = vnfSupport;
        this.notManagerNodes = notManagerNodes;
    }

    public Node(int cores, int ram, boolean vnfSupport) {
        this(cores, ram, vnfSupport, new HashSet<>());
    }

    public Node(int cores, int ram) {
        this(cores, ram, true, new HashSet<>());
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

    public boolean isVnfSupport() {
        return vnfSupport;
    }

    public Set<Integer> getNotManagerNodes() {
        return notManagerNodes;
    }

    @Override
    public String toString() {
        return "Node{" +
                "cores=" + cores +
                ", ram=" + ram +
                ", vnfSupport=" + vnfSupport +
                ", notManagerNodes=" + notManagerNodes +
                '}';
    }
}
