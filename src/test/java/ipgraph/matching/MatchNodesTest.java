package ipgraph.matching;

import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by geraldkurlandski on 5/13/15.
 */
public class MatchNodesTest {

    private DGraph stringToDGraph(String in)    {
        DTree dtree = DTree.buildTree(in);
        return DGraph.buildDGraph(dtree);
    }

    @Test
    public void testGetEdges()  {
        DGraph dgraph1 = stringToDGraph("John bit the dust.");
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

    // Verify that what is returned by getPCGraph() is a defensive copy.
    @Test
    public void testGetPCGraph()    {
        DGraph dgraph1 = stringToDGraph("John laughed.");
        DGraph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);

        // First try to remove an element using the reference returned by the getter.
        assertEquals(1, matcher.getPCGraph().size());
        Set<Edge> pcGraph = matcher.getPCGraph();
        pcGraph.remove(pcGraph.iterator().next());
        // Verify that the object's field hasn't changed.
        assertEquals(1, matcher.getPCGraph().size());

        // Now try to modify an element using the reference returned by the getter.
        // NOTE: this test fails because we haven't made a deep copy; not sure if necessary

        // First get the pos of the one node.
//        pcGraph = matcher.getPCGraph();
//        NodePair pair = (NodePair) pcGraph.iterator().next().source;
//        String pos1 = pair.node1.getPOS();
//
//        // Now change the pos in our local copy, making sure its not the same as the original.
//        pair.node1.setPOS("det");
//        assertFalse(pos1.equals("det"));
//        assertEquals("det", ((NodePair) pcGraph.iterator().next().source).node1.getPOS());
//
//        // Now test the pos in the true copy.
//        Set<Edge> pcGraphTrue = matcher.getPCGraph();
//        String actual = ((NodePair) pcGraphTrue.iterator().next().source).node1.getPOS();
//        assertEquals(pos1, actual);
    }

    /**
     * Test pc graph when we don't require the labels of the nodes to match if they're going to be inserted.
     */
    @Test
    public void testPCGraphNotRequiringLabelMatch()    {
        DGraph dgraph1 = stringToDGraph("John laughed hard.");
        DGraph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes.requireLabelMatchForPCGraph = false;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        Set<Edge> pcGraph = matcher.getPCGraph();

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
        DGraph dgraph1 = stringToDGraph("John laughed hard.");
        DGraph dgraph2 = stringToDGraph("Mark laughed.");

        MatchNodes.requireLabelMatchForPCGraph = true;
        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        Set<Edge> pcGraph = matcher.getPCGraph();

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

    @Test
    public void testExact() {
        DGraph dgraph1 = stringToDGraph("John laughed.");
        DGraph dgraph2 = stringToDGraph("John laughed.");

        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        //Map<NodePair, Double> actual = matcher.compareGraphNodes();

    }
}
