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
public class MatchNodes implements GraphComparer {

    public static final int MAX_ITERATION_NUM = 10000;
    public static final int MIN_ITERATION_NUM = 7;

    final DGraph leftGraph;
    final DGraph rightGraph;

    final Set<Edge> leftGraphEdges;
    final Set<Edge> rightGraphEdges;

    final Set<Edge> pcGraph = Sets.newHashSet();

    public MatchNodes(DGraph d1, DGraph d2) {
        leftGraph = d1;
        rightGraph = d2;

        leftGraphEdges = getEdges(leftGraph);
        rightGraphEdges = getEdges(rightGraph);

        calcPCGraph();
    }

    /**
     * Create the pairwise connectivity graph.
     */
    private void calcPCGraph() {
        // JERRY: Match.initSigma0( ) does the entire cross-product of the graph nodes,
        // but p. 8 of paper includes only those which share the same edge.

//        pcGraph = Sets.newHashSet();

        for (Edge edgeL : leftGraphEdges)    {
            for (Edge edgeR : rightGraphEdges)   {
                if (edgeL.matches(edgeR)) {
                    NodePair pairL = new NodePair((DNode)edgeL.source, (DNode)edgeR.source);
                    NodePair pairR = new NodePair((DNode)edgeL.target, (DNode)edgeR.target);
                    pcGraph.add(new Edge(pairL, edgeL.label, pairR));
                }
            }
        }

    }

    /**
     * @return
     *  a defensive copy of the pcGraph
     */
    public Set<Edge> getPDGraph()   {
        return Sets.newHashSet(pcGraph);
        //return pcGraph;
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
