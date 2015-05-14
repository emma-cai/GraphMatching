package ipgraph.matching;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import ipgraph.datastructure.DNode;
import org.apache.commons.math.stat.StatUtils;

import java.util.Collections;
import java.util.Vector;

/**
 * Created by qingqingcai on 5/14/15.
 */
public class VertexCost implements NodeComparer {

    private static boolean debug = true;

    private static ILexicalDatabase db = new NictWordNet();

    private static RelatednessCalculator[] rcs = {
            /**new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
            new Resnik(db), new JiangConrath(db), new Lin(db), **/new Path(db)
    };

    public double getNodeCost(DNode dnode_T, DNode dnode_H) {

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

        return VertexSub;
    }

    /** **************************************************************
     * Implement  method getNodeSimilarity in NodeComparer: Calculcate
     * a value from 0 to 1 inclusive that indicates how similar the
     * two nodes are.
     */
    @Override
    public double getNodeSimilarity(DNode dnode_T, DNode dnode_H) {

        double nodeMatchingCost = getNodeSimilarity(dnode_T, dnode_H);
        double nodeSimilarity = 1 - nodeMatchingCost;
        return nodeSimilarity;
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

    /**
     *
     */
    private static double doWS4JMatch(DNode dnode_T, DNode dnode_H) {
        double[] ws4jSimilarities = computeWS4JSimilarity(dnode_T, dnode_H);
        double average = StatUtils.sum(ws4jSimilarities) / (ws4jSimilarities.length * 1.0);
        if (Double.compare(average, 0.5) > 0)
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

    private static double[] computeWS4JSimilarity(DNode dNode_T, DNode dNode_H) {

        double[] ws4jSimilarities = new double[rcs.length];
        WS4JConfiguration.getInstance().setMFS(true);

        int index = 0;
        for ( RelatednessCalculator rc : rcs ) {
            double s = rc.calcRelatednessOfWords(dNode_T.getForm(), dNode_H.getForm());
            ws4jSimilarities[index++] = s;
        }

        System.out.println(dNode_T.getForm() + "\t" + dNode_H.getForm() + "\t");
        for (double v : ws4jSimilarities)
            System.out.print(v + ", ");
        System.out.println();
        return ws4jSimilarities;
    }
}
