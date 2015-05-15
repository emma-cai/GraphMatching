package ipgraph.matching.similarityflooding;

import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.Graph;
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

    final Graph leftGraph;
    final Graph rightGraph;

    final Set<Edge> leftGraphEdges;
    final Set<Edge> rightGraphEdges;

    final Set<Edge> pcGraphEdges = Sets.newHashSet();

    final Set<NodePair> pcGraphNodes = Sets.newHashSet();

    static boolean requireLabelMatchForPCGraph = true;

    public MatchNodes(Graph g1, Graph g2) {
        leftGraph = g1;
        rightGraph = g2;

        leftGraphEdges = getEdges(leftGraph);
        rightGraphEdges = getEdges(rightGraph);

        calcPCGraph(requireLabelMatchForPCGraph);

        calcInPropGraph();
    }

    /**
     * Create the induced propogation graph.
     */
    private void calcInPropGraph() {
    }

    /**
     * Create the pairwise connectivity graph.
     * @param requireEdgeMatch
     *  if true, we call match() on the edges, requiring that this method return true before we insert the pair
     *  into the pairwise connectivity graph
     */
    private void calcPCGraph(boolean requireEdgeMatch) {
        // Note: Match.initSigma0( ) does the entire cross-product of the graph nodes,
        // but p. 8 of paper includes only those which share the same edge.

        for (Edge edgeL : leftGraphEdges)    {
            for (Edge edgeR : rightGraphEdges)   {
                // If we require a match, then call matches; else, do it
                boolean doIt = requireEdgeMatch ? edgeL.matches(edgeR) : true;
                if (doIt) {
                    NodePair pairL = new NodePair((DNode)edgeL.source, (DNode)edgeR.source);
                    NodePair pairR = new NodePair((DNode)edgeL.target, (DNode)edgeR.target);
                    pcGraphEdges.add(new Edge(pairL, edgeL.label, pairR));
                }
            }
        }

        // Now get a permanent copy of the nodes in the pcGraph.
        pcGraphNodes.addAll(getNodesInGraph(pcGraphEdges));

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
     */public Set<Edge> getPCGraphEdges()   {
        return Sets.newHashSet(pcGraphEdges);
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
    public static Set<Edge> getEdges(Graph graph) {
        Set<Edge> returnSet = Sets.newHashSet();

        for (DefaultEdge edge : graph.edgeSet()) {
            Object s = graph.getEdgeSource(edge);
            Object t = graph.getEdgeTarget(edge);

            String label = "";

            // FIXME: behavior depends on type of node
            if (t instanceof DNode) {
                DNode node = (DNode) t;
                label = node.getDepLabel();
            }

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
