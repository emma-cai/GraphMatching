package ipgraph.matching;

import com.interdataworking.mm.alg.DSimplifiedMatch;
import com.interdataworking.mm.alg.SimplifiedMatch;
import com.interdataworking.mm.alg.MapPair;
import com.interdataworking.mm.alg.Match;
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
    public void testGoodMatch_UsingMatchICDE02() throws ModelException {
        Match match = new Match();
        match.setFormula(com.interdataworking.mm.alg.Match.FORMULA_FFT);
        match.setFlowGraphType(com.interdataworking.mm.alg.Match.FG_PRODUCT);

        Matching matching = new Matching(match);

        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("proteins", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.539, actual.sim, .01);
    }

    @Test
    public void testBadMatch_UsingMatchICDE02() throws ModelException {
        Match match = new Match();
        match.setFormula(com.interdataworking.mm.alg.Match.FORMULA_FFT);
        match.setFlowGraphType(com.interdataworking.mm.alg.Match.FG_PRODUCT);

        Matching matching = new Matching(match);

        String s1 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("substrates", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.617, actual.sim, .01);
    }

    @Test
    public void testGoodMatch_UsingSimplifiedMatch() throws ModelException {
        SimplifiedMatch match = new SimplifiedMatch();

        Matching matching = new Matching(match);

        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("proteins", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.329, actual.sim, .01);
    }

    @Test
    public void testBadMatch_UsingSimplifiedMatch() throws ModelException {
        SimplifiedMatch match = new SimplifiedMatch();

        Matching matching = new Matching(match);

        String s1 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("substrates", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.515, actual.sim, .01);
    }

    @Test
    public void testGoodMatch_UsingDSimplifiedMatch() throws ModelException {
        DSimplifiedMatch match = new DSimplifiedMatch();

        Matching matching = new Matching(match);

        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("proteins", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.329, actual.sim, .01);
    }

    @Test
    public void testBadMatch_UsingDSimplifiedMatch() throws ModelException {
        DSimplifiedMatch match = new DSimplifiedMatch();

        Matching matching = new Matching(match);

        String s1 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(Matching.postagSet);

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(Matching.postagSet);

        MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
        MapPair actual = Matching.extractAnswer(actualPairs, "What");

        assertEquals("substrates", actual.getLeftNode().getLabel());
        assertEquals("What", actual.getRightNode().getLabel());
        assertEquals(0.515, actual.sim, .01);
    }

}
