package ipgraph.matching;

import com.interdataworking.mm.alg.Match;
import com.interdataworking.mm.alg.NodeComparer;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.w3c.rdf.model.ModelException;

import java.util.*;

/**
 * Created by qingqingcai on 5/12/15.
 */
public class DMatching {

    public static final Set<String> postagSet = NodeComparer.postagSet;

    // TODO: define excludeVertex
    public static final Set<String> excludeVertex = new HashSet<>(Arrays.asList());

    /** **************************************************************
     * Compute the cost of matching graph_T to graph_H. Basically, the
     * goal is to find the matching with lowest cost.
     */
    public static double computeMatchingCost(DGraph graph_T, DGraph graph_H) {

        double graphSim = Double.MAX_VALUE;
        double VertexCost = computeVertexCost(graph_T, graph_H);
        double PathCost = computePathCost(graph_T, graph_H);
        graphSim = 1.0 * VertexCost + 0 * PathCost;
        return graphSim;
    }

    /** **************************************************************
     * Compute vertex substitution cost to match graph_T to graph_H
     */
    public static double computeVertexCost(DGraph dgraph_T, DGraph dgraph_H) {

        double VertexCost = 0.0;
        double normalize = 0.0;

        for (DNode dnode_H : dgraph_H.vertexSet()) {
            if (excludeVertex.contains(dnode_H)) continue;
            double dnode_weight = 1.0;                 // TODO: decide the weight for each different node
//            normalize += dnode_weight;
            for (DNode dnode_T : dgraph_T.vertexSet()) {
                if (excludeVertex.contains(dnode_T)) continue;
                normalize++;                            // TODO: how to normalize the value

                VertexCost vc = new VertexCost();
                double VertexSub = vc.getNodeSimilarity(dnode_T, dnode_H);
                VertexCost += dnode_weight * VertexSub;
            }
        }
//        System.out.println("\n----------------------------------------");
//        System.out.println("normalize = " + normalize);
//        System.out.println("VertexCost = " + VertexCost);
//        System.out.println("----------------------------------------");
        return VertexCost / normalize;
    }

    /** **************************************************************
     * Compute path substitution cost to match graph_T to graph_H
     */
    public static double computePathCost(DGraph graph_T, DGraph graph_H) {

        double PathCost = 0.0;

        return PathCost;
    }

    public static void main(String[] args) throws Exception {

     //   test1();
     //   test2();
     //   test3();
        test4();
    }

    public static void test1() throws ModelException {

        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(postagSet);
        System.out.println("\nsubgraph1 = \n" + dgraph1.toString());

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(postagSet);
        System.out.println("\nsubgraph2 = \n" + dgraph2.toString());

        String s3 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree3 = DTree.buildTree(s3);
        DGraph dgraph3 = DGraph.buildDGraph(dtree3).getSubgraph(postagSet);
        System.out.println("\nsubgraph3 = \n" + dgraph3.toString());

        System.out.flush();

        double matchingCost = computeMatchingCost(dgraph1, dgraph2);
        System.out.println("1 to 2: " + matchingCost);

        matchingCost = computeMatchingCost(dgraph3, dgraph2);
        System.out.println("3 to 2: " + matchingCost);
    }

    public static void test2() throws Exception {

        Match match = new Match();
        match.setFormula(com.interdataworking.mm.alg.Match.FORMULA_FFT);
        match.setFlowGraphType(com.interdataworking.mm.alg.Match.FG_PRODUCT);
        Matching matching = new Matching(match);


        List<String> texts = new ArrayList<>();
        texts.add("Sarojini Naidu (born as Sarojini Chattopadhyay), also known by the sobriquet as The Nightingale of India, was an Indian independence activist and poet.");
        texts.add("Naidu served as the first governor of the United Provinces of Agra and Oudh from 1947 to 1949; the first woman to become the governor of an Indian state.");
        texts.add("She was the second woman to become the president of the Indian National Congress in 1925 and the first Indian woman to do so.");
        texts.add("Sarojini Naidu was born in Hyderabad to Aghore Nath Chattopadhyay and Barada Sundari Debi on 13 February 1879.");
        texts.add("Her father, with a doctorate of Science from Edinburgh University, settled in Hyderabad, where he founded and administered Hyderabad College, which later became the Nizam's College in Hyderabad.");

        List<String> queries = new ArrayList<>();
        queries.add("Who was also known by the sobriquet as The Nightingale of India?");
        for (String query : queries) {

            DTree QDTree = DTree.buildTree(query);
            DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);
            System.out.println("Graph for query = \n" + QDGraph);

            for (String text : texts) {
                DTree TDTree = DTree.buildTree(text);
                DGraph TDGraph = DGraph.buildDGraph(TDTree).getSubgraph(postagSet);

                double matchingCost = computeMatchingCost(TDGraph, QDGraph);

                System.out.println("\n=========================================");
                System.out.println("Graph for text = " + TDGraph);
                System.out.println("query = " + query);
                System.out.println("text = " + text);
                System.out.println("matchingCost = " + matchingCost);
                System.out.println("=========================================");
            }
        }
    }

    public static void test3() throws ModelException {

        String s1 = "James Cameron is the director of the film Titanic.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1).getSubgraph(postagSet);
        System.out.println("\nsubgraph1 = \n" + dgraph1.toString());

        String s2 = "Who directed Titanic?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2).getSubgraph(postagSet);
        System.out.println("\nsubgraph2 = \n" + dgraph2.toString());

        String s3 = "Jason Blum is the founder and CEO of Blumhouse Productions.";
        DTree dtree3 = DTree.buildTree(s3);
        DGraph dgraph3 = DGraph.buildDGraph(dtree3).getSubgraph(postagSet);
        System.out.println("\nsubgraph3 = \n" + dgraph3.toString());

        System.out.flush();

        double matchingCost = computeMatchingCost(dgraph1, dgraph2);
        System.out.println("1 to 2: " + matchingCost);

        matchingCost = computeMatchingCost(dgraph3, dgraph2);
        System.out.println("3 to 2: " + matchingCost);
    }

    public static void test4() throws Exception {

        Match match = new Match();
        match.setFormula(com.interdataworking.mm.alg.Match.FORMULA_FFT);
        match.setFlowGraphType(com.interdataworking.mm.alg.Match.FG_PRODUCT);
        Matching matching = new Matching(match);

        List<String> texts = new ArrayList<>();
        texts.add("Amelia Mary Earhart (July 24, 1897 – July 2, 1937) was an American aviator, one of the first women to fly a plane long distances.");
        texts.add("She was the first woman to fly a plane by herself across the Atlantic Ocean.");
        texts.add("She broke many records and showed how air travel had moved forward.");
        texts.add("She also wrote books, most of them were about her flights.");
        texts.add("Earhart vanished over the South Pacific Ocean in July 1937 while trying to fly around the world.");

        texts.add("She was declared dead on January 5, 1939.");
        texts.add("Many researchers including navigator and aeronautical engineer Elgen Long believe that the Electra ran out of fuel and that Earhart and Noonan ditched at sea.");
        texts.add("The \"crash and sink\" theory is often the most widely accepted explanation of Earhart’s and Noonan’s fate.");
        texts.add("However, there is a range of documented, archaeological, and anecdotal evidence supporting the hypothesis that Earhart and Noonan found Gardner Island, uninhabited at the time, landed the Electra on a flat reef near the wreck of a freighter, and sent sporadic radio messages from there.");
        texts.add("It has been surmised that Earhart and Noonan might have survived on Nikumaroro for several weeks before succumbing.");


        List<String> queries = new ArrayList<>();
        queries.add("Who was the first woman to fly long distance?");
        queries.add("Who was Amelia Earhart?");
        queries.add("Who broke many records?");

        double minimumCost = 1000;
        String bestAnswer = null;
        for (String query : queries) {

            DTree QDTree = DTree.buildTree(query);
            DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);
            System.out.println("Graph for query = \n" + QDGraph);

            for (String text : texts) {
                DTree TDTree = DTree.buildTree(text);
                DGraph TDGraph = DGraph.buildDGraph(TDTree).getSubgraph(postagSet);

                double matchingCost = computeMatchingCost(TDGraph, QDGraph);
                if (Double.compare(matchingCost, minimumCost) < 0) {
                    minimumCost = matchingCost;
                    bestAnswer = text;
                }

                System.out.println("\n=========================================");
                System.out.println("Graph for text = " + TDGraph);
                System.out.println("query = " + query);
                System.out.println("text = " + text);
                System.out.println("graphSimilarity = " + matchingCost);
                System.out.println("=========================================");
            }

            System.out.println("\n\n\n**************************************************");
            System.out.println("query = " + query);
            System.out.println("best answer = " + bestAnswer);
        }

    }
}
