package ipgraph.matching;

import com.interdataworking.mm.alg.MapPair;
import com.interdataworking.mm.alg.Match;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.rdf.model.*;
import org.w3c.rdf.util.RDFFactory;
import org.w3c.rdf.util.RDFFactoryImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 5/4/15.
 */
public class Matching {

    private static boolean debug = true;

    /** **************************************************************
     * Create Similarity Flooding input model from DGraph
     */
    static Model createModelFromGraph(DGraph graph, ArrayList<Resource> NodeResources) throws Exception {

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
     */
    static void computeNodeSimilarity(DGraph dgraph1, DGraph dgraph2) throws Exception {

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

        Match sf = new Match();

        // Two lines below are used to get the same setting as in the example of the ICDE'02 paper.
        // (In general, this formula won't converge! So better stick to the default values instead)
        sf.formula = com.interdataworking.mm.alg.Match.FORMULA_FFT;
        sf.FLOW_GRAPH_TYPE = com.interdataworking.mm.alg.Match.FG_PRODUCT;

        MapPair[] result = sf.getMatch(A, B, initMap);
        MapPair.sort(result);

        // Answer Extraction
        String answer = extractAnswer(dgraph1, result);
        System.out.println("Answer = " + answer);
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
}
