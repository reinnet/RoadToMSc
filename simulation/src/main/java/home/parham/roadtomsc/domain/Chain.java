package home.parham.roadtomsc.domain;

import java.util.ArrayList;

public class Chain {
    private ArrayList<Type> chain;
    private ArrayList<Link> links;

    private ArrayList<ArrayList<Integer>> managementConstraints;
    private int cost;

    public Chain(int cost) {
        this.chain = new ArrayList<>();
        this.links = new ArrayList<>();
        this.managementConstraints = new ArrayList<>();
        this.cost = cost;
    }

    public Chain addNode(int id) {
        this.addNodeWithManagementConstraints(id, new ArrayList<Integer>());
        return this;
    }

    public Chain addNodeWithManagementConstraints(int id, ArrayList<Integer> constraints) {
        this.chain.add(Type.get(id));
        this.managementConstraints.add(constraints);
        return this;
    }

    public Chain addLink(int bandwidth, int source, int destination) {
        this.links.add(new Link(bandwidth, source, destination));
        return this;
    }

    public Type getNode(int index) {
        return this.chain.get(index);
    }

    public Link getLink(int index) {
        return this.links.get(index);
    }

    public int nodes() {
        return this.chain.size();
    }

    public int links() {return this.links.size(); }

    public int getCost() {
        return cost;
    }

    public ArrayList<ArrayList<Integer>> getManagementConstraints() {
        return managementConstraints;
    }
}
