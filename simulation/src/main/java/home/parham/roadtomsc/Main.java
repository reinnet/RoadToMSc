package home.parham.roadtomsc;

import com.yacl4j.core.ConfigurationBuilder;
import home.parham.roadtomsc.domain.Chain;
import home.parham.roadtomsc.domain.Link;
import home.parham.roadtomsc.domain.Node;
import home.parham.roadtomsc.domain.Type;
import home.parham.roadtomsc.model.Config;
import home.parham.roadtomsc.model.Model;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    private final static Logger logger = Logger.getLogger(Main.class.getName());

    private static void usage() {
        System.out.println("roadtomsc /path/to/configuration.yaml");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            return;
        }

        // load configuration from file
        SimulationConfig config = ConfigurationBuilder.newBuilder()
                .source().fromFile(new File(args[0]))
                .build(SimulationConfig.class);

        // build the model configuration from the loaded configuration

        Config cfg = new Config(
                config.getVNFM().getRam(),
                config.getVNFM().getCores(),
                config.getVNFM().getCapacity(),
                config.getVNFM().getRadius(),
                config.getVNFM().getBandwidth()
        );

        // current mapping between node identification and their index in model
        Map<String, Integer> nodes = new HashMap<>();

        // physical nodes {{{
        for (int i = 0; i < config.getNodes().size(); i++) {
            nodes.put(config.getNodes().get(i).getID(), i);
        }

        for (int i = 0; i < config.getNodes().size(); i++) {
            SimulationConfig.NodeConfig nodeConfig = config.getNodes().get(i);
            Node node = new Node(
                    nodeConfig.getCores(),
                    nodeConfig.getRam(),
                    nodeConfig.getVnfSupport(),
                    new HashSet<>(nodeConfig.getNotManagerNodes().stream().map(nodes::get).collect(Collectors.toList()))
            );
            cfg.addNode(node);
            logger.info(String.format("create physical node (%s) in index %d [%s]", nodeConfig.getID(), i, node));
        }
        // }}}

        // physical links {{{
        config.getLinks().forEach(linkConfig -> {
            Link l1 = new Link(
                    linkConfig.getBandwidth() * 1000,
                    nodes.get(linkConfig.getSource()),
                    nodes.get(linkConfig.getDestination())
            );
            cfg.addLink(l1);
            logger.info(String.format("create physical link from %s to %s [%s]",
                    linkConfig.getSource(), linkConfig.getDestination(), l1));
            Link l2 = new Link(
                    linkConfig.getBandwidth() * 1000,
                    nodes.get(linkConfig.getDestination()),
                    nodes.get(linkConfig.getSource())
            );
            cfg.addLink(l2);
            logger.info(String.format("create physical link from %s to %s [%s]",
                    linkConfig.getDestination(), linkConfig.getSource(), l2));
        });
        /// }}}

        // current mapping between vnf type identification and their index in model
        Map<String, Integer> types = new HashMap<>();


        // VNF types {{{
        config.getTypes().forEach(typeConfig -> {
            Type.add(typeConfig.getCores(), typeConfig.getRam());
            types.put(typeConfig.getName(), Type.len() - 1);
            logger.info(String.format("create virtual type %s [cores: %d, ram: %d]",
                    typeConfig.getName(),
                    typeConfig.getCores(),
                    typeConfig.getRam()
            ));
        });
        // }}}

        // SFC requests {{{
        // consider to create requests after creating VNF types
        config.getChains().forEach(chainConfig -> {
            Chain chain = new Chain(chainConfig.getCost());

            Map<String, Integer> vNodes = new HashMap<>();

            for (int i = 0; i < chainConfig.getNodes().size(); i++) {
                SimulationConfig.ChainConfig.NodeConfig n = chainConfig.getNodes().get(i);
                chain.addNode(types.get(n.getType()));
                vNodes.put(n.getID(), i);
            }

            chainConfig.getLinks().forEach(linkConfig -> chain.addLink(
                    linkConfig.getBandwidth(),
                    vNodes.get(linkConfig.getSource()),
                    vNodes.get(linkConfig.getDestination())
            ));

            cfg.addChain(chain);
        });
        // }}}

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
