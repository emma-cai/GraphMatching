package ipgraph.matching;

import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.Set;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class MatchNodes implements GraphNodeComparer {

    public static final int MAX_ITERATION_NUM = 10000;
    public static final int MIN_ITERATION_NUM = 7;

    DGraph leftGraph;
    DGraph rightGraph;

    public MatchNodes(DGraph d1, DGraph d2) {
        leftGraph = d1;
        rightGraph = d2;
    }

    @Override
    public Map<NodePair, Double> compareGraphNodes(Map<NodePair, Double> initVals)  {
        return null;
    }

    /**
     *
     */
    public static Set<Edge> getEdges(DGraph graph) {
        Set<Edge> returnSet = Sets.newHashSet();

        for (DefaultEdge edge : graph.edgeSet()) {
            DNode s = graph.getEdgeSource(edge);
            DNode t = graph.getEdgeTarget(edge);
            String label = t.getDepLabel();

            // Ignore punctuation and root.
            Set<String> ignoreList = Sets.newHashSet("punct", "root");
            if(ignoreList.contains(label))  {
                continue;
            }

            returnSet.add(new Edge(s, label, t));
        }

        return returnSet;
    }
}
