package ipgraph.matching;

import com.interdataworking.mm.alg.GraphComparer;
import com.interdataworking.mm.alg.MapPair;
import com.interdataworking.mm.alg.Match;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.rdf.model.*;
import org.w3c.rdf.util.RDFFactory;
import org.w3c.rdf.util.RDFFactoryImpl;

import java.util.*;

/**
 * Created by qingqingcai on 5/4/15.
 */
public class Matching {

    private static boolean debug = true;

    public static final Set<String> postagSet = new HashSet<>(Arrays.asList(new String[]{"NN", "NNS", "NNP", "NNPS", "WP"}));

    GraphComparer graphComparer;

    public Matching(GraphComparer comparer) {
        graphComparer = comparer;
    }

    /** **************************************************************
     * Create Similarity Flooding input model from DGraph
     */
    static Model createModelFromGraph(DGraph graph, ArrayList<Resource> NodeResources) throws ModelException {

        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        // create graph/model M
        Model M = rf.createModel();

        for (DefaultEdge edge : graph.edgeSet()) {
            DNode source = graph.getEdgeSource(edge);
            DNode target = graph.getEdgeTarget(edge);
            Resource s = nf.createResource(source.getForm());
            Resource t = nf.createResource(target.getForm());
            Resource e = nf.createResource(target.getDepLabel());

            NodeResources.add(s);
            NodeResources.add(t);

            M.add(nf.createStatement(s, e, t));
        }

        return M;
    }

    /** **************************************************************
     * Compute the similarity between two DGraphs' nodes
     * @param dgraph1
     * @param dgraph2
     * @return  a MapPair[] sorted by similarity score
     * @throws Exception
     */
    public MapPair[] computeNodeSimilarity(DGraph dgraph1, DGraph dgraph2) throws ModelException {

        ArrayList<Resource> NodeResourcesDGraph1 = new ArrayList<>();
        ArrayList<Resource> NodeResourcesDGraph2 = new ArrayList<>();

        Model A = createModelFromGraph(dgraph1, NodeResourcesDGraph1);
        Model B = createModelFromGraph(dgraph2, NodeResourcesDGraph2);

        // create an initial mapping which is just a cross-product with 1's as weights
        List initMap = new ArrayList();
        for (Resource r1 : NodeResourcesDGraph1) {
            for (Resource r2 : NodeResourcesDGraph2) {
                initMap.add(new MapPair(r1, r2, 1.0));
            }
        }

//        Match sf = new Match();

        // Two lines below are used to get the same setting as in the example of the ICDE'02 paper.
        // (In general, this formula won't converge! So better stick to the default values instead)
//        sf.formula = com.interdataworking.mm.alg.Match.FORMULA_FFT;
//        sf.FLOW_GRAPH_TYPE = com.interdataworking.mm.alg.Match.FG_PRODUCT;

//        MapPair[] result = sf.getMatch(A, B, initMap);

        MapPair[] result = graphComparer.getComparison(A, B, initMap);

        MapPair.sort(result);

        // Answer Extraction
        String answer = extractAnswer(dgraph1, result);
        System.out.println("Answer = " + answer);

        return result;
    }

    /** **************************************************************
     * Extract answers, defined as the node with highest similarity
     * score with WH-node
     */
    static String extractAnswer(DGraph graph, MapPair[] result) throws ModelException {

        String answer = "";
        for (MapPair mp : result) {
            RDFNode leftNode = mp.getLeftNode();
            RDFNode rightNode = mp.getRightNode();
            if (rightNode.getLabel().equals("What")) {
                System.out.println();
                System.out.println("mp.left = " + leftNode.getLabel());
                System.out.println("mp.right = " + rightNode.getLabel());
                System.out.println("mp.similarity = " + mp.sim);
                answer = leftNode.getLabel();
                return answer;
            }
        }
        return answer;
    }

    /**
     * Given a MapPair[], sorts the array by similarity and then finds the highest-ranked pair whose right-node label matches the given string.
     * The expected use case for this method is a MapPair array whose left nodes are an assertion, and whose right nodes are a question about
     * that assertion. However, there is nothing in the method itself that requires this relationship between the left and right nodes.
     * @param allPairs
     *      a MapPair array
     * @param label
     *      a string which will be used to search the labels of the rightNodes in the given MapPair array
     * @return
     *      the MapPair with the highest similarity value whose rightNode label matches the input string
     * @throws ModelException
     */
    static MapPair extractAnswer(MapPair[] allPairs, String label) throws ModelException {
        MapPair answer = new MapPair();

        MapPair.sort(allPairs);
        for (MapPair mp : allPairs) {
            RDFNode rightNode = mp.getRightNode();
            if (rightNode.getLabel().equals("What")) {
                answer = mp;
                break;
            }
        }
        return answer;
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
        System.out.println("1 to 2");

        Match match = new Match();
        match.setFormula(com.interdataworking.mm.alg.Match.FORMULA_FFT);
        match.setFlowGraphType(com.interdataworking.mm.alg.Match.FG_PRODUCT);
        Matching matching = new Matching(match);

        matching.computeNodeSimilarity(dgraph1, dgraph2);
        System.out.println("3 to 2");
        matching.computeNodeSimilarity(dgraph3, dgraph2);
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
            System.out.println("query = " + query);
            for (String text : texts) {
                System.out.println("text = " + text);
                DTree QDTree = DTree.buildTree(query);
                DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);

                DTree TDTree = DTree.buildTree(text);
                DGraph TDGraph = DGraph.buildDGraph(TDTree).getSubgraph(postagSet);

                matching.computeNodeSimilarity(QDGraph, TDGraph);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        test1();
        //test2();
    }

}
