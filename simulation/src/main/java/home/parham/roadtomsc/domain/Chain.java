package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Chain {
    private ArrayList<Type> chain;
    private int cost;

    public Chain(int cost) {
        this.chain = new ArrayList<>();
        this.cost = cost;
    }

    public Chain addVNF(int id) {
        this.chain.add(Type.get(id));
        return this;
    }

    public Type getVNF(int index) {
        return this.chain.get(index);
    }

    public int getCost() {
        return cost;
    }
}
