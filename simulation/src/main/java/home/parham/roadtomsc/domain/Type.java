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
     * indicates that this type is an ingress type so it must be placed only on ingress nodes
     */
    private boolean ingress;

    /**
     * indicates that this type is a manageable type or not. manageable types need a VNFM
     */
    private boolean manageable;

    private static ArrayList<Type> types;

    public static void add(int cores, int ram, boolean egress, boolean ingress, boolean manageable) {
        types.add(new Type(types.size(), cores, ram, egress, ingress, manageable));
    }

    public static Type get(int id) {
        return types.get(id);
    }

    public static int len() {
        return types.size();
    }

    private Type(int index, int cores, int ram, boolean ingress, boolean egress, boolean manageable) {
        this.cores = cores;
        this.ram = ram;
        this.index = index;
        this.egress = egress;
        this.ingress = ingress;
        this.manageable = manageable;
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

    public boolean isManageable() {
        return manageable;
    }
}
