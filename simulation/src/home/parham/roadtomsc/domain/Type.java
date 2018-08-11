package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Type {
    static {
        types = new ArrayList<>();
    }

    private static ArrayList<Type> types;

    public static int addType(int cores, int ram) {
        types.add(new Type(cores, ram));
        return types.size() - 1;
    }

    public static Type getType(int id) {
        return types.get(id);
    }

    private int cores;
    private int ram;

    private Type(int cores, int ram) {
        this.cores = cores;
        this.ram = ram;
    }

    public int getCores() {
        return cores;
    }

    public int getRam() {
        return ram;
    }
}
