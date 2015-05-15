package ipgraph.matching.similarityflooding;

import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.matching.GraphComparer;
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

    final Set<NodePair> pcGraphNodes = Sets.newHashSet();

    static boolean requireLabelMatchForPCGraph = true;

    public MatchNodes(DGraph d1, DGraph d2) {
        leftGraph = d1;
        rightGraph = d2;

        leftGraphEdges = getEdges(leftGraph);
        rightGraphEdges = getEdges(rightGraph);

        calcPCGraph(requireLabelMatchForPCGraph);
    }

    /**
     * Create the pairwise connectivity graph.
     * @param requireEdgeMatch
     *  if true, we call match() on the edges, requiring that this method return true before we insert the pair
     *  into the pairwise connectivity graph
     */
    private void calcPCGraph(boolean requireEdgeMatch) {
        // JERRY: Match.initSigma0( ) does the entire cross-product of the graph nodes,
        // but p. 8 of paper includes only those which share the same edge.

        for (Edge edgeL : leftGraphEdges)    {
            for (Edge edgeR : rightGraphEdges)   {
                // If we require a match, then call matches; else, do it
                boolean doIt = requireEdgeMatch ? edgeL.matches(edgeR) : true;
                if (doIt) {
                    NodePair pairL = new NodePair((DNode)edgeL.source, (DNode)edgeR.source);
                    NodePair pairR = new NodePair((DNode)edgeL.target, (DNode)edgeR.target);
                    pcGraph.add(new Edge(pairL, edgeL.label, pairR));
                }
            }
        }

        // Now get a permanent copy of the nodes in the pcGraph.
        pcGraphNodes.addAll(getNodesInGraph(pcGraph));

    }

    public static Set<NodePair> getNodesInGraph(Set<Edge> graph) {
        Set<NodePair> returnSet = Sets.newHashSet();

        for (Edge edge : graph)   {
            returnSet.add((NodePair) edge.source);
            returnSet.add((NodePair) edge.target);
        }

        return returnSet;
    }

    /**
     * @return
     *  a semi-defensive (but not deep) copy of the pcGraph
     */public Set<Edge> getPCGraph()   {
        return Sets.newHashSet(pcGraph);
    }

    /**
     * Return a copy of the nodes in the pairwise connectivity graph.
     * @return
     */
    @Override
    public Set<NodePair> getPCGraphNodes() {
        return Sets.newHashSet(pcGraphNodes);
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
