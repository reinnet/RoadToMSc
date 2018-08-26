package home.parham.roadtomsc;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

public class Main {
    public static void main(String[] args) {
        // physical nodes
        final Node[] nodes = {
            new Node(2, 2),
            new Node(2, 2),
        };
        final int W = nodes.length;

        // physical links
        final Link[] links = {
                new Link(10, 0, 1),
        };
        int E[][] = new int[W][W];
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < W; j++) {
                E[i][j] = 0;
            }
        }
        for (Link link : links) {
            E[link.getSource()][link.getDestination()] = link.getDestination();
        }

        // number of VNF types
        Type.add(1, 1); // Type 0
        final int F = Type.len();

        // number of SFC requests
        // consider to create requests after creating VNF types
        final Chain[] chains = {
                new Chain(10).addNode(0)
        };
        final int T = chains.length;
        int V = 0; // Total number of VNF
        int U = 0; // Total number of virtual links
        for (Chain chain : chains) {
            V += chain.nodes();
            U += chain.links();
        }

        // VNFM
        int vnfmRam = 1;
        int vnfmCores = 1;
        int vnfmCapacity = 5;
        int vnfmRadius = 5;
        int vnfmBandwidth = 1;


        try {
            IloCplex cplex = new IloCplex();

            // binary variable assuming the value 1 if the _h_th SFC request is accepted;
            // otherwise its value is zero
            String[] xNames = new String[T];
            for (int i = 0; i < T; i++) {
                xNames[i] = String.format("x_%d", i);
            }
            IloIntVar[] x = cplex.boolVarArray(T, xNames);

            // the number of VNF instances of type _k_ that are used in server _w_
            String[][] yNames = new String[W][F];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < F; j++) {
                    yNames[i][j] = String.format("y_%d_%d", i, j);
                }
            }
            IloIntVar[][] y = new IloIntVar[W][F];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < F; j++) {
                    y[i][j] = cplex.intVar(0, Integer.MAX_VALUE, yNames[i][j]);
                }
            }

            // binary variable assuming the value 1 if VNFM on server w is used;
            // otherwise its value is zero
            String[] yHatNames = new String[W];
            for (int i = 0; i < W; i++) {
                yHatNames[i] = String.format("y^_%d", i);
            }
            IloIntVar[] yHat = cplex.boolVarArray(W, yHatNames);

            // binary variable assuming the value 1 if the VNF node _v_ is served by the VNF instance of type
            // _k_ in the server _w_
            String[][][] zNames = new String[F][W][V];
            for (int i = 0; i < F; i++) {
                for (int j = 0; j < W; j++) {
                    int v = 0;
                    for (int h = 0; h < T; h++) {
                        for (int k = 0; k < chains[h].nodes(); k++) {
                            zNames[i][j][k + v] = String.format("z_%d_%d_%d-%d", i, j, h, k);
                        }
                        v += chains[h].nodes();
                    }
                }
            }
            IloIntVar[][][] z = new IloIntVar[F][W][V];
            for (int i = 0; i < F; i++) {
                for (int j = 0; j < W; j++) {
                    for (int k = 0; k < V; k++) {
                        z[i][j][k] = cplex.boolVar(zNames[i][j][k]);
                    }
                }
            }

            // binary variable assuming the value 1 if the _h_th SFC is assigned to VNFM
            // on server w
            String[][] zHatNames = new String[T][W];
            for (int i = 0; i < T; i++) {
                for (int j = 0; j < W; j++) {
                    zHatNames[i][j] = String.format("z^_%d_%d", i, j);
                }
            }
            IloIntVar[][] zHat = new IloIntVar[T][W];
            for (int i = 0; i < T; i++) {
                for (int j = 0; j < W; j++) {
                    zHat[i][j] = cplex.boolVar(zHatNames[i][j]);
                }
            }

            // binary variable assuming the value 1 if the virtual link _(u, v)_ is routed on
            // the physical network link from _i_ to _j_.
            String[][][] tauNames = new String[W][W][U];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < W; j++) {
                    int u = 0;
                    for (int h = 0; h < T; h++) {
                        for (int k = 0; k < chains[h].links(); k++) {
                            tauNames[i][j][u + k] = String.format("tau_%d_%d_%d-%d", i, j, h, k);
                        }
                        u += chains[h].links();
                    }
                }
            }
            IloIntVar[][][] tau = new IloIntVar[W][W][U];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < W; j++) {
                    for (int k = 0; k < U; k++) {
                        tau[i][j][k] = cplex.boolVar(tauNames[i][j][k]);
                    }
                }
            }

            // binary variable assuming the value 1 if the management of VNF node _v_
            // is routed on the physical network link from _i_ to _j_.
            String[][][] tauHatNames = new String[W][W][V];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < W; j++) {
                    int v = 0;
                    for (int h = 0; h < T; h++) {
                        for (int k = 0; k < V; k++) {
                            tauHatNames[i][j][v + k] = String.format("tau^_%d_%d_%d-%d", i, j, h, k);
                        }
                        v += chains[h].nodes();
                    }
                }
            }
            IloIntVar[][][] tauHat = new IloIntVar[W][W][V];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < W; j++) {
                    for (int k = 0; k < V; k++) {
                        tauHat[i][j][k] = cplex.boolVar(tauHatNames[i][j][k]);
                    }
                }
            }

            // objective function
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < T; i++) {
                expr.addTerm(chains[i].getCost(), x[i]);
            }
            cplex.addMaximize(expr);

            // Node Memory/CPU Constraint
            for (int i = 0; i < W; i++) {
                IloLinearNumExpr ramConstraint = cplex.linearNumExpr();
                IloLinearNumExpr cpuConstraint = cplex.linearNumExpr();
                ramConstraint.addTerm(vnfmRam, yHat[i]); // VNFM
                cpuConstraint.addTerm( vnfmCores, yHat[i]); // VNFM

                for (int j = 0; j < F; j++) {
                    ramConstraint.addTerm(Type.get(j).getRam(), y[i][j]); // instance
                    cpuConstraint.addTerm(Type.get(j).getCores(), y[i][j]); // instance
                }

                cplex.addLe(cpuConstraint, nodes[i].getCores(), String.format("node_cpu_constraint %d", i));
                cplex.addLe(ramConstraint, nodes[i].getRam(), String.format("node_memory_constraint %d", i));
            }

            // Service Place Constraint
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < F; j++) {
                    IloLinearIntExpr constraint = cplex.linearIntExpr();

                    for (int k = 0; k < V; k++) {
                        constraint.addTerm(1, z[i][j][k]);
                    }

                    cplex.addLe(constraint, y[i][j], String.format("service_place_constraint %d %d", i, j));
                }
            }

            // Service Constraint + Type constraint
            int v = 0;
            for (int h = 0; h < T; h++) {
                for (int k = 0; k < chains[h].nodes(); k++) {
                    IloLinearIntExpr constraint = cplex.linearIntExpr();

                    for (int i = 0; i < W; i++) {
                        for (int j = 0; j < F; j++) {
                            cplex.addLe(z[i][j][k + v], chains[h].getNode(k).getIndex() == j ? 1 : 0, "type_constraint");
                            constraint.addTerm(1, z[i][j][k + v]);
                        }
                    }

                    cplex.addEq(constraint, x[h], String.format("service_constraint %d", h));
                }
                v += chains[h].nodes();
            }


            // Manage Constraint
            for (int i = 0; i < T; i++) {
                IloLinearIntExpr constraint = cplex.linearIntExpr();

                for (int j = 0; j < W; j++) {
                    constraint.addTerm(1, zHat[i][j]);
                }

                cplex.addEq(constraint, x[i], String.format("manage_constraint %d", i));
            }

            // Manage Place Constraint
            for (int i = 0; i < T; i++) {
                for (int j = 0; j < W; j++) {
                    IloLinearIntExpr constraint = cplex.linearIntExpr();

                    constraint.addTerm(1, zHat[i][j]);

                    cplex.addLe(constraint, yHat[j], String.format("manage place constraint %d", j));
                }
            }

            // Manager Capacity Constraint
            for (int i = 0; i < W; i++) {
                IloLinearIntExpr constraint = cplex.linearIntExpr();

                for (int j = 0; j < T; j++) {
                    constraint.addTerm(1, zHat[j][i]);
                }

                cplex.addLe(constraint, vnfmCapacity, String.format("manager_capacity_constraint %d", i));
            }


            // Flow conservation
            // linkConstraint == nodeConstraint
            v = 0;
            for (Chain chain : chains) {
                for (int i = 0; i < W; i++) {  // Source of Physical link
                    for (int l = 0; l < chain.links(); l++) { // Virtual link
                        int virtualSource = chain.getLink(l).getSource() + v;
                        int virtualDestination = chain.getLink(l).getDestination() + v;

                        IloLinearIntExpr linkConstraint = cplex.linearIntExpr();
                        IloLinearIntExpr nodeConstraint = cplex.linearIntExpr();

                        for (int j = 0; j < W; j++) { // Destination of Physical link
                            if (E[i][j] > 0) {
                                linkConstraint.addTerm(1, tau[i][j][l]);
                            }
                            if (E[j][i] > 0) {
                                linkConstraint.addTerm(-1, tau[j][i][l]);
                            }
                        }

                        for (int k = 0; k < F; k++) {
                            nodeConstraint.addTerm(1, z[k][i][virtualSource]);
                            nodeConstraint.addTerm(-1, z[k][i][virtualDestination]);
                        }

                        cplex.addEq(linkConstraint, nodeConstraint, "flow_conservation");
                    }
                }
                v += chain.nodes();
            }

            // Management Flow conservation
            // linkConstraint == nodeConstraint
            v = 0;
            for (int h = 0; h < T; h++) {
                for (int i = 0; i < W; i++) {  // Source of Physical link
                    for (int n = 0; n < chains[h].nodes(); n++) { // Virtual link
                        IloLinearIntExpr linkConstraint = cplex.linearIntExpr();
                        IloLinearIntExpr nodeConstraint = cplex.linearIntExpr();

                        for (int j = 0; j < W; j++) { // Destination of Physical link
                            if (E[i][j] > 0) {
                                linkConstraint.addTerm(1, tauHat[i][j][v + n]);
                            }
                            if (E[j][i] > 0) {
                                linkConstraint.addTerm(-1, tauHat[j][i][v + n]);
                            }
                        }

                        for (int k = 0; k < F; k++) {
                            nodeConstraint.addTerm(1, z[k][i][v + n]);
                        }
                        nodeConstraint.addTerm(-1, zHat[h][i]);

                        cplex.addEq(linkConstraint, nodeConstraint, "management flow_conservation");
                    }
                }
                v += chains[h].nodes();
            }


            // Link Bandwidth Constraint
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < W; j++) {
                    if (E[i][j] > 0) {
                        IloLinearIntExpr constraint = cplex.linearIntExpr();

                        int linkCounter = 0;
                        int nodeCounter = 0;
                        for (int h = 0; h < T; h++) {
                            // VNFs
                            for (int k = 0; k < chains[h].links(); k++) {
                                constraint.addTerm(chains[h].getLink(k).getBandwidth(), tau[i][j][k + linkCounter]);
                            }

                            // VNFM
                            for (int k = 0; k < chains[h].nodes(); k++) {
                                constraint.addTerm(vnfmBandwidth, tauHat[i][j][k + nodeCounter]);
                            }
                            nodeCounter += chains[h].nodes();
                            linkCounter += chains[h].links();
                        }

                        cplex.addLe(constraint, E[i][j]);
                    }
                }
            }


            // Radius Constraint
            for (int i = 0; i < V; i++) {
                IloLinearIntExpr constraint = cplex.linearIntExpr();

                for (int j = 0; j < W; j++) {
                    for (int k = 0; k < W; k++) {
                        if (E[j][k] > 0) {
                            constraint.addTerm(1, tauHat[j][k][i]);
                        }
                    }
                }

                cplex.addLe(constraint, vnfmRadius);
            }


            cplex.exportModel("simulation.lp");

            if (cplex.solve()) {
                System.out.println();
                System.out.println(" Solution Status = " + cplex.getStatus());
                System.out.println();
                System.out.println(" cost = " + cplex.getObjValue());


                System.out.println();
                System.out.println(" >> Chains");
                for (int i = 0; i < T; i++) {
                    if (cplex.getValue(x[i]) == 1) {
                        System.out.printf("Chain %s is accepted.\n", i);
                    } else {
                        System.out.printf("Chain %s is not accepted.\n", i);
                    }
                }
                System.out.println();

                System.out.println();
                System.out.println(" >> Instance mapping");
                v = 0;
                for (int h = 0; h < T; h++) {
                    System.out.printf("Chain %d:\n", h);
                    for (int k = 0; k < chains[h].nodes(); k++) {
                        for (int i = 0; i < W; i++) {
                            for (int j = 0; j < F; j++) {
                                if (cplex.getValue(z[i][j][k + v]) == 1) {
                                    System.out.printf("Node %d with type %d is mapped on %d\n", k, j, i);
                                }
                            }
                        }
                    }
                    v += chains[h].nodes();
                }
                System.out.println();

                System.out.println();
                System.out.println(" >> Manager mapping");
                for (int h = 0; h < T; h++) {
                    for (int i = 0; i < W; i++) {
                        if (cplex.getValue(zHat[h][i]) == 1) {
                            System.out.printf("Chain %d manager is %d\n", h, i);
                        }
                    }
                }
                System.out.println();
            } else {
                System.out.printf("Solve failed: %s\n", cplex.getStatus());
            }


        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
