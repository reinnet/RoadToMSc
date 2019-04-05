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
        cfg.addNode(new Node(144, 1408)); // server 1
        cfg.addNode(new Node(144, 1408)); // server 2
        cfg.addNode(new Node(72, 288)); // server 3
        cfg.addNode(new Node(72, 288)); // server 4
        cfg.addNode(new Node(72, 288)); // server 5
        cfg.addNode(new Node(72, 288)); // server 6
        cfg.addNode(new Node(144, 1408)); // server 7
        cfg.addNode(new Node(144, 1408)); // server 8
        cfg.addNode(new Node(0, 0)); // switch 9
        cfg.addNode(new Node(0, 0)); // switch 10
        cfg.addNode(new Node(0, 0)); // switch 11
        cfg.addNode(new Node(0, 0)); // switch 12
        cfg.addNode(new Node(0, 0)); // switch 13
        cfg.addNode(new Node(0, 0)); // switch 14
        cfg.addNode(new Node(0, 0)); // switch 15

        // physical links
        cfg.addLink(new Link(40 * 1000, 0, 11)); // server 1 - switch 12
        cfg.addLink(new Link(40 * 1000, 11, 0));

        cfg.addLink(new Link(40 * 1000, 1, 11)); // server 2 - switch 12
        cfg.addLink(new Link(40 * 1000, 11, 1));

        cfg.addLink(new Link(40 * 1000, 2, 12)); // server 3 - switch 13
        cfg.addLink(new Link(40 * 1000, 12, 2));

        cfg.addLink(new Link(40 * 1000, 3, 12)); // server 4 - switch 13
        cfg.addLink(new Link(40 * 1000, 12, 3));

        cfg.addLink(new Link(40 * 1000, 4, 13)); // server 5 - switch 14
        cfg.addLink(new Link(40 * 1000, 13, 4));

        cfg.addLink(new Link(40 * 1000, 5, 13)); // server 6 - switch 14
        cfg.addLink(new Link(40 * 1000, 13, 5));

        cfg.addLink(new Link(40 * 1000, 6, 14)); // server 7 - switch 15
        cfg.addLink(new Link(40 * 1000, 14, 6));

        cfg.addLink(new Link(40 * 1000, 7, 14)); // server 8 - switch 15
        cfg.addLink(new Link(40 * 1000, 14, 7));

        cfg.addLink(new Link(40 * 1000, 11, 9)); // switch 12 - switch 10
        cfg.addLink(new Link(40 * 1000, 9, 11));

        cfg.addLink(new Link(40 * 1000, 11, 10)); // switch 12 - switch 11
        cfg.addLink(new Link(40 * 1000, 10, 11));

        cfg.addLink(new Link(40 * 1000, 12, 9)); // switch 13 - switch 10
        cfg.addLink(new Link(40 * 1000, 9, 12));

        cfg.addLink(new Link(40 * 1000, 12, 10)); // switch 13 - switch 11
        cfg.addLink(new Link(40 * 1000, 10, 12));

        cfg.addLink(new Link(40 * 1000, 13, 9)); // switch 14 - switch 10
        cfg.addLink(new Link(40 * 1000, 9, 13));

        cfg.addLink(new Link(40 * 1000, 13, 10)); // switch 14 - switch 11
        cfg.addLink(new Link(40 * 1000, 10, 13));

        cfg.addLink(new Link(40 * 1000, 14, 9)); // switch 15 - switch 10
        cfg.addLink(new Link(40 * 1000, 9, 14));

        cfg.addLink(new Link(40 * 1000, 14, 10)); // switch 15 - switch 11
        cfg.addLink(new Link(40 * 1000, 10, 14));

        cfg.addLink(new Link(40 * 1000, 8, 9)); // switch 9 - switch 10
        cfg.addLink(new Link(40 * 1000, 9, 8));

        cfg.addLink(new Link(40 * 1000, 8, 10)); // switch 9 - switch 11
        cfg.addLink(new Link(40 * 1000, 10, 8));

        // VNF types
        Type.add(2, 2); // Type 0 vFW
        Type.add(2, 4); // Type 1 vNAT
        Type.add(2, 2); // Type 2 vIDS

        // SFC requests
        // consider to create requests after creating VNF types
        cfg.addChain(new Chain(10).addNode(0).addNode(1).addLink(1000, 0, 1));
        cfg.addChain(new Chain(10).addNode(0).addNode(2).addLink(1500, 0, 1));

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
                                    if (cplex.getValue(model.getTau()[i][j][u + k]) == 1) {
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
