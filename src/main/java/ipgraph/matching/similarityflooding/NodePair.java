package ipgraph.matching.similarityflooding;

import ipgraph.datastructure.DNode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class NodePair {
    public static final double NULL_SIMILARITY = Double.MIN_VALUE;

    DNode node1;
    DNode node2;

    // Final similarity
    public double sim = NULL_SIMILARITY;

    // Initialized value
    double sim0 = NULL_SIMILARITY;

    // Intermediary values.
    double simN1 = NULL_SIMILARITY;
    double simN = NULL_SIMILARITY;

    public NodePair(DNode n1, DNode n2) {
        node1 = n1;
        node2 = n2;
    }

    @Override
    public String toString() {
        return "NodePair[" + "node1=" + node1 + "; node2=" + node2 + "; sim=" + sim + "; sim0=" + sim0 + "; simN=" + simN + "; simN1=" + simN1;
    }

    @Override
    public int hashCode()   {
        return new HashCodeBuilder(17, 31).
                append(node1).
                append(node2).
//                append(sim).
//                append(sim0).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodePair))
            return false;
        if (obj == this)
            return true;

        NodePair rhs = (NodePair) obj;
        return new EqualsBuilder().
                append(node1, rhs.node1).
                append(node2, rhs.node2).
//                append(sim, rhs.sim).
//                append(sim0, rhs.sim0).
                isEquals();
    }

}
