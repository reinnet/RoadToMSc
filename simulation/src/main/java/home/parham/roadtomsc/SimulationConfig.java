/*
 * In The Name Of God
 * ======================================
 * [] Project Name : roadtomsc
 *
 * [] Package Name : home.parham.roadtomsc
 *
 * [] Creation Date : 21-06-2019
 *
 * [] Created By : Parham Alvani (parham.alvani@gmail.com)
 * =======================================
 */

package home.parham.roadtomsc;

import java.util.List;

public interface SimulationConfig {
    VNFMConfig getVNFM();
    List<NodeConfig> getNodes();
    List<LinkConfig> getLinks();
    List<TypeConfig> getTypes();
    List<ChainConfig> getChains();

    interface TypeConfig {
        String getName();
        int getCores();
        int getRam();
        boolean getEgress();
        boolean getIngress();
        boolean getManageable();
    }

    interface VNFMConfig {
        int getRam();
        int getCores();
        int getCapacity();
        int getRadius();
        int getBandwidth();
    }

    interface NodeConfig {
        String getID();
        int getRam();
        int getCores();
        boolean getVnfSupport();
        List<String> getNotManagerNodes();
        boolean getEgress();
        boolean getIngress();
    }

    interface LinkConfig {
        String getSource();
        String getDestination();
        int getBandwidth();
    }

    interface ChainConfig {
        int getCost();
        List<LinkConfig> getLinks();
        List<NodeConfig> getNodes();

        interface LinkConfig {
            String getSource();
            String getDestination();
            int getBandwidth();
        }

        interface NodeConfig {
            String getType();
            String getID();
        }
    }
}
