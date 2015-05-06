package ipgraph.datastructure;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class DGraph extends SimpleGraph<DNode, DefaultEdge> {

    public DGraph(Class<? extends DefaultEdge> aClass) {
        super(aClass);
    }

    public DGraph(Class<? extends DefaultEdge> aClass,
                  Set<DNode> vertexSubset, Set<DefaultEdge> edgeSubset) {

        super(aClass);

        for (DNode v : vertexSubset)
            this.addVertex(v);

        for (DefaultEdge e : edgeSubset)
            this.addEdge(this.getEdgeSource(e), this.getEdgeTarget(e));
    }

    /** **************************************************************
     * Build an undirected graph from dependency tree
     */
    public static DGraph buildDGraph(DTree dtree) {

        DGraph dgraph = new DGraph(DefaultEdge.class);

        // Initialize vertex set
        Set<Integer> processed = new HashSet<>();
        for (DNode n : dtree) {
            if (!processed.contains(n.getId())
                    && !n.getDepLabel().equals("erased")) {
                dgraph.addVertex(n);
                processed.add(n.getId());
            }
        }

        // Initialize edge set
        for (DNode n : dtree) {
            List<DNode> children = n.getChildren();
            for (DNode c : children) {
                if (!c.getDepLabel().equals("erased")) {
                    dgraph.addEdge(n, c);
                }
            }
        }

        return dgraph;
    }

    /** **************************************************************
     * Print Vertex and Edge set
     */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        Set<DefaultEdge> edges = this.edgeSet();
        for (DefaultEdge edge : edges) {
            DNode node = this.getEdgeTarget(edge);

            builder.append(node.getId()).append("\t\t");
            builder.append(node.getForm()).append("\t\t");
            builder.append(node.getLemma()).append("\t\t");
            builder.append(node.getPOS()).append("\t\t");
            builder.append(node.getHead().getId()).append("\t\t");
            builder.append(node.getLevel()).append("\t\t");
            builder.append(node.getDepLabel());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    public static String toString(Subgraph subgraph) {

        StringBuilder builder = new StringBuilder();
        Set<DefaultEdge> edges = subgraph.edgeSet();
        for (DefaultEdge edge : edges) {
            DNode node = (DNode) subgraph.getEdgeTarget(edge);

            builder.append(node.getId()).append("\t\t");
            builder.append(node.getForm()).append("\t\t");
            builder.append(node.getLemma()).append("\t\t");
            builder.append(node.getPOS()).append("\t\t");
            builder.append(node.getHead().getId()).append("\t\t");
            builder.append(node.getLevel()).append("\t\t");
            builder.append(node.getDepLabel());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /** **************************************************************
     * Find the shortest path from "from" to "to"
     */
    public List findShortestPath(DNode from, DNode to) {

        List path = new ArrayList();
        if (!this.vertexSet().contains(from)) {
            System.out.println("ERROR: " + from + " is not in the graph!");
            return path;
        } else if (!this.vertexSet().contains(to)) {
            System.out.println("ERROR: " + to + " is not in the graph!");
            return path;
        }

        path = DijkstraShortestPath.findPathBetween(this, from, to);
        if (path == null)
            path = new ArrayList<>();
        return path;
    }

//    /** **************************************************************
//     * Return the subtree/subgraph containing all nodes in containNodes
//     */
//    public Subgraph getSubgraph(UndirectedGraph<DNode, DefaultEdge> dgraph, Set<Integer> containNodes) {
//
//        Set edgeSubset = new HashSet<>();
//        Set vertexSubset = new HashSet<>();
//        for (int nid : containNodes) {
//            List<DefaultEdge> path = findShortestPath(getNodeById(0), nid);
//            edgeSubset.addAll(path);
//            for (DefaultEdge p : path) {
//                DNode sn = dgraph.getEdgeSource(p);
//                DNode tn = dgraph.getEdgeTarget(p);
//                vertexSubset.add(sn);
//                vertexSubset.add(tn);
//            }
//        }
//        Subgraph subgraph = new Subgraph(dgraph, vertexSubset, edgeSubset);
//        return subgraph;
//    }

    /** **************************************************************
     * Build an undirected graph from dtree, where all nodes whose POS
     * tag is NN, NNS, NNP, NNPS
     */
    public DGraph getSubgraph(Set<String> posTags) {

        DNode root = this.getNodeById(0);
        Set edgeSubset = new HashSet<>();
        Set vertexSubset = new HashSet<>();
        for (DNode vn : this.vertexSet()) {
            if (posTags.contains(vn.getPOS())) {
                vertexSubset.add(vn);
                List<DefaultEdge> path = findShortestPath(root, vn);
                edgeSubset.addAll(path);
                for (DefaultEdge p : path) {
                    DNode sn = this.getEdgeSource(p);
                    DNode tn = this.getEdgeTarget(p);
                    vertexSubset.add(sn);
                    vertexSubset.add(tn);
                }
            }
        }
        Subgraph subgraph = new Subgraph(this, vertexSubset, edgeSubset);
        DGraph dsubgraph = new DGraph(DefaultEdge.class, vertexSubset, edgeSubset);
        return dsubgraph;
    }

    /** **************************************************************
     * In graph, return the node whose id is equal to targetId
     */
    public DNode getNodeById(int targetID) {

        Set<DNode> vertexSet = this.vertexSet();
        for (DNode n : vertexSet) {
            if (targetID == n.getId())
                return n;
        }
        return null;
    }

    /** **************************************************************
     * In graph, return all nodes with id = targetLevel
     */
    public Set<DNode> getNodesByLevel(int targetLevel) {

        Set<DNode> DNodeWithSpecificLevel = new HashSet<>();
        Set<DNode> vertexSet = this.vertexSet();
        for (DNode n : vertexSet) {
            if (targetLevel == n.getLevel())
                DNodeWithSpecificLevel.add(n);
        }
        return DNodeWithSpecificLevel;
    }

    /** **************************************************************
     * Compute and add DNode.level (distance from each node to root;
     * For example:
     *   if ROOT-0 --> transported-7, then level_of_transported-7 = 1;
     * Return the maximum level in the graph/tree;
     */
    public int addNodeLevel() {

        int maxium = 0;
        DNode root = getNodeById(0);
        for (DNode n : this.vertexSet()) {
            List<DefaultEdge> path = findShortestPath(root, n);
            n.setLevel(path.size());
            if (path.size() > maxium)
                maxium = path.size();
        }
        return maxium;
    }
}
