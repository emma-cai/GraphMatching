package ipgraph.matching;

import com.interdataworking.mm.alg.MapPair;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DTree;
import org.junit.Test;
import org.w3c.rdf.model.ModelException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MatchingTest {

    @Test
    public void testGoodMatch() throws ModelException {
        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  Matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("proteins", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.539, actual.sim, .01);
    }

    @Test
    public void testBadMatch() throws ModelException {
        String s1 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  Matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("substrates", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.617, actual.sim, .01);
    }

}
