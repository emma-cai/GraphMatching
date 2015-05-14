package ipgraph.matching;

import ipgraph.datastructure.DNode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class Edge {
    DNode source;
    DNode target;

    String label;

    public Edge(DNode n1, String name, DNode n2)    {
        source = n1;
        target = n2;
        label = name;
    }

    @Override
    public int hashCode()   {
        return new HashCodeBuilder(17, 31).
                append(source).
                append(label).
                append(target).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge))
            return false;
        if (obj == this)
            return true;

        Edge rhs = (Edge) obj;
        return new EqualsBuilder().
                append(source, rhs.source).
                append(label, rhs.label).
                append(target, rhs.target).
                isEquals();
    }

    @Override
    public String toString()    {
        return "[source: " + source + "; label: " + label + "; target: " + target + "]";
    }
}
