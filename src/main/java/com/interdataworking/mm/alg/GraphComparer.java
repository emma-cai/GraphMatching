package com.interdataworking.mm.alg;

import org.w3c.rdf.model.Model;
import org.w3c.rdf.model.ModelException;

import java.util.List;

/**
 * Known implementations: Match, CompareGraphs
 */
public interface GraphComparer {

    public PGNode[] getComparison(Model m1, Model m2, List sigma0) throws ModelException;

    public void setFlowGraphType(int type);

    public void setFormula(boolean[] vals);

}
