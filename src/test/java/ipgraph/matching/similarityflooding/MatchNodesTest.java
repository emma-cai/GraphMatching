package ipgraph.matching.similarityflooding;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import ipgraph.datastructure.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class MatchNodesTest {

    private Graph stringToDGraph(String in)    {
        DTree dtree = DTree.buildTree(in);
        return Graph.buildDGraph(dtree);
    }

    @Test
    public void testGetEdges()  {
        Graph dgraph1 = stringToDGraph("John bit the dust.");
        Set<Edge> actual = MatchNodes.getEdges(dgraph1);

        // Find the nodes associated with each word.
        DNode john, bit, the, dust;
        john = bit = the = dust = null;
        Set<DNode> nodes = dgraph1.getNodesByLevel(0);
        for (DNode node : nodes)    {
            if (node.getForm().equals("John"))
                john = node;
            else if (node.getForm().equals("bit"))
                bit = node;
            else if (node.getForm().equals("the"))
                the = node;
            else if (node.getForm().equals("dust"))
                dust = node;
        }

        Set<Edge> expected = Sets.newHashSet();
        expected.add(new Edge(bit, "nsubj", john));
        expected.add(new Edge(bit, "dobj", dust));
        expected.add(new Edge(dust, "det", the));

        assertEquals(expected, actual);
    }

    // Verify that what is returned by getPCGraphEdges() is a defensive copy.
    @Test
    public void testGetPCGraph()    {
        Graph dgraph1 = stringToDGraph("John laughed.");
        Graph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);

        // First try to remove an element using the reference returned by the getter.
        assertEquals(1, matcher.getPCGraphEdges().size());
        Set<Edge> pcGraph = matcher.getPCGraphEdges();
        pcGraph.remove(pcGraph.iterator().next());
        // Verify that the object's field hasn't changed.
        assertEquals(1, matcher.getPCGraphEdges().size());

        // Now try to modify an element using the reference returned by the getter.
        // NOTE: The test below fails because we haven't made a deep copy; not sure if necessary

        // First get the pos of the one node.
//        pcGraph = matcher.getPCGraphEdges();
//        NodePair pair = (NodePair) pcGraph.iterator().next().source;
//        String pos1 = pair.node1.getPOS();
//
//        // Now change the pos in our local copy, making sure its not the same as the original.
//        pair.node1.setPOS("det");
//        assertFalse(pos1.equals("det"));
//        assertEquals("det", ((NodePair) pcGraph.iterator().next().source).node1.getPOS());
//
//        // Now test the pos in the true copy.
//        Set<Edge> pcGraphTrue = matcher.getPCGraphEdges();
//        String actual = ((NodePair) pcGraphTrue.iterator().next().source).node1.getPOS();
//        assertEquals(pos1, actual);
    }

    /**
     * Test pc graph when we don't require the labels of the nodes to match if they're going to be inserted.
     */
    @Test
    public void testPCGraphNotRequiringLabelMatch()    {
        Graph dgraph1 = stringToDGraph("John laughed hard.");
        Graph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes.requireLabelMatchForPCGraph = false;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        Set<Edge> pcGraph = matcher.getPCGraphEdges();

        DNode john, laughed, hard;
        john = laughed = hard = null;
        Set<DNode> nodes = dgraph1.getNodesByLevel(0);
        for (DNode node : nodes)    {
            if (node.getForm().equals("John"))
                john = node;
            else if (node.getForm().equals("laughed"))
                laughed = node;
            else if (node.getForm().equals("hard"))
                hard = node;
        }

        DNode mark, laughed2;
        mark = laughed2 = null;
        Set<DNode> nodes2 = dgraph2.getNodesByLevel(0);
        for (DNode node : nodes2)    {
            if (node.getForm().equals("Mark"))
                mark = node;
            else if (node.getForm().equals("laughed"))
                laughed2 = node;
        }

        Set<Edge> expected = Sets.newHashSet();
        NodePair nodePair1 = new NodePair(laughed, laughed2);
        NodePair nodePair2 = new NodePair(john, mark);
        expected.add(new Edge(nodePair1, "nsubj", nodePair2));
        NodePair nodePair3 = new NodePair(hard, mark);
        expected.add(new Edge(nodePair1, "acomp", nodePair3));

        assertEquals(expected, pcGraph);

        // Now verify pcGraphNodes.
        Set<NodePair> actualNodes = matcher.getPCGraphNodes();

        Set<NodePair> expectedNodes = Sets.newHashSet(nodePair1, nodePair2, nodePair3);

        assertEquals(expectedNodes, actualNodes);
    }


   /**
     * Test pc graph when we require the labels of the nodes to match if they're going to be inserted.
     */
    @Test
    public void testPCGraphRequiringLabelMatch()    {
        Graph dgraph1 = stringToDGraph("John laughed hard.");
        Graph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes.requireLabelMatchForPCGraph = true;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        Set<Edge> pcGraph = matcher.getPCGraphEdges();

        DNode john, laughed, hard;
        john = laughed = hard = null;
        Set<DNode> nodes = dgraph1.getNodesByLevel(0);
        for (DNode node : nodes)    {
            if (node.getForm().equals("John"))
                john = node;
            else if (node.getForm().equals("laughed"))
                laughed = node;
            else if (node.getForm().equals("hard"))
                hard = node;
        }

        DNode mark, laughed2;
        mark = laughed2 = null;
        Set<DNode> nodes2 = dgraph2.getNodesByLevel(0);
        for (DNode node : nodes2)    {
            if (node.getForm().equals("Mark"))
                mark = node;
            else if (node.getForm().equals("laughed"))
                laughed2 = node;
        }

        Set<Edge> expected = Sets.newHashSet();
        NodePair nodePair1 = new NodePair(laughed, laughed2);
        NodePair nodePair2 = new NodePair(john, mark);
        expected.add(new Edge(nodePair1, "nsubj", nodePair2));

        assertEquals(expected, pcGraph);

        // Now verify pcGraphNodes.
        Set<NodePair> actualNodes = matcher.getPCGraphNodes();

        Set<NodePair> expectedNodes = Sets.newHashSet(nodePair1, nodePair2);

        assertEquals(expectedNodes, actualNodes);
    }

    /**
     * Test induced propagation graph.
     * FIMXE: this test would be easier if we could create an expected DirectedWeightedMultigraph and test the two are identical with equals()
     */
    @Test
    public void testInducedPropagationGraph()   {
        Graph dgraph1 = stringToDGraph("John laughed hard.");
        Graph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes.requireLabelMatchForPCGraph = false;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);

        /**
         * This graph will have three nodes, with two edges connecting each node for a total of four edges.
         * node1: laughed_laughed; node2: john_mark; node3 hard_mark
         * laughed_laughed => 0.5 => john_mark
         * laughed_laughed => 0.5 => hard_mark
         * john_mark => 1.0 => laughed_laughed
         * hard_mark => 1.0 => laughed_laughed
         */
        DirectedWeightedMultigraph inPropGraph = matcher.getInducedPropGraph();

        // Now create the expected output.
        DNode john, laughed, hard;
        john = laughed = hard = null;
        Set<DNode> nodes = dgraph1.getNodesByLevel(0);
        for (DNode node : nodes)    {
            if (node.getForm().equals("John"))
                john = node;
            else if (node.getForm().equals("laughed"))
                laughed = node;
            else if (node.getForm().equals("hard"))
                hard = node;
        }

        DNode mark, laughed2;
        mark = laughed2 = null;
        Set<DNode> nodes2 = dgraph2.getNodesByLevel(0);
        for (DNode node : nodes2)    {
            if (node.getForm().equals("Mark"))
                mark = node;
            else if (node.getForm().equals("laughed"))
                laughed2 = node;
        }

        NodePair laughed_laughed2 = new NodePair(laughed, laughed2);
        NodePair john_mark = new NodePair(john, mark);
        NodePair hard_mark = new NodePair(hard, mark);

        assertEquals(3, inPropGraph.vertexSet().size());
        assertEquals(4, inPropGraph.edgeSet().size());

        Set<DefaultWeightedEdge> actualEdgeSet = inPropGraph.getAllEdges(laughed_laughed2, hard_mark);
        assertEquals(1, actualEdgeSet.size());
        DefaultWeightedEdge weightedEdge = (DefaultWeightedEdge) actualEdgeSet.iterator().next();
        assertEquals(0.5, inPropGraph.getEdgeWeight(weightedEdge), 0);

        actualEdgeSet = inPropGraph.getAllEdges(laughed_laughed2, john_mark);
        assertEquals(1, actualEdgeSet.size());
        weightedEdge = (DefaultWeightedEdge) actualEdgeSet.iterator().next();
        assertEquals(0.5, inPropGraph.getEdgeWeight(weightedEdge), 0);

        actualEdgeSet = inPropGraph.getAllEdges(john_mark, laughed_laughed2);
        assertEquals(1, actualEdgeSet.size());
        weightedEdge = (DefaultWeightedEdge) actualEdgeSet.iterator().next();
        assertEquals(1.0, inPropGraph.getEdgeWeight(weightedEdge), 0);

        actualEdgeSet = inPropGraph.getAllEdges(hard_mark, laughed_laughed2);
        assertEquals(1, actualEdgeSet.size());
        weightedEdge = (DefaultWeightedEdge) actualEdgeSet.iterator().next();
        assertEquals(1.0, inPropGraph.getEdgeWeight(weightedEdge), 0);
    }

    // FIXME: test not finished; awaiting implementation of compareGraphNodes
    @Test
    public void testExact() {
        Graph dgraph1 = stringToDGraph("John laughed.");
        Graph dgraph2 = stringToDGraph("John laughed.");

        MatchNodes.MAX_ITERATION_NUM = 2;
        MatchNodes.requireLabelMatchForPCGraph = true;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);


        NodePair laughed1_laughed2 = null;
        NodePair john1_john2 = null;

        Set<NodePair> nodePairs = matcher.getPCGraphNodes();
        for (NodePair pair : nodePairs) {
            if (pair.node1.getForm().equals("laughed") && pair.node2.getForm().equals("laughed"))  {
                laughed1_laughed2 = pair;
            }
            else if (pair.node1.getForm().equals("John") && pair.node2.getForm().equals("John"))  {
                john1_john2 = pair;
            }
        }


        // Create init values.
        Map<NodePair, Double> initVals = Maps.newHashMap();
        initVals.put(laughed1_laughed2, 1.0);
        initVals.put(john1_john2, 0.5);

        assertEquals(4.9E-324, laughed1_laughed2.sim0, 0);
        assertEquals(4.9E-324, john1_john2.sim0, 0);


        Map<NodePair, Double> actual = matcher.compareGraphNodes(initVals);

        // Test initialized values.
        assertEquals(1.0, laughed1_laughed2.sim0, 0);
        assertEquals(0.5, john1_john2.sim0, 0);


        // Test final values.
        assertEquals(1.0, actual.get(laughed1_laughed2), 0);
        assertEquals(0.5, actual.get(john1_john2), 0);

        assertEquals(2, actual.keySet().size());
    }
}
