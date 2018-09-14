package home.parham.roadtomsc.model;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;

import java.util.ArrayList;

/**
 * Config represents problem configuration
 * by configuration we mean parameters that are given
 * by user.
 */
public class Config {

    /**
     * Shows build status of configuration.
     * this variable will set to true if the configuration build is successful.
     */
    private boolean build;

    /**
     * physical nodes
     */
    private ArrayList<Node> nodes;

    /**
     * boolean array indicates that a physical node w can support VNFM
     */
    private ArrayList<Boolean> isSupportVNFM;

    /**
     * number of physical nodes
     */
    private int W;

    /**
     * physical links
     */
    private ArrayList<Link> links;
    /**
     * connectivity matrix
     */
    private int[][] E;

    /**
     * number of VNF types
     */
    private int F;

    /**
     * SFC requests chains
     */
    private ArrayList<Chain> chains;
    /**
     * number of SFC requests
     */
    private int T;

    /**
     * total number of VNFs
     */
    private int V;
    /**
     * total number of virtual links
     */
    private int U;

    /**
     * VNFMs parameters
     */
    private int vnfmRam, vnfmCores, vnfmCapacity, vnfmRadius, vnfmBandwidth;

    public Config(int vnfmRam, int vnfmCores, int vnfmCapacity, int vnfmRadius, int vnfmBandwidth) {
        this.vnfmRam = vnfmRam;
        this.vnfmCores = vnfmCores;
        this.vnfmCapacity = vnfmCapacity;
        this.vnfmRadius = vnfmRadius;
        this.vnfmBandwidth = vnfmBandwidth;

        this.links = new ArrayList<>();
        this.chains = new ArrayList<>();
        this.nodes = new ArrayList<>();

        this.build = false;
    }

    /**
     * Adds physical link to network topology
     * @param link: physical link
     */
    public void addLink(Link link) {
        if (!this.isBuild()) {
            this.links.add(link);
        }
    }

    /**
     * Adds chain to SFC request collection, note that our problem is offline
     * @param chain: SFC request
     */
    public void addChain(Chain chain) {
        if (!this.isBuild()) {
            this.chains.add(chain);
        }
    }

    /**
     * Adds physical node to network topology
     * @param node: physical node
     */
    public void addNode(Node node, boolean isSupportVNFM) {
        if (!this.isBuild()) {
            this.nodes.add(node);
            this.isSupportVNFM.add(isSupportVNFM);
        }
    }

    /**
     * Adds physical node to network topology
     * @param node: physical node
     */
    public void addNode(Node node) {
        this.addNode(node, true);
    }

    public void build() {
        this.W = nodes.size();
        // connectivity matrix
        this.E = new int[this.W][this.W];
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.W; j++) {
                this.E[i][j] = 0;
            }
        }
        for (Link link : this.links) {
            E[link.getSource()][link.getDestination()] = link.getBandwidth();
        }

        // VNF types
        this.F = Type.len();

        // SFC requests
        this.T = chains.size();

        // Total number of VNFs and virtual links
        for (Chain chain : chains) {
            this.V += chain.nodes();
            this.U += chain.links();
        }

        build = true;
    }
    public boolean isBuild() {
        return build;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Boolean> getIsSupportVNFM() {
        return isSupportVNFM;
    }

    public int getW() {
        return W;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public int[][] getE() {
        return E;
    }

    public int getF() {
        return F;
    }

    public ArrayList<Chain> getChains() {
        return chains;
    }

    public int getT() {
        return T;
    }

    public int getV() {
        return V;
    }

    public int getU() {
        return U;
    }

    public int getVnfmRam() {
        return vnfmRam;
    }

    public int getVnfmCores() {
        return vnfmCores;
    }

    public int getVnfmCapacity() {
        return vnfmCapacity;
    }

    public int getVnfmRadius() {
        return vnfmRadius;
    }

    public int getVnfmBandwidth() {
        return vnfmBandwidth;
    }
}
