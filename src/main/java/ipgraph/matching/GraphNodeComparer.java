package ipgraph.matching;

import ipgraph.datastructure.LangLib;
import org.w3c.rdf.model.ModelException;

import java.util.*;

/**
 * Known implementations:
 */
public interface GraphNodeComparer {

    public static final Set<String> postagSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN,
                    LangLib.POS_NNS,
                    LangLib.POS_NNP,
                    LangLib.POS_NNPS,
                    LangLib.POS_WP}));

    public Map<NodePair, Double> compareGraphNodes(Map<NodePair, Double> initVals);


}
