package ipgraph.matching;

import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;

import java.util.Set;

/**
 * Created by qingqingcai on 5/4/15.
 */
public class Matching {

    private static boolean debug = true;

    /** **************************************************************
     * Compute similarity cost between two graphs. Higher cost indicates
     * lower similarity.
     */
    public static double GraphSimilarityCost(DGraph TGraph, DGraph HGraph) {

        double COST = 0.0;

        int TMaxLevel = TGraph.addNodeLevel();
        int HMaxLevel = HGraph.addNodeLevel();
        int MaxLevel = Math.min(TMaxLevel, HMaxLevel);

        for (int level = 1; level <= MaxLevel; level++) {
            Set<DNode> TDNodesInLevel = TGraph.getNodesByLevel(level);
            Set<DNode> HDNodesInLevel = HGraph.getNodesByLevel(level);
            for (DNode TDNode : TDNodesInLevel) {
                for (DNode HDNode : HDNodesInLevel) {
                    if (debug) System.out.println(TDNode.getForm() + "\t" + HDNode.getForm() + "\t");
                    double cost = 0.0;
                    if (TDNode.getDepLabel().equals(HDNode.getDepLabel())) {
                        cost = DNodeSimilarity(TDNode, HDNode);
                        COST += cost;
                        if (debug) System.out.println("\t" + cost + "\t" + COST);
                        break;
                    } else {
                        cost = 0.3;
                        COST += cost;
                    }

                    if (debug) System.out.println("\t" + cost + "\t" + COST);
                }
            }
        }

        return COST;
    }

    /** **************************************************************
     * Compute similarity cost between two nodes;
     */
    public static double DNodeSimilarity(DNode n1, DNode n2) {

        if (n1.getLemma().equals(n2.getLemma()))
            return 0.0;
        return 1.0;
    }
}
