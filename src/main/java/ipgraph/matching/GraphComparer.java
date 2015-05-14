package ipgraph.matching;

import ipgraph.datastructure.LangLib;
import org.w3c.rdf.model.ModelException;

import java.util.*;

/**
 * Compare two graphs. Comparison may take place at the low node level or a high level.
 * Known implementations: MatchNodes
 */
public interface GraphComparer {

    public static final Set<String> postagSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN,
                    LangLib.POS_NNS,
                    LangLib.POS_NNP,
                    LangLib.POS_NNPS,
                    LangLib.POS_WP}));

    /**
     * For two graphs g1 and g2, score how similar each node of g1 is to each node of g2.
     * @param initVals
     *      A set of values for initialization.
     * @return
     */
    public Map<NodePair, Double> compareGraphNodes(Map<NodePair, Double> initVals);


}
