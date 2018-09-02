package home.parham.roadtomsc.model;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;
import ilog.concert.*;

public class Model {
    private IloModeler modeler;

    /**
     * physical nodes
     */
    private Node[] nodes;
    private int W;

    /**
     * physical links
     */
    private Link[] links;
    /**
     * connectivity matrix
     */
    private int[][] E;

    // number of VNF types
    private int F;

    /**
     * SFC requests chains
     */
    private Chain[] chains;
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


    /**
     * binary variable assuming the value 1 if the _h_th SFC request is accepted;
     * otherwise its value is zero
     *
     * x[h]
     */
    private IloIntVar[] x;


    /**
     * the number of VNF instances of type _k_ that are used in server _w_
     *
     * y[w][k]
     */
    private IloIntVar[][] y;


    /**
     * the number of VNFMs that are used in server _w_
     *
     * yh[w]
     */
    private IloIntVar[] yHat;

    /**
     * binary variable assuming the value 1 if the VNF node _v_ is served by the VNF instance of type
     * _k_ in the server _w_
     *
     * z[k][w][v]
     */
    private IloIntVar[][][] z;

    /**
     * binary variable assuming the value 1 if the _h_th SFC is assigned to VNFM
     * on server w
     *
     * zh[h][w]
     */
    private IloIntVar[][] zHat;

    /**
     * binary variable assuming the value 1 if the virtual link _(u, v)_ is routed on
     * the physical network link from _i_ to _j_.
     *
     * tau[i][j][uv]
     */
    private IloIntVar[][][] tau;

    /**
     * binary variable assuming the value 1 if the management of VNF node _v_
     * is routed on the physical network link from _i_ to _j_.
     *
     * tauHat[i][j][v]
     */
    private IloIntVar[][][] tauHat;

    public Model(IloModeler modeler) throws IloException {
        this.modeler = modeler;

        // physical nodes
        this.nodes = new Node[]{
                new Node(1, 1),
                new Node(1, 1),
                new Node(2, 4),
                new Node(1, 1),
                new Node(1, 1),
        };
        this.W = nodes.length;

        // physical links
        this.links = new Link[]{
                new Link(10, 0, 1),
                new Link(10, 0, 2),
                new Link(10, 2, 0),
                new Link(10, 1, 2),
                new Link(10, 3, 4),
        };
        // connectivity matrix
        this.E = new int[W][W];
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < W; j++) {
                E[i][j] = 0;
            }
        }
        for (Link link : links) {
            E[link.getSource()][link.getDestination()] = link.getBandwidth();
        }

        // VNF types
        Type.add(1, 1); // Type 0
        Type.add(1, 2); // Type 1
        this.F = Type.len();

        // SFC requests
        // consider to create requests after creating VNF types
        this.chains = new Chain[]{
                new Chain(10).addNode(0),
                new Chain(10).addNode(0).addNode(1).addNode(0).addLink(1, 0, 1)
                        .addLink(1, 0, 2),
        };
        this.T = chains.length;

        // Total number of VNFs and virtual links
        for (Chain chain : chains) {
            this.V += chain.nodes();
            this.U += chain.links();
        }

        // VNFMs parameters
        this.vnfmRam = 1;
        this.vnfmCores = 1;
        this.vnfmCapacity = 5;
        this.vnfmRadius = 5;
        this.vnfmBandwidth = 1;
    }

    /**
     * Adds model variables
     * @return Model
     * @throws IloException
     */
    public Model variables() throws IloException {
        // x
        String[] xNames = new String[this.T];
        for (int i = 0; i < this.T; i++) {
            xNames[i] = String.format("x(%d)", i);
        }
        this.x = this.modeler.boolVarArray(this.T, xNames);

        // y
        String[][] yNames = new String[this.W][this.F];
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.F; j++) {
                yNames[i][j] = String.format("y(%d,%d)", i, j);
            }
        }
        this.y = new IloIntVar[this.W][this.F];
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.F; j++) {
                this.y[i][j] = this.modeler.intVar(0, Integer.MAX_VALUE, yNames[i][j]);
            }
        }

        // yHat
        String[] yHatNames = new String[this.W];
        for (int i = 0; i < this.W; i++) {
            yHatNames[i] = String.format("yh(%d)", i);
        }
        this.yHat = this.modeler.intVarArray(0, Integer.MAX_VALUE, W, yHatNames);

        // z
        String[][][] zNames = new String[this.F][this.W][this.V];
        for (int i = 0; i < this.F; i++) {
            for (int j = 0; j < this.W; j++) {
                int v = 0;
                for (int h = 0; h < this.T; h++) {
                    for (int k = 0; k < this.chains[h].nodes(); k++) {
                        zNames[i][j][k + v] = String.format("z(%d,%d,%d_%d)", i, j, h, k);
                    }
                    v += chains[h].nodes();
                }
            }
        }
        this.z = new IloIntVar[this.F][this.W][this.V];
        for (int i = 0; i < this.F; i++) {
            for (int j = 0; j < this.W; j++) {
                for (int k = 0; k < this.V; k++) {
                    this.z[i][j][k] = modeler.boolVar(zNames[i][j][k]);
                }
            }
        }

        // zHat
        String[][] zHatNames = new String[this.T][this.W];
        for (int i = 0; i < this.T; i++) {
            for (int j = 0; j < this.W; j++) {
                zHatNames[i][j] = String.format("zh(%d,%d)", i, j);
            }
        }
        this.zHat = new IloIntVar[this.T][this.W];
        for (int i = 0; i < this.T; i++) {
            for (int j = 0; j < this.W; j++) {
                this.zHat[i][j] = this.modeler.boolVar(zHatNames[i][j]);
            }
        }

        // tau, tauHat
        String[][][] tauNames = new String[this.W][this.W][this.U];
        String[][][] tauHatNames = new String[this.W][this.W][this.V];
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.W; j++) {
                int u = 0;
                int v = 0;
                for (int h = 0; h < this.T; h++) {
                    for (int k = 0; k < this.chains[h].links(); k++) {
                        tauNames[i][j][u + k] = String.format("tau(%d,%d,%d_%d)", i, j, h, k);
                    }
                    for (int k = 0; k < this.chains[h].nodes(); k++) {
                        tauHatNames[i][j][v + k] = String.format("tauh(%d,%d,%d_%d)", i, j, h, k);
                    }
                    v += chains[h].nodes();
                    u += chains[h].links();

                }
            }
        }
        this.tauHat = new IloIntVar[this.W][this.W][this.V];
        this.tau = new IloIntVar[this.W][this.W][this.U];
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.W; j++) {
                for (int k = 0; k < this.U; k++) {
                    this.tau[i][j][k] = modeler.boolVar(tauNames[i][j][k]);
                }
                for (int k = 0; k < this.V; k++) {
                    this.tauHat[i][j][k] = modeler.boolVar(tauHatNames[i][j][k]);
                }
            }
        }

        return this;
    }

    /**
     * Adds objective function
     * @throws IloException
     * @return Model
     */
    public Model objective() throws IloException {
        IloLinearNumExpr expr = this.modeler.linearNumExpr();
        for (int i = 0; i < T; i++) {
            expr.addTerm(chains[i].getCost(), x[i]);
        }
        this.modeler.addMaximize(expr);

        return this;
    }

    /**
     * Adds model constraints
     * @return Model
     * @throws IloException
     */
    public Model constraints() throws IloException {
        this.nodeMemoryCPUConstraint();
        this.servicePlaceConstraint();
        this.serviceTypeConstraint();
        this.manageConstraint();
        this.managePlaceConstraint();
        this.managerCapacityConstraint();

        this.flowConservation();
        this.managementFlowConservation();

        this.linkBandwidthConstraint();
        this.radiusConstraint();

        return this;
    }

    /**
     * Node Memory/CPU Constraint
     * @throws IloException
     */
    private void nodeMemoryCPUConstraint() throws IloException {
        for (int i = 0; i < W; i++) {
            IloLinearNumExpr ramConstraint = this.modeler.linearNumExpr();
            IloLinearNumExpr cpuConstraint = this.modeler.linearNumExpr();

            ramConstraint.addTerm(this.vnfmRam, this.yHat[i]); // VNFMs ram
            cpuConstraint.addTerm(this.vnfmCores, this.yHat[i]); // VNFMs cpu

            for (int j = 0; j < this.F; j++) {
                ramConstraint.addTerm(Type.get(j).getRam(), this.y[i][j]); // instance ram
                cpuConstraint.addTerm(Type.get(j).getCores(), this.y[i][j]); // instance cpu
            }

            this.modeler.addLe(cpuConstraint, this.nodes[i].getCores(), String.format("node_cpu_constraint_%d", i));
            this.modeler.addLe(ramConstraint, this.nodes[i].getRam(), String.format("node_memory_constraint_%d", i));
        }
    }

    /**
     * Service Place Constraint
     * @throws IloException
     */
    private void servicePlaceConstraint() throws IloException {
        for (int i = 0; i < this.F; i++) {
            for (int j = 0; j < this.W; j++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int k = 0; k < this.V; k++) {
                    constraint.addTerm(1, this.z[i][j][k]);
                }

                this.modeler.addLe(constraint, this.y[j][i], String.format("service_place_constraint_%d_%d", i, j));
            }
        }
    }

    /**
     * Service Constraint + Type constraint
     * @throws IloException
     */
    private void serviceTypeConstraint() throws IloException {
        int v = 0;
        for (int h = 0; h < this.T; h++) {
            for (int k = 0; k < this.chains[h].nodes(); k++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int i = 0; i < F; i++) {
                    for (int j = 0; j < W; j++) {
                        this.modeler.addLe(this.z[i][j][k + v],
                                this.chains[h].getNode(k).getIndex() == i ? 1 : 0, "type_constraint");
                        constraint.addTerm(1, this.z[i][j][k + v]);
                    }
                }

                this.modeler.addEq(constraint, this.x[h], String.format("service_constraint_%d", h));
            }
            v += this.chains[h].nodes();
        }
    }

    /**
     * Manage Constraint
     * @throws IloException
     */
    private void manageConstraint() throws IloException {
            for (int i = 0; i < this.T; i++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int j = 0; j < this.W; j++) {
                    constraint.addTerm(1, this.zHat[i][j]);
                }

                this.modeler.addEq(constraint, this.x[i], String.format("manage_constraint_%d", i));
            }
    }


    /**
     * Manage Place Constraint
     * @throws IloException
     */
    private void managePlaceConstraint() throws IloException {
        for (int j = 0; j < this.W; j++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();
            for (int i = 0; i < this.T; i++) {
                constraint.addTerm(1, this.zHat[i][j]);
            }

            this.modeler.addLe(constraint, this.yHat[j], String.format("manage_place_constraint_%d", j));
        }
    }

    /**
     * Manager Capacity Constraint
     * @throws IloException
     */
    private void managerCapacityConstraint() throws IloException {
        for (int i = 0; i < this.W; i++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();

            for (int j = 0; j < this.T; j++) {
                constraint.addTerm(this.chains[j].nodes(), this.zHat[j][i]);
            }

            this.modeler.addLe(constraint, vnfmCapacity, String.format("manager_capacity_constraint_%d", i));
        }
    }


    /**
     * Flow conservation
     * @throws IloException
     */
    private void flowConservation() throws IloException {
        // linkConstraint == nodeConstraint
        int v = 0;
        int u = 0;
        for (Chain chain : chains) {
            for (int i = 0; i < W; i++) {  // Source of Physical link
                for (int l = 0; l < chain.links(); l++) { // Virtual link
                    int virtualSource = chain.getLink(l).getSource() + v;
                    int virtualDestination = chain.getLink(l).getDestination() + v;

                    IloLinearIntExpr linkConstraint = this.modeler.linearIntExpr();
                    IloLinearIntExpr nodeConstraint = this.modeler.linearIntExpr();

                    for (int j = 0; j < W; j++) { // Destination of Physical link
                        if (E[i][j] > 0) {
                            linkConstraint.addTerm(1, this.tau[i][j][u + l]);
                        }
                        if (E[j][i] > 0) {
                            linkConstraint.addTerm(-1, this.tau[j][i][u + l]);
                        }
                    }

                    for (int k = 0; k < F; k++) {
                        nodeConstraint.addTerm(1, this.z[k][i][virtualSource]);
                        nodeConstraint.addTerm(-1, this.z[k][i][virtualDestination]);
                    }

                    this.modeler.addEq(linkConstraint, nodeConstraint, "flow_conservation");
                }
            }
            v += chain.nodes();
            u += chain.links();
        }
    }

    /**
     * Management Flow conservation
     * @throws IloException
     */
    private void managementFlowConservation() throws IloException {
        // linkConstraint == nodeConstraint
        int v = 0;
        for (int h = 0; h < this.T; h++) {
            for (int i = 0; i < this.W; i++) {  // Source of Physical link
                for (int n = 0; n < this.chains[h].nodes(); n++) { // Virtual link
                    IloLinearIntExpr linkConstraint = this.modeler.linearIntExpr();
                    IloLinearIntExpr nodeConstraint = this.modeler.linearIntExpr();

                    for (int j = 0; j < this.W; j++) { // Destination of Physical link
                        if (this.E[i][j] > 0) {
                            linkConstraint.addTerm(1, this.tauHat[i][j][v + n]);
                        }
                        if (this.E[j][i] > 0) {
                            linkConstraint.addTerm(-1, this.tauHat[j][i][v + n]);
                        }
                    }

                    for (int k = 0; k < F; k++) {
                        nodeConstraint.addTerm(1, this.z[k][i][v + n]);
                    }
                    nodeConstraint.addTerm(-1, this.zHat[h][i]);

                    this.modeler.addEq(linkConstraint, nodeConstraint, "management_flow_conservation");
                }
            }
            v += chains[h].nodes();
        }
    }

    /**
     * Link Bandwidth Constraint
     * @throws IloException
     */
    private void linkBandwidthConstraint() throws IloException {
        for (int i = 0; i < this.W; i++) {
            for (int j = 0; j < this.W; j++) {
                if (this.E[i][j] > 0) {
                    IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                    int u = 0;
                    int v = 0;
                    for (Chain chain : this.chains) {
                        // VNFs
                        for (int k = 0; k < chain.links(); k++) {
                            constraint.addTerm(chain.getLink(k).getBandwidth(), this.tau[i][j][k + u]);
                        }

                        // VNFM
                        for (int k = 0; k < chain.nodes(); k++) {
                            constraint.addTerm(this.vnfmBandwidth, this.tauHat[i][j][k + v]);
                        }
                        v += chain.nodes();
                        u += chain.links();
                    }

                    this.modeler.addLe(constraint, this.E[i][j], "link_bandwidth_constraint");
                }
            }
        }
    }

    /**
     * Radius Constraint
     * @throws IloException
     */
    private void radiusConstraint() throws IloException {
        for (int i = 0; i < this.V; i++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();

            for (int j = 0; j < this.W; j++) {
                for (int k = 0; k < this.W; k++) {
                    if (this.E[j][k] > 0) {
                        constraint.addTerm(1, this.tauHat[j][k][i]);
                    }
                }
            }

            this.modeler.addLe(constraint, this.vnfmRadius, "management_radius_constraint");
        }
    }
}
