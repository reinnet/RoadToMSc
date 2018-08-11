package home.parham.roadtomsc.domain;

public class Node {
    private int cores;
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
