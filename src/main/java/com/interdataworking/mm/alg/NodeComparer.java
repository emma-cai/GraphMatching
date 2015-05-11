package com.interdataworking.mm.alg;

import ipgraph.datastructure.LangLib;
import org.w3c.rdf.model.Model;
import org.w3c.rdf.model.ModelException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Known implementations: Match, SimplifiedMatch
 */
public interface NodeComparer {

    public static final Set<String> postagSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN,
                    LangLib.POS_NNS,
                    LangLib.POS_NNP,
                    LangLib.POS_NNPS,
                    LangLib.POS_WP}));

    public PGNode[] getComparison(Model m1, Model m2, List sigma0) throws ModelException;

    public void setFlowGraphType(int type);

    public void setFormula(boolean[] vals);

}
