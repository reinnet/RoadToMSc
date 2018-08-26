package home.parham.roadtomsc.domain;

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

    public int getRam() {
        return ram;
    }

    public int getCores() {
        return cores;
    }
}
