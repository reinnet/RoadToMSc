package home.parham.roadtomsc;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
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
                new Chain(10).addVNF(0)
        };
        final int T = chains.length;

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

            // the number of VNF instances of type _k_ that are used in server w
            String[] yNames = new String[W * F];
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < F; j++) {
                    yNames[i * F + j] = String.format("y_%d_%d", i, j);
                }
            }
            IloIntVar[] y = cplex.intVarArray(W * F, 0, Integer.MAX_VALUE,  yNames);

            // binary variable assuming the value 1 if VNFM on server w is used;
            // otherwise its value is zero
            String[] yHatNames = new String[W];
            for (int i = 0; i < W; i++) {
                yHatNames[i] = String.format("y^_%d", i);
            }
            IloIntVar[] yHat = cplex.boolVarArray(W, yHatNames);

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
                    ramConstraint.addTerm(Type.get(j).getRam(), y[i * F + j]); // instance
                    cpuConstraint.addTerm(Type.get(j).getCores(), y[i * F + j]); // instance
                }

                cplex.addGe(cpuConstraint, nodes[i].getRam(), String.format("node_cpu_constraint %d", i));
                cplex.addGe(ramConstraint, nodes[i].getRam(), String.format("node_memory_constraint %d", i));
            }



        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
