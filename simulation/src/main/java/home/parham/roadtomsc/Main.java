package home.parham.roadtomsc;

import home.parham.roadtomsc.domain.Chain;
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
        };
        final int W = nodes.length;

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
        for (Chain chain : chains) {
            V += chain.nodes();
        }

        // VNFM
        int nfvmRam = 1;
        int nfvmCores = 1;


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
                ramConstraint.addTerm(nfvmRam, yHat[i]); // VNFM
                cpuConstraint.addTerm(nfvmCores, yHat[i]); // VNFM

                for (int j = 0; j < F; j++) {
                    ramConstraint.addTerm(Type.get(j).getRam(), y[i][j]); // instance
                    cpuConstraint.addTerm(Type.get(j).getCores(), y[i][j]); // instance
                }

                cplex.addLe(cpuConstraint, nodes[i].getRam(), String.format("node_cpu_constraint %d", i));
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

            // Service Constraint
            int v = 0;
            for (int h = 0; h < T; h++) {
                for (int k = 0; k < chains[h].nodes(); k++) {
                    IloLinearIntExpr constraint = cplex.linearIntExpr();

                    for (int i = 0; i < W; i++) {
                        for (int j = 0; j < F; j++) {
                            constraint.addTerm(1, z[i][j][k + v]);
                        }
                    }

                    cplex.addEq(constraint, x[h], String.format("service_constraint %d", h));
                }
                v += chains[h].nodes();
            }



        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
