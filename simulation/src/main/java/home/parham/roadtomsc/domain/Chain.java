package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Chain {
    private ArrayList<Type> chain;

    public Chain addVNF(int id) {
        this.chain.add(Type.getType(id));
        return this;
    }

    public Type getVNF(int index) {
        return this.chain.get(index);
    }

}
