package ipgraph.matching;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.apache.commons.math.stat.StatUtils;

import java.util.*;

/**
 * Created by qingqingcai on 5/14/15.
 */
public class VertexSub implements NodeComparer {

    private static boolean debug = false;

    private static double ws4jThreshold = 0.5;  // threshold for WordNet similarity

    private static ILexicalDatabase db = new NictWordNet();

    private static RelatednessCalculator[] rcs = {
    //        new HirstStOnge(db),
    //        new LeacockChodorow(db),
    //        new Lesk(db),
    //        new WuPalmer(db),
    //        new Resnik(db),
    //        new JiangConrath(db),
    //        new Lin(db),
            new Path(db)
    };

    /** **************************************************************
     * Implement method getNodeSimilarity in NodeComparer: Calculcate
     * a value from 0 to 1 inclusive that indicates how similar the
     * two nodes are.
     * Version1: nodeSimilarity = 1 - nodeMatchingCost;
     */
    @Override
    public double getNodeSimilarity(DNode dnode_T, DNode dnode_H) {

        double nodeMatchingCost = getVertexSub(dnode_T, dnode_H);
        double nodeSimilarity = 1 - nodeMatchingCost;
        return nodeSimilarity;
    }

    /** **************************************************************
     * Compute VertexCost
     */
    public double getVertexSub(DNode dnode_T, DNode dnode_H) {

        Vector<Double> fie_vector = new Vector<>();
        double exactMatch_tag = doExactMatch(dnode_T, dnode_H);
        double lemmaMatch_tag = doLemmaMatch(dnode_T, dnode_H);
        double posMatch_tag = doPOSMatch(dnode_T, dnode_H);
        double ws4jMatch_tag = doWS4JMatch(dnode_T, dnode_H);

        fie_vector.add(exactMatch_tag);
        fie_vector.add(lemmaMatch_tag);
        fie_vector.add(posMatch_tag);
        fie_vector.add(ws4jMatch_tag);

        Double[] weights = {0.3, 0.2, 0.2, 0.3};
        Vector<Double> fie_weight_vector = new Vector();
        Collections.addAll(fie_weight_vector, weights);
//        Vector<Double> fie_weight_vector = new Vector<>(fie_vector.size());
//        for (int i = 0; i < fie_vector.size(); i++)
//            fie_weight_vector.add(1.0/fie_vector.size());

        double VertexSub = computeExpFun(fie_weight_vector, fie_vector);

        if (debug) {
            System.out.println("\n----------------------------------------");
            System.out.println("dnode_H = " + dnode_H);
            System.out.println("dnode_T = " + dnode_T);
            System.out.print("fie_vector = ");
            for (double v : fie_vector) System.out.print(" \t" + v);
            System.out.println();
            System.out.print("fie_weight_vector = ");
            for (double v : fie_weight_vector) System.out.print(" \t" + v);
            System.out.println();
            System.out.println("VertexSub = " + VertexSub);
            System.out.println("----------------------------------------");
        }

//        System.out.println("\n----------------------------------------");
//        System.out.println("dnode_H = " + dnode_H);
//        System.out.println("dnode_T = " + dnode_T);
//        System.out.println("Vertex cost = " + VertexSub);
//        System.out.println("----------------------------------------");

        return VertexSub;
    }

    /** **************************************************************
     * If v and M(v) are identical words; return 0 (cost value) if they
     * are, otherwise return 1;
     */
    private static double doExactMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getForm().equals(dnode_H.getForm()))
            return 0.0;             // if exactly matched, the cost is 0
        else
            return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same lemma; return 0 (cost value) if they
     * match, otherwise return 1;
     */
    private static double doLemmaMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getLemma().equals(dnode_H.getLemma()))
            return 0.0;
        else
            return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same part of speech; return 0 (cost value)
     * if they match, otherwise return 1;
     */
    private static double doPOSMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getPOS().equals(dnode_H.getPOS()))
            return 0.0;             // if the pos tags are matched, the cost is 0
        else
            return 1.0;
    }

    /** **************************************************************
     * If WordNet similarity > ws4jThreshold, WS4JCost = 0;
     * Otherwise, WS4JCost = 1.0;
     */
    private static double doWS4JMatch(DNode dnode_T, DNode dnode_H) {

        double[] ws4jSimilarities = computeWS4JSimilarity(dnode_T, dnode_H);
        double average = StatUtils.sum(ws4jSimilarities) / (ws4jSimilarities.length * 1.0);
        if (Double.compare(average, ws4jThreshold) > 0)
            return 0.0;
        else
            return 1.0;
    }

    /** **************************************************************
     * Compute f = (exp(weightVector * valueVector)) / (1.0 + exp(weightVector * valueVector))
     * @param weightVector Weight vector
     * @param feaVector Feature vector
     * @return
     */
    private static double computeExpFun(Vector<Double> weightVector, Vector<Double> feaVector) {

        if (weightVector.size() != feaVector.size()) {
            System.err.println("Feature dimension and weight dimenstion are NOT identical!");
            System.err.println("Feature = " + feaVector);
            System.err.println("FeatureWeight = " + weightVector);
            System.exit(-1);
        }

        double tmp = 0.0;
        for (int i = 0; i < feaVector.size(); i++) {
            tmp += (feaVector.get(i) * weightVector.get(i));
        }

        return Math.exp(tmp) / (1 + Math.exp(tmp));
    }

    /** **************************************************************
     * Compute WordNet similarity for two words by using cmu-ws4j
     */
    private static double[] computeWS4JSimilarity(DNode dNode_T, DNode dNode_H) {

        double[] ws4jSimilarities = new double[rcs.length];
        WS4JConfiguration.getInstance().setMFS(true);

        int index = 0;
        for ( RelatednessCalculator rc : rcs ) {
            double s = rc.calcRelatednessOfWords(dNode_T.getForm(), dNode_H.getForm());
            ws4jSimilarities[index++] = s;
        }
        return ws4jSimilarities;
    }

    /** **************************************************************
     * In VertexSub computation, exclude nodes if:
     * (1) the node is the root, which is labeled as "^";
     * (2) the cPOSTag of the node is "PUNCT", "DET;
     */
    public static boolean excludeNodes(DNode node) {

        // A list of forms/word_strings which will not be considered in VertexSub
        Set<String> excludeFormList = new HashSet<>(Arrays.asList("^"));
        if (excludeFormList.contains(node.getForm()))
            return true;

        // A list of pos-tags which will not be considered in VertexSub
        Set<String> excludePOSList = new HashSet<>(Arrays.asList("PUNCT", "DET", "ADP"));
        if (excludePOSList.contains(node.getcPOSTag()))
            return true;

        return false;
    }

    /**
     * TODO: compute the importance weight dnode_H in our algorithm
     * @param dnode
     * @return
     */
    public static double Importance(DNode dnode) {
        return 1.0;
    }


    public static void main(String[] args) {

        String T1 = "Many researchers including navigator and aeronautical engineer Elgen Long believe that the Electra ran out of fuel and that Earhart and Noonan ditched at sea.";
        DTree dtree_T1 = DTree.buildTree(T1);
        DGraph dgraph_T1 = DGraph.buildDGraph(dtree_T1);
        System.out.println("\nsubgraph_T1 = \n" + dgraph_T1.toString());

        String T2 = "The \"crash and sink\" theory is often the most widely accepted explanation of Earhart’s and Noonan’s fate.";
        DTree dtree_T2 = DTree.buildTree(T2);
        DGraph dgraph_T2 = DGraph.buildDGraph(dtree_T2);
        System.out.println("\nsubgraph_T2 = \n" + dgraph_T2.toString());

        String Q1 = "Who believes that Earhart and Noonan ditched at sea?";
        DTree dtree_Q1 = DTree.buildTree(Q1);
        DGraph dgraph_Q1 = DGraph.buildDGraph(dtree_Q1);
        System.out.println("\nsubgraph_Q1 = \n" + dgraph_Q1.toString());


        VertexSub vs = new VertexSub();
        for (DNode dnode_H : dgraph_Q1.vertexSet()) {
            if (excludeNodes(dnode_H))
                continue;
            for (DNode dnode_T : dgraph_T1.vertexSet()) {
                if (excludeNodes(dnode_T))
                    continue;
                double nodeSimilarity = vs.getNodeSimilarity(dnode_T, dnode_H);
                System.out.println("\n------------------------------------------");
                System.out.println(dnode_H + "\n"
                        + dnode_T + "\n"
                        + nodeSimilarity);
                System.out.println("------------------------------------------");
            }
        }
    }
}
