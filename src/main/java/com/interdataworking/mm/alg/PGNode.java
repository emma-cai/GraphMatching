package com.interdataworking.mm.alg;

/**
 * Created by geraldkurlandski on 5/11/15.
 */
/**
 * A node in the propagation graph
 */
public class PGNode extends MapPair {

    double sim0;
    // double sim; corresponds to simN, defined in MapPair
    double simN1; // N+1
    double simN; // for comparing vectors, storage only

    public PGNode(Object r1, Object r2) {

        super(r1, r2);
    }

    public String toString() {

        return "[" + getLeft() + "," + getRight() + ": sim=" + sim + ", init=" + sim0 + ", N=" + simN + ", N1=" + simN1 + (inverse ? ", inverse" : "") + "]";
    }
}
