package ipgraph.matching;

import ipgraph.datastructure.DNode;

/**
 * Created by qingqingcai on 5/14/15.
 */
public class VertexCost implements NodeComparer {

    private static boolean debug = false;

    private static int vertexFeaNum = 3;

    /** **************************************************************
     * Implement  method getNodeSimilarity in NodeComparer: Calculcate
     * a value from 0 to 1 inclusive that indicates how similar the
     * two nodes are.
     */
    @Override
    public double getNodeSimilarity(DNode dnode_T, DNode dnode_H) {

        double[] fie_vector = new double[vertexFeaNum];
        double exactMatch_tag = isExactMatch(dnode_T, dnode_H);
        double lemmaMatch_tag = isLemmaMatch(dnode_T, dnode_H);
        double posMatch_tag = isPOSMatch(dnode_T, dnode_H);

        double[] fie_weight_vector = {0.4, 0.4, 0.2};

        fie_vector[0] = exactMatch_tag;
        fie_vector[1] = lemmaMatch_tag;
        fie_vector[2] = posMatch_tag;

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
     * If v and M(v) are identical words; return 0 (cost value) if they
     * are, otherwise return 1;
     */
    private static double isExactMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getForm().equals(dnode_H.getForm()))
            return 0.0;             // if exactly matched, the cost is 0
        else
            return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same lemma; return 0 (cost value) if they
     * match, otherwise return 1;
     */
    private static double isLemmaMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getLemma().equals(dnode_H.getLemma()))
            return 0.0;
        else
            return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same part of speech; return 0 (cost value)
     * if they match, otherwise return 1;
     */
    private static double isPOSMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getPOS().equals(dnode_H.getPOS()))
            return 0.0;             // if the pos tags are matched, the cost is 0
        else
            return 1.0;
    }

    /** **************************************************************
     * Compute f = (exp(weightVector * valueVector)) / (1.0 + exp(weightVector * valueVector))
     * @param weightVector Weight vector
     * @param feaVector Feature vector
     * @return
     */
    private static double computeExpFun(double[] weightVector, double[] feaVector) {

        if (weightVector.length != feaVector.length) {
            System.err.println("Feature dimension and weight dimenstion are NOT identical!");
            System.exit(-1);
        }

        double tmp = 0.0;
        for (int i = 0; i < feaVector.length; i++) {
            tmp += (feaVector[i] * weightVector[i]);
        }

        return Math.exp(tmp) / (1 + Math.exp(tmp));
    }
}
