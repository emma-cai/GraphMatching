package ipgraph.matching;

import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DTree;

import java.util.*;

import static ipgraph.matching.Matching.GraphSimilarityCost;

/**
* Created by qingqingcai on 5/4/15.
*/
public class Main {

    private static Set<String> postagSet = new HashSet<>(Arrays.asList(new String[]{"NN", "NNS", "NNP", "NNPS"}));

    public static void main(String[] args) {

        test1();
    //    test2();
    }

    public static void test1() {

        String s1 = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        DTree dtree1 = DTree.buildTree(s1);
        DGraph dgraph1 = DGraph.buildDGraph(dtree1);
        DGraph dsubgraph1 = dgraph1.getSubgraph(postagSet);
        System.out.println("\nsubgraph1 = \n" + dsubgraph1.toString());

        String s2 = "What are transported into the RER during synthesis?";
        DTree dtree2 = DTree.buildTree(s2);
        DGraph dgraph2 = DGraph.buildDGraph(dtree2);
        DGraph dsubgraph2 = dgraph2.getSubgraph(postagSet);
        System.out.println("\nsubgraph2 = \n" + dsubgraph2.toString());

        String s3 = "Biosynthesis (also called biogenesis) is an enzyme-catalyzed process in cells of living organisms by which substrates are converted to more complex products (also simply known as protein translation).";
        DTree dtree3 = DTree.buildTree(s3);
        DGraph dgraph3 = DGraph.buildDGraph(dtree3);
        DGraph dsubgraph3 = dgraph3.getSubgraph(postagSet);
        System.out.println("\nsubgraph3 = \n" + dsubgraph3.toString());

        double similarity_s1_s2 = GraphSimilarityCost(dsubgraph1, dsubgraph2);
    //    double similarity_s3_s2 = GraphSimilarityCost(dsubgraph3, dsubgraph2);

        System.out.println("s1 = " + s1);
        System.out.println("s2 = " + s2);
        System.out.println("s3 = " + s3);
        System.out.println("similarity_s1_s2 = " + similarity_s1_s2);
    //     System.out.println("similarity_s3_s2 = " + similarity_s3_s2);
    }

    public static void test2() {

        List<String> texts = new ArrayList<>();
        texts.add("Sarojini Naidu (born as Sarojini Chattopadhyay), also known by the sobriquet as The Nightingale of India, was an Indian independence activist and poet.");
        texts.add("Naidu served as the first governor of the United Provinces of Agra and Oudh from 1947 to 1949; the first woman to become the governor of an Indian state.");
        texts.add("She was the second woman to become the president of the Indian National Congress in 1925 and the first Indian woman to do so.");
        texts.add("Sarojini Naidu was born in Hyderabad to Aghore Nath Chattopadhyay and Barada Sundari Debi on 13 February 1879.");
        texts.add("Her father, with a doctorate of Science from Edinburgh University, settled in Hyderabad, where he founded and administered Hyderabad College, which later became the Nizam's College in Hyderabad.");

        List<String> queries = new ArrayList<>();
        queries.add("Who was also known by the sobriquet as The Nightingale of India?");
        for (String query : queries) {
            System.out.println("query = " + query);
            for (String text : texts) {
                System.out.println("text = " + text);
                DTree QDTree = DTree.buildTree(query);
                DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);

                DTree TDTree = DTree.buildTree(text);
                DGraph TDGraph = DGraph.buildDGraph(TDTree).getSubgraph(postagSet);

                double graphSimilarity = GraphSimilarityCost(QDGraph, TDGraph);
                System.out.println("similarity = " + graphSimilarity + "\n");
            }
        }
    }
}
