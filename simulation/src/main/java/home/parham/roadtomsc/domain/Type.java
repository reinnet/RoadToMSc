package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Type {
    static {
        types = new ArrayList<>();
    }

    /**
     * cores indicate number of CPU cores
     */
    private int cores;

    /**
     * ram indicate amount of memory in GB
     */
    private int ram;


    /**
     * index indicate type identifier
     */
    private int index;

    /**
     * indicates that this type is an egress type so it must be placed only on egress nodes
     */
    private boolean egress;

    /**
     * indicates that is type is an ingress type so it must be placed only on ingress nodes
     */
    private boolean ingress;

    private static ArrayList<Type> types;

    public static void add(int cores, int ram, boolean egress, boolean ingress) {
        types.add(new Type(types.size(), cores, ram, egress, ingress));
    }

    public static void add(int cores, int ram) {
        types.add(new Type(types.size(), cores, ram, false, false));
    }

    public static Type get(int id) {
        return types.get(id);
    }

    public static int len() {
        return types.size();
    }

    private Type(int index, int cores, int ram, boolean ingress, boolean egress) {
        this.cores = cores;
        this.ram = ram;
        this.index = index;
        this.egress = egress;
        this.ingress = ingress;
    }

    public int getCores() {
        return cores;
    }

    public int getRam() {
        return ram;
    }

    public int getIndex() {
        return index;
    }

    public boolean isIngress() {
        return ingress;
    }

    public boolean isEgress() {
        return egress;
    }
}
