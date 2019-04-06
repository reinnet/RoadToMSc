package home.parham.roadtomsc.model;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Type;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloModeler;

/**
 * Model creates variables, objective and constraints of mathematical
 * model of our problem.
 */
public class Model {
    /**
     * modeler is a cplex model builder.
     */
    private final IloModeler modeler;

    /**
     * Configuration instance that provides problem parameters.
     */
    private final Config cfg;

    /**
     * binary variable assuming the value 1 if the _h_th SFC request is accepted;
     * otherwise its value is zero.
     *
     * x[h]
     * .lp format: x (chain number)
     */
    private IloIntVar[] x;


    /**
     * the number of VNF instances of type _k_ that are used in server _w_.
     *
     * y[w][k]
     * .lp format: y (physical node, type)
     */
    private IloIntVar[][] y;


    /**
     * the number of VNFMs that are used in server _w_.
     *
     * yh[w]
     * .lp format: y (physical node)
     */
    private IloIntVar[] yHat;

    /**
     * binary variable assuming the value 1 if the VNF node _v_ is served by the VNF instance of type
     * _k_ in the server _w_.
     *
     * z[k][w][v]
     * .lp format: z (type, physical node, chain number _ node number in the chain)
     */
    private IloIntVar[][][] z;

    /**
     * binary variable assuming the value 1 if the _h_th SFC is assigned to VNFM
     * on server w.
     *
     * zh[h][w]
     * .lp format: zh (chain number, physical node)
     */
    private IloIntVar[][] zHat;

    /**
     * binary variable assuming the value 1 if the virtual link _(u, v)_ is routed on
     * the physical network link from _i_ to _j_.
     *
     * tau[i][j][uv]
     * .lp format: tau (physical link source, physical link destination, chain number _ link number in the chain)
     */
    private IloIntVar[][][] tau;

    /**
     * binary variable assuming the value 1 if the management of VNF node _v_
     * is routed on the physical network link from _i_ to _j_.
     *
     * tauHat[i][j][v]
     * .lp format: tauh (physical link source, physical link destination, chain number _ node number in the chain)
     */
    private IloIntVar[][][] tauHat;

    /**
     *
     * @param pModeler CPLEX modeler instance
     * @param pCfg configuration instance
     */
    public Model(final IloModeler pModeler, final Config pCfg) {
        this.modeler = pModeler;
        if (!pCfg.isBuild()) {
            throw new IllegalArgumentException("Configuration"
                    + "must build before use");
        }
        this.cfg = pCfg;
    }

    /**
     * Adds model variables.
     *
     * @return Model
     */
    public Model variables() throws IloException {
        xVariable();
        yVariable();
        yHatVariable();
        zVariable();
        zHatVariable();

        tauTauHatVariable();

        return this;
    }

    private void xVariable() throws IloException {
        // x
        String[] xNames = new String[this.cfg.getT()];
        for (int i = 0; i < this.cfg.getT(); i++) {
            xNames[i] = String.format("x(%d)", i);
        }
        this.x = this.modeler.boolVarArray(this.cfg.getT(), xNames);
    }

    private void yVariable() throws IloException {
        // y
        String[][] yNames = new String[this.cfg.getW()][this.cfg.getF()];
        for (int i = 0; i < this.cfg.getW(); i++) {
            for (int j = 0; j < this.cfg.getF(); j++) {
                yNames[i][j] = String.format("y(%d,%d)", i, j);
            }
        }
        this.y = new IloIntVar[this.cfg.getW()][this.cfg.getF()];
        for (int i = 0; i < this.cfg.getW(); i++) {
            for (int j = 0; j < this.cfg.getF(); j++) {
                this.y[i][j] = this.modeler.intVar(0, Integer.MAX_VALUE, yNames[i][j]);
            }
        }
    }

    private void yHatVariable() throws IloException {
        // yHat
        String[] yHatNames = new String[this.cfg.getW()];
        for (int i = 0; i < this.cfg.getW(); i++) {
            yHatNames[i] = String.format("yh(%d)", i);
        }
        this.yHat = this.modeler.intVarArray(this.cfg.getW() , 0, Integer.MAX_VALUE, yHatNames);
    }

    private void zVariable() throws IloException {
        // z
        String[][][] zNames = new String[this.cfg.getF()][this.cfg.getW()][this.cfg.getV()];
        for (int i = 0; i < this.cfg.getF(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                int v = 0;
                for (int h = 0; h < this.cfg.getT(); h++) {
                    for (int k = 0; k < this.cfg.getChains().get(h).nodes(); k++) {
                        zNames[i][j][k + v] = String.format("z(%d,%d,%d_%d)", i, j, h, k);
                    }
                    v += this.cfg.getChains().get(h).nodes();
                }
            }
        }
        this.z = new IloIntVar[this.cfg.getF()][this.cfg.getW()][this.cfg.getV()];
        for (int i = 0; i < this.cfg.getF(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                for (int k = 0; k < this.cfg.getV(); k++) {
                    this.z[i][j][k] = modeler.boolVar(zNames[i][j][k]);
                }
            }
        }
    }

    private void zHatVariable() throws IloException {
        // zHat
        String[][] zHatNames = new String[this.cfg.getT()][this.cfg.getW()];
        for (int i = 0; i < this.cfg.getT(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                zHatNames[i][j] = String.format("zh(%d,%d)", i, j);
            }
        }
        this.zHat = new IloIntVar[this.cfg.getT()][this.cfg.getW()];
        for (int i = 0; i < this.cfg.getT(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                this.zHat[i][j] = this.modeler.boolVar(zHatNames[i][j]);
            }
        }
    }

    private void tauTauHatVariable() throws IloException {
        // tau, tauHat
        String[][][] tauNames = new String[this.cfg.getW()][this.cfg.getW()][this.cfg.getU()];
        String[][][] tauHatNames = new String[this.cfg.getW()][this.cfg.getW()][this.cfg.getV()];
        for (int i = 0; i < this.cfg.getW(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                int u = 0;
                int v = 0;
                for (int h = 0; h < this.cfg.getT(); h++) {
                    for (int k = 0; k < this.cfg.getChains().get(h).links(); k++) {
                        tauNames[i][j][u + k] = String.format("tau(%d,%d,%d_%d)", i, j, h, k);
                    }
                    u += this.cfg.getChains().get(h).links();

                    for (int k = 0; k < this.cfg.getChains().get(h).nodes(); k++) {
                        tauHatNames[i][j][v + k] = String.format("tauh(%d,%d,%d_%d)", i, j, h, k);
                    }
                    v += this.cfg.getChains().get(h).nodes();
                }
            }
        }
        this.tauHat = new IloIntVar[this.cfg.getW()][this.cfg.getW()][this.cfg.getV()];
        this.tau = new IloIntVar[this.cfg.getW()][this.cfg.getW()][this.cfg.getU()];
        for (int i = 0; i < this.cfg.getW(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {

                for (int k = 0; k < this.cfg.getU(); k++) {
                    this.tau[i][j][k] = modeler.boolVar(tauNames[i][j][k]);
                }

                for (int k = 0; k < this.cfg.getV(); k++) {
                    this.tauHat[i][j][k] = modeler.boolVar(tauHatNames[i][j][k]);
                }
            }
        }
    }

    /**
     * Adds objective function
     * @throws IloException
     * @return Model
     */
    public Model objective() throws IloException {
        IloLinearNumExpr expr = this.modeler.linearNumExpr();
        for (int i = 0; i < this.cfg.getT(); i++) {
            expr.addTerm(this.cfg.getChains().get(i).getCost(), this.x[i]);
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
        /*
        check following method for more detail about its removal.
        this.serviceTypeConstraint();
        */
        this.manageConstraint();
        this.managePlaceConstraint();
        this.vnfSupportConstraint();
        this.managerToNodeSupportConstraint();
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
        for (int i = 0; i < this.cfg.getW(); i++) {
            IloLinearNumExpr ramConstraint = this.modeler.linearNumExpr();
            IloLinearNumExpr cpuConstraint = this.modeler.linearNumExpr();

            ramConstraint.addTerm(this.cfg.getVnfmRam(), this.yHat[i]); // VNFMs ram
            cpuConstraint.addTerm(this.cfg.getVnfmCores(), this.yHat[i]); // VNFMs cpu

            for (int j = 0; j < this.cfg.getF(); j++) {
                ramConstraint.addTerm(Type.get(j).getRam(), this.y[i][j]); // instance ram
                cpuConstraint.addTerm(Type.get(j).getCores(), this.y[i][j]); // instance cpu
            }

            this.modeler.addLe(cpuConstraint, this.cfg.getNodes().get(i).getCores(),
                    String.format("node_cpu_constraint_node{%d}", i));
            this.modeler.addLe(ramConstraint, this.cfg.getNodes().get(i).getRam(),
                    String.format("node_memory_constraint_node{%d}", i));
        }
    }

    /**
     * Service Place Constraint
     * @throws IloException
     */
    private void servicePlaceConstraint() throws IloException {
        for (int i = 0; i < this.cfg.getF(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int k = 0; k < this.cfg.getV(); k++) {
                    constraint.addTerm(1, this.z[i][j][k]);
                }

                this.modeler.addLe(constraint, this.y[j][i], String.format("service_place_constraint_type{%d}_node{%d}", i, j));
            }
        }
    }

    /**
     * Service Constraint + Type constraint
     * Type constraint was removed in favor of removing redundant _z_s from
     * service constraint
     * https://github.com/1995parham/RoadToMSc/issues/14
     * @deprecated
     * @throws IloException
     */
    private void serviceTypeConstraint() throws IloException {
        int v = 0;
        for (int h = 0; h < this.cfg.getT(); h++) {
            for (int k = 0; k < this.cfg.getChains().get(h).nodes(); k++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int i = 0; i < this.cfg.getF(); i++) {
                    for (int j = 0; j < this.cfg.getW(); j++) {
                        // adds z variable into service constraint if type of z is equal to type of node v
                        if (this.cfg.getChains().get(h).getNode(k).getIndex() == i) {
                            constraint.addTerm(1, this.z[i][j][k + v]);
                        }
                    }
                }

                // if chain `h` is serviced then all of its nodes should be serviced
                this.modeler.addEq(constraint, this.x[h], String.format("service_constraint_chain{%d}_vnf{%d}", h, k));
            }
            v += this.cfg.getChains().get(h).nodes();
        }
    }

    /**
     * Manage Constraint
     * @throws IloException
     */
    private void manageConstraint() throws IloException {
            for (int i = 0; i < this.cfg.getT(); i++) {
                IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int j = 0; j < this.cfg.getW(); j++) {
                    constraint.addTerm(1, this.zHat[i][j]);
                }

                this.modeler.addEq(constraint, this.x[i], String.format("manage_constraint_chain{%d}", i));
            }
    }


    /**
     * Manage Place Constraint
     * @throws IloException
     */
    private void managePlaceConstraint() throws IloException {
        for (int j = 0; j < this.cfg.getW(); j++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();

            for (int i = 0; i < this.cfg.getT(); i++) {
                constraint.addTerm(1, this.zHat[i][j]);
            }

            this.modeler.addLe(constraint, this.yHat[j], String.format("manage_place_constraint_node{%d}", j));
        }
    }

    /**
     * VNF support constraint
     * @throws IloException
     */
    private void vnfSupportConstraint() throws IloException {
        for (int i = 0; i < this.cfg.getW(); i++) {
            if (!this.cfg.getNodes().get(i).isVnfSupport()) {
            	IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                for (int j = 0; j < this.cfg.getF(); j++) {
                    constraint.addTerm(1, this.y[i][j]);
                }

                this.modeler.addEq(constraint, 0, String.format("vnf_support_constraint_node{%d}", i));
            }
        }
    }

    /**
     * Manager to node support constraint
     * @throws IloException
     */
    private void managerToNodeSupportConstraint() throws IloException {
        int v = 0;
        for (int h = 0; h < this.cfg.getT(); h++) {
            for (int k = 0; k < this.cfg.getChains().get(h).nodes(); k++) {
                for (int i = 0; i < this.cfg.getF(); i++) {

                    // skip if k-th vnf does not have type i
                    if (this.cfg.getChains().get(h).getNode(k).getIndex() != i) {
                        continue;
                    }

                    for (int j = 0; j < this.cfg.getW(); j++) {
                        IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                        for (int n = 0; n < this.cfg.getW(); n++) {
                            if (this.cfg.getNodes().get(n).getNotManagerNodes().contains(j)) {
                                constraint.addTerm(1, this.zHat[h][n]);
                            }
                        }
                        // if constraint is empty skip it!
                        if (!constraint.linearIterator().hasNext()) {
                            continue;
                        }

                        IloLinearIntExpr rhs = this.modeler.linearIntExpr(1);
                        rhs.addTerm(-1, z[i][j][k + v]);

                        this.modeler.addLe(constraint,  rhs, String.format("manager_to_node_support_constraint_chain{%d}_vnf{%d}_type{%d}_node{%d}", h, k + v, i, j));
                    }
                }
            }
            v += this.cfg.getChains().get(h).nodes();
        }
    }

    /**
     * Manager Capacity Constraint
     * @throws IloException
     */
    private void managerCapacityConstraint() throws IloException {
        for (int i = 0; i < this.cfg.getW(); i++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();

            for (int j = 0; j < this.cfg.getT(); j++) {
                constraint.addTerm(this.cfg.getChains().get(j).nodes(), this.zHat[j][i]);
            }

            this.modeler.addLe(constraint, this.cfg.getVnfmCapacity(),
                    String.format("manager_capacity_constraint_%d", i));
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
        for (Chain chain : this.cfg.getChains()) {
            for (int i = 0; i < this.cfg.getW(); i++) {  // Source of Physical link
                for (int l = 0; l < chain.links(); l++) { // Virtual link
                    int virtualSource = chain.getLink(l).getSource() + v;
                    int virtualDestination = chain.getLink(l).getDestination() + v;

                    IloLinearIntExpr linkConstraint = this.modeler.linearIntExpr();
                    IloLinearIntExpr nodeConstraint = this.modeler.linearIntExpr();

                    // link constraint
                    for (int j = 0; j < this.cfg.getW(); j++) { // Destination of Physical link
                        if (this.cfg.getE()[i][j] > 0) {
                            linkConstraint.addTerm(1, this.tau[i][j][u + l]);
                        }
                        if (this.cfg.getE()[j][i] > 0) {
                            linkConstraint.addTerm(-1, this.tau[j][i][u + l]);
                        }
                    }

                    // node constraint
                    for (int k = 0; k < this.cfg.getF(); k++) {
                        if (chain.getNode(virtualSource - v).getIndex() == k) {
                            nodeConstraint.addTerm(1, this.z[k][i][virtualSource]);
                        }
                        if (chain.getNode(virtualDestination - v).getIndex() == k) {
                            nodeConstraint.addTerm(-1, this.z[k][i][virtualDestination]);
                        }
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
        for (int h = 0; h < this.cfg.getT(); h++) {
            for (int i = 0; i < this.cfg.getW(); i++) {  // Source of Physical link
                for (int n = 0; n < this.cfg.getChains().get(h).nodes(); n++) { // Virtual node

                    IloLinearIntExpr linkConstraint = this.modeler.linearIntExpr();
                    IloLinearIntExpr nodeConstraint = this.modeler.linearIntExpr();

                    // link constraint
                    for (int j = 0; j < this.cfg.getW(); j++) { // Destination of Physical link
                        if (this.cfg.getE()[i][j] > 0) {
                            linkConstraint.addTerm(1, this.tauHat[i][j][v + n]);
                        }
                        if (this.cfg.getE()[j][i] > 0) {
                            linkConstraint.addTerm(-1, this.tauHat[j][i][v + n]);
                        }
                    }

                    // node constraint
                    for (int k = 0; k < this.cfg.getF(); k++) {
                        if (this.cfg.getChains().get(h).getNode(n).getIndex() == k) {
                            nodeConstraint.addTerm(1, this.z[k][i][v + n]);
                        }
                    }
                    nodeConstraint.addTerm(-1, this.zHat[h][i]);

                    this.modeler.addEq(linkConstraint, nodeConstraint, "management_flow_conservation");
                }
            }
            v += this.cfg.getChains().get(h).nodes();
        }
    }

    /**
     * Link Bandwidth Constraint
     * @throws IloException
     */
    private void linkBandwidthConstraint() throws IloException {
        for (int i = 0; i < this.cfg.getW(); i++) {
            for (int j = 0; j < this.cfg.getW(); j++) {
                if (this.cfg.getE()[i][j] > 0) {
                    IloLinearIntExpr constraint = this.modeler.linearIntExpr();

                    int u = 0;
                    int v = 0;
                    for (Chain chain : this.cfg.getChains()) {
                        // VNFs
                        for (int k = 0; k < chain.links(); k++) {
                            constraint.addTerm(chain.getLink(k).getBandwidth(), this.tau[i][j][k + u]);
                        }

                        // VNFM
                        for (int k = 0; k < chain.nodes(); k++) {
                            constraint.addTerm(this.cfg.getVnfmBandwidth(), this.tauHat[i][j][k + v]);
                        }
                        v += chain.nodes();
                        u += chain.links();
                    }

                    this.modeler.addLe(constraint, this.cfg.getE()[i][j], "link_bandwidth_constraint");
                }
            }
        }
    }

    /**
     * Radius Constraint
     * @throws IloException
     */
    private void radiusConstraint() throws IloException {
        for (int i = 0; i < this.cfg.getV(); i++) {
            IloLinearIntExpr constraint = this.modeler.linearIntExpr();

            for (int j = 0; j < this.cfg.getW(); j++) {
                for (int k = 0; k < this.cfg.getW(); k++) {
                    if (this.cfg.getE()[j][k] > 0) {
                        constraint.addTerm(1, this.tauHat[j][k][i]);
                    }
                }
            }

            this.modeler.addLe(constraint, this.cfg.getVnfmRadius(), "management_radius_constraint");
        }
    }

    public IloIntVar[] getX() {
        return x;
    }

    public IloIntVar[][] getY() {
        return y;
    }

    public IloIntVar[] getyHat() {
        return yHat;
    }

    public IloIntVar[][][] getZ() {
        return z;
    }

    public IloIntVar[][] getzHat() {
        return zHat;
    }

    public IloIntVar[][][] getTau() {
        return tau;
    }

    public IloIntVar[][][] getTauHat() {
        return tauHat;
    }
}
