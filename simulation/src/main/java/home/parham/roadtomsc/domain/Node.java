package home.parham.roadtomsc.domain;

import java.util.Set;

/**
 * Node represents physical node in the problem space
 */
public class Node {

    /**
     * node human identification
     */
    private String name;

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
     * notManagerNodes is a set of physical nodes that can not manage this physical node
     */
    private Set<Integer> notManagerNodes;

    /**
     * egress node can support VNFs with egress types
     */
    private boolean egress;

    /**
     * ingress node can support VNFs with ingress types
     */
    private boolean ingress;

    public Node(String name, int cores, int ram, boolean vnfSupport, Set<Integer> notManagerNodes, boolean egress, boolean ingress) {
        this.name = name;
        this.cores = cores;
        this.ram = ram;
        this.vnfSupport = vnfSupport;
        this.notManagerNodes = notManagerNodes;
        this.ingress = ingress;
        this.egress = egress;
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

    /**
     * @return that this physical node can run VNFs
     */
    public boolean isVnfSupport() {
        return vnfSupport;
    }

    /**
     * @return the nodes that can't manage this node
     */
    public Set<Integer> getNotManagerNodes() {
        return notManagerNodes;
    }

    /**
     * @return that this node can support egress VNF types
     */
    public boolean isEgress() {
        return egress;
    }

    /**
     * @return that this node can support ingress VNF types
     */
    public boolean isIngress() {
        return ingress;
    }

    @Override
    public String toString() {
        return "Node{" +
                "cores=" + cores +
                ", ram=" + ram +
                ", vnfSupport=" + vnfSupport +
                ", notManagerNodes=" + notManagerNodes +
                ", egress=" + egress +
                ", ingress=" + ingress +
                '}';
    }

    /**
     * @return name of the node
     */
    public String getName() {
        return name;
    }
}
