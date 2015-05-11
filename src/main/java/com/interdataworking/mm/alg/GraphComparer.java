package com.interdataworking.mm.alg;

import org.w3c.rdf.model.Model;

/**
 * Created by geraldkurlandski on 5/11/15.
 */
public interface GraphComparer {

    public double compareGraphs(Model m1, Model m2);
}
