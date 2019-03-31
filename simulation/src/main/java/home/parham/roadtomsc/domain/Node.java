package home.parham.roadtomsc.domain;

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

    public Node(int cores, int ram) {
        this.cores = cores;
        this.ram = ram;
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
}
