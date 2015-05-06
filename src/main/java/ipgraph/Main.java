package ipgraph;

import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class Main {

    private static Set<String> postagSet = new HashSet<>(Arrays.asList(new String[]{"NN", "NNS", "NNP", "NNPS", "WP"}));

    public static void main(String[] args) {

        String sentence = "What are transported into the RER during synthesis?";

        // build tree
        DTree dtree = DTree.buildTree(sentence);
        System.out.println("\ndtree = \n" + dtree.toString());

        // build graph
        DGraph dgraph = DGraph.buildDGraph(dtree);
        System.out.println("\ndgraph = \n" + dgraph.toString());

        // compute and add nodes' levels
        int maxiumlevel = dgraph.addNodeLevel();
        System.out.println("\ndgraph (add levels to nodes) <maximumlevel = " + maxiumlevel + "> = \n" + dgraph.toString());

        // find the path between two nodes
        int sid = 1; int tid = 6;
        DNode sn = dtree.getNodeById(1);
        DNode tn = dtree.getNodeById(6);
        List<DefaultEdge> paths = dgraph.findShortestPath(sn, tn);
        printPath(dgraph, paths, sn, tn);

        // build subgraph
        DGraph dsubgraph_NOUN = dgraph.getSubgraph(postagSet);
        System.out.println("\nsubgraph_for_NOUN = \n" + dsubgraph_NOUN.toString());
    }

    public static void printPath(UndirectedGraph<DNode, DefaultEdge> dgraph,
           List<DefaultEdge> paths, DNode sn, DNode tn) {
        System.out.println("\nshortest path from " + sn.getId() + " to " + tn.getId() + " = ");
        for (int i = 0; i < paths.size(); i++) {
            DefaultEdge p = paths.get(i);
            DNode s = dgraph.getEdgeSource(p);
            DNode t = dgraph.getEdgeTarget(p);
            System.out.println(s.getId() + ":" + s.getForm() + " --> " + t.getDepLabel() + " --> " + t.getId() + ":" + t.getForm());
        }
    }

    private static void printSplitLine() {
        System.out.println("===============================================================");
    }
}
