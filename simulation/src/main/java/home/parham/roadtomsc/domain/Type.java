package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Type {
    static {
        types = new ArrayList<>();
    }

    private static ArrayList<Type> types;

    public static void add(int cores, int ram) {
        types.add(new Type(types.size(), cores, ram));
    }

    public static Type get(int id) {
        return types.get(id);
    }

    public static int len() {
        return types.size();
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

    private Type(int index, int cores, int ram) {
        this.cores = cores;
        this.ram = ram;
        this.index = index;
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
}
