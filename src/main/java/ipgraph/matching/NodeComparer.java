package ipgraph.matching;

import ipgraph.datastructure.DNode;

/**
 * Compares two nodes.
 */
public interface NodeComparer {

    /**
     * Calculcate a value from 0 to 1 inclusive that indicates how similar the two nodes are.
     * @param node1
     * @param node2
     * @return
     */
    double getNodeSimilarity(DNode node1, DNode node2);

}
