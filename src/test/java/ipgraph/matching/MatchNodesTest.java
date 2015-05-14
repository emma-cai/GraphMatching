package ipgraph.matching;

import com.google.common.collect.Sets;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import ipgraph.matching.MatchNodes;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

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

        Set<Edge> pcGraphFromField0 = matcher.getPDGraph();
        assertEquals(1, pcGraphFromField0.size());
        pcGraphFromField0.remove(pcGraphFromField0.iterator().next());
        assertEquals(1, matcher.getPDGraph().size());

        Set<Edge> pcGraphFromGetter = matcher.getPDGraph();
    }

    @Test
    public void testExact() {
        DGraph dgraph1 = stringToDGraph("John laughed.");
        DGraph dgraph2 = stringToDGraph("John laughed.");

        MatchNodes matcher = new MatchNodes(dgraph1, dgraph2);
        //Map<NodePair, Double> actual = matcher.compareGraphNodes();

    }
}
