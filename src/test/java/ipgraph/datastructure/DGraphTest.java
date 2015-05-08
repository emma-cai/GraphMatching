package ipgraph.datastructure;

import com.clearspring.analytics.util.Lists;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class DGraphTest {

    @Test
    public void test()  {
        String sentence = "What are transported into the RER during synthesis?";

        // build tree
        DTree dtree = DTree.buildTree(sentence);
        System.out.println("\ndtree = \n" + dtree.toString());

        // build graph
        DGraph dgraph = DGraph.buildDGraph(dtree);
        System.out.println("\ndgraph = \n" + dgraph.toString());

        // compute and add nodes' levels
        int maxiumlevel = dgraph.addNodeLevel();
        System.out.println("\ndgraph (add levels to nodes) <maximumlevel = " + maxiumlevel + "> = \n" + dgraph.toString());

        // find the path between two nodes
        int sid = 1; int tid = 6;
        DNode sn = dtree.getNodeById(1);
        DNode tn = dtree.getNodeById(6);
        List<DefaultEdge> paths = dgraph.findShortestPath(sn, tn);

        // Create the expected.
        // TODO: This isn't very good--can't we test on the edges?
       String expectedStr = "[(3\ttransported\ttransported\tVERB\tVBN\t_\t0\troot\t_\t_ : 1\tWhat\tWhat\tPRON\tWP\t_\t3\tnsubjpass\t_\t_)," +
               " (3\ttransported\ttransported\tVERB\tVBN\t_\t0\troot\t_\t_ : 4\tinto\tinto\tADP\tIN\t_\t3\tprep\t_\t_)," +
               " (4\tinto\tinto\tADP\tIN\t_\t3\tprep\t_\t_ : 6\tRER\tRER\tPROPN\tNNP\t_\t4\tpobj\t_\t_)]";

        assertEquals(expectedStr, paths.toString());


        // FIXME: Figure out how to create an expected DGraph.

        // build subgraph
//        DGraph dsubgraph_NOUN = dgraph.getSubgraph(DGraph.postagSet);
//
//        DGraph expectedGraph = new DGraph(DefaultEdge.class);
//        assertEquals(expectedGraph, dsubgraph_NOUN);
    }
}