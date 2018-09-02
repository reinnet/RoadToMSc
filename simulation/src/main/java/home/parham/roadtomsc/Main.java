package home.parham.roadtomsc;

import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;
import home.parham.roadtomsc.model.Config;
import home.parham.roadtomsc.model.Model;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class Main {
    public static void main(String[] args) {
        Config cfg = new Config(
                1,
                1,
                5,
                5,
                1
        );

        // physical nodes
        cfg.addNode(new Node(1, 1));
        cfg.addNode(new Node(1, 1));
        cfg.addNode(new Node(2, 4));
        cfg.addNode(new Node(1, 1));
        cfg.addNode(new Node(1, 1));

        // physical links
        cfg.addLink(new Link(10, 0, 1));
        cfg.addLink(new Link(10, 0, 2));
        cfg.addLink(new Link(10, 2, 0));
        cfg.addLink(new Link(10, 1, 2));
        cfg.addLink(new Link(10, 3, 4));

        // VNF types
        Type.add(1, 1); // Type 0
        Type.add(1, 2); // Type 1

        // SFC requests
        // consider to create requests after creating VNF types
        cfg.addChain(new Chain(10).addNode(0));
        cfg.addChain(new Chain(10).addNode(0).addNode(1).addNode(0).addLink(1, 0, 1)
                .addLink(1, 0, 2));

        // build configuration
        cfg.build();

        try {
            IloCplex cplex = new IloCplex();

            Model model = new Model(cplex, cfg);
            model.variables().objective().constraints();

            cplex.exportModel("simulation.lp");

            if (cplex.solve()) {
                System.out.println();
                System.out.println(" Solution Status = " + cplex.getStatus());
                System.out.println();
                System.out.println(" cost = " + cplex.getObjValue());

                System.out.println();
                System.out.println(" >> Chains");
                for (int i = 0; i < cfg.getT(); i++) {
                    if (cplex.getValue(model.getX()[i]) == 1) {
                        System.out.printf("Chain %s is accepted.\n", i);
                    } else {
                        System.out.printf("Chain %s is not accepted.\n", i);
                    }
                }
                System.out.println();

                System.out.println();
                System.out.println(" >> Instance mapping");
                int v = 0;
                for (int h = 0; h < cfg.getT(); h++) {
                    System.out.printf("Chain %d:\n", h);
                    for (int k = 0; k < cfg.getChains().get(h).nodes(); k++) {
                        for (int i = 0; i < cfg.getF(); i++) {
                            for (int j = 0; j < cfg.getW(); j++) {
                                if (cplex.getValue(model.getZ()[i][j][k + v]) == 1) {
                                    System.out.printf("Node %d with type %d is mapped on %d\n", k, i, j);
                                }
                            }
                        }
                    }
                    v += cfg.getChains().get(h).nodes();
                }
                System.out.println();

                System.out.println();
                System.out.println(" >> Manager mapping");
                for (int h = 0; h < cfg.getT(); h++) {
                    for (int i = 0; i < cfg.getW(); i++) {
                        if (cplex.getValue(model.getzHat()[h][i]) == 1) {
                            System.out.printf("Chain %d manager is %d\n", h, i);
                        }
                    }
                }
                System.out.println();

                System.out.println();
                System.out.println(" >> Instance and Management links");
                int u = 0;
                v = 0;
                for (int h = 0; h < cfg.getT(); h++) {
                    for (int i = 0; i < cfg.getW(); i++) {
                        for (int j = 0; j < cfg.getW(); j++) {
                            if (cfg.getE()[i][j] > 0) {

                                for (int k = 0; k < cfg.getChains().get(h).links(); k++) {
                                    if (cplex.getValue(model.getTauHat()[i][j][u + k]) == 1) {
                                        Link l = cfg.getChains().get(h).getLink(k);
                                        System.out.printf("Chain %d link %d (%d - %d) is on %d-%d\n", h, k,
                                                l.getSource(), l.getDestination(), i, j);
                                    }
                                }

                                for (int k = 0; k < cfg.getChains().get(h).nodes(); k++) {
                                    if (cplex.getValue(model.getTauHat()[i][j][v + k]) == 1) {
                                        System.out.printf("Chain %d node %d manager is on %d-%d\n", h, k, i, j);
                                    }
                                }

                            }
                        }
                    }
                    u += cfg.getChains().get(h).links();
                    v += cfg.getChains().get(h).nodes();
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
