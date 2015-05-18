package ipgraph.matching.similarityflooding;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.interdataworking.mm.alg.MapPair;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.Graph;
import ipgraph.matching.GraphComparer;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;
import java.util.Set;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class MatchNodes implements GraphComparer {

    // JERRY: testing: public static final int MAX_ITERATION_NUM = 5;
    public static final int MIN_ITERATION_NUM = 5;
    static int MAX_ITERATION_NUM = 2;
    //static int MIN_ITERATION_NUM = 5;

    final Graph leftGraph;
    final Graph rightGraph;

    final Set<Edge> leftGraphEdges;
    final Set<Edge> rightGraphEdges;

    final Graph pcGraph;

    // JERRY: we need Edge objects to verify edge labels; we need DefaultWeightedEdge's in the Graph object
    final Set<Edge> pcGraphEdges = Sets.newHashSet();

    final Set<NodePair> pcGraphNodes = Sets.newHashSet();

    final DirectedWeightedMultigraph ipGraph;



    static boolean requireLabelMatchForPCGraph = true;

    public MatchNodes(Graph g1, Graph g2) {
        leftGraph = g1;
        rightGraph = g2;

        leftGraphEdges = getEdges(leftGraph);
        rightGraphEdges = getEdges(rightGraph);

        pcGraph = calcPCGraph(requireLabelMatchForPCGraph);

        ipGraph = calcInducedPropagationGraph();
    }

    /**
     * Create the induced propagation graph.
     */
    private DirectedWeightedMultigraph calcInducedPropagationGraph() {

        //DirectedWeightedMultigraph graph = Graph.buildGraph(pcGraphEdges);
        // JERRY: graph.
        DirectedWeightedMultigraph graph = new DirectedWeightedMultigraph(DefaultWeightedEdge.class);

        // For every edge, create an edge going in the opposite direction.
        Set<Edge> reversedEdges = Sets.newHashSet();
        for (Edge edge : pcGraphEdges) {
            NodePair sourcePair = (NodePair) edge.source;
            NodePair targetPair = (NodePair) edge.target;
            graph.addVertex(sourcePair);
            graph.addVertex(targetPair);
            graph.addEdge(sourcePair, targetPair);
            graph.addEdge(targetPair, sourcePair);
        }

        // Set weights.
        for (Object objVertex : graph.vertexSet()) {
            Set<Object> allEdgesOfSourceNode = graph.outgoingEdgesOf(objVertex);
            int nbrEdges = allEdgesOfSourceNode.size();
            for (Object objEdge : allEdgesOfSourceNode)  {
                graph.setEdgeWeight(objEdge, 1/(double) nbrEdges);
            }
        }

//        for (Object objEdge : graph.edgeSet())    {
//
//            DefaultWeightedEdge edge = (DefaultWeightedEdge) objEdge;
//
//            NodePair source = (NodePair) graph.getEdgeSource(edge);
//            Set<Object> allEdgesOfSourceNode = graph.edgesOf(source);
//            int nbrEdges = allEdgesOfSourceNode.size();
//        }

        return graph;
    }

    /**
     * Create the pairwise connectivity graph.
     * @param requireEdgeMatch
     *  if true, we call match() on the edges, requiring that this method return true before we insert the pair
     *  into the pairwise connectivity graph
     */
    private Graph calcPCGraph(boolean requireEdgeMatch) {
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
        // JERRY: call get edges of graph?
        pcGraphNodes.addAll(getNodesInGraph(pcGraphEdges));

        // JERRY: reimplementing pcGraphEdges as a Graph object called pcGraph
        // need to remove old getter on getPCGraphEdges? create new setter?
        return Graph.buildGraph(pcGraphEdges);

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

   /**
     *
     */
    public static Set<Edge> getEdges(Graph graph) {
        Set<Edge> returnSet = Sets.newHashSet();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
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

    /**
     * FIXME: this is not a defensive copy
     * @return
     */
    DirectedWeightedMultigraph getInducedPropGraph() {
        return ipGraph;
    }

    @Override
    public Map<NodePair, Double> compareGraphNodes(Map<NodePair, Double> initVals)  {

        Map<NodePair, Double> fixpointVals = Maps.newHashMap();

        // Initialize
        for (Object objVertex : ipGraph.vertexSet()) {
            NodePair nodePair = (NodePair) objVertex;

            if(initVals.containsKey(nodePair))  {
                nodePair.sim0 = initVals.get(nodePair);
            }
            else    {
                nodePair.sim0 = 0.0;
            }
            fixpointVals.put(nodePair, nodePair.sim0);
        }
        // JERRY: if initVals is empty, I think we want to set all values to 1.0, not 0.0--see original Match class

        //
        int itCnt = 1;
        while (itCnt <=  MAX_ITERATION_NUM)  {
            itCnt++;

            double maxMappingVal = 0.0;

            // for every node in the graph
            for (Object objSource : ipGraph.vertexSet()) {
                NodePair source = (NodePair) objSource;
                double currSourceVal = fixpointVals.get(source);

                Set<Object> allEdgesOfSourceNode = ipGraph.outgoingEdgesOf(objSource);

                // Update each neighbor (target of corresponding edge) with currVal * coefficient
                for (Object objEdge : allEdgesOfSourceNode)   {
                    Object objNeighbor = ipGraph.getEdgeTarget(objEdge);

                    // get the weight of the edge leading from neighbor back to target
                    double coeff = ipGraph.getEdgeWeight(ipGraph.getEdge(objNeighbor, objSource));

                    NodePair neighbor = (NodePair) ipGraph.getEdgeTarget(objEdge);
                    double currNeighborVal = fixpointVals.get(neighbor);

                    double newVal = currSourceVal + (coeff * currNeighborVal);
                    fixpointVals.put(neighbor, newVal);

                    maxMappingVal = Double.max(maxMappingVal, newVal);
                 }
            }

            // Normalize
            for (NodePair mapPair : fixpointVals.keySet())   {
                double newVal = fixpointVals.get(mapPair) / maxMappingVal;
                fixpointVals.put(mapPair, newVal);
            }

            // FIXME: create hasConverged( ) and use in if below
            if (false && itCnt >= MIN_ITERATION_NUM)    {
                break;
            }
        }

        // JERRY: working on this

        return fixpointVals;
    }

    public static Set<NodePair> getNodesInGraph(Set<Edge> graph) {
        Set<NodePair> returnSet = Sets.newHashSet();

        for (Edge edge : graph)   {
            returnSet.add((NodePair) edge.source);
            returnSet.add((NodePair) edge.target);
        }

        return returnSet;
    }

}
