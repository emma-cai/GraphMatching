package ipgraph.datastructure;

import com.interdataworking.mm.alg.NodeComparer;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.w3c.rdf.model.*;

import java.util.*;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class DGraph extends SimpleGraph<DNode, DefaultEdge> implements Model {

    public static final Set<String> postagSet = NodeComparer.postagSet;

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

    /**
     * Set a base URI for the model.
     *
     * @param uri
     */
    @Override
    public void setSourceURI(String uri) throws ModelException {

    }

    /**
     * Returns current base URI setting.
     */
    @Override
    public String getSourceURI() throws ModelException {
        return null;
    }

    /**
     * Number of triples in the model
     *
     * @return number of triples, -1 if unknown
     * @seeAlso org.w3c.rdf.model.VirtualModel
     */
    @Override
    public int size() throws ModelException {
        return 0;
    }

    /**
     * true if the model contains no triples
     */
    @Override
    public boolean isEmpty() throws ModelException {
        return false;
    }

    /**
     * Enumerate triples
     */
    @Override
    public Enumeration elements() throws ModelException {
        return null;
    }

    /**
     * Tests if the model contains the given triple.
     *
     * @param t
     * @return <code>true</code> if the triple belongs to the model;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean contains(Statement t) throws ModelException {
        return false;
    }

    /**
     * Adds a new triple to the model.
     *
     * @param t
     */
    @Override
    public void add(Statement t) throws ModelException {

    }

    /**
     * Removes the triple from the model.
     *
     * @param t
     */
    @Override
    public void remove(Statement t) throws ModelException {

    }

    /**
     * True if the model supports <tt>add()</tt> and <tt>remove()</tt> methods.
     * A model may change behavior of this function over time.
     */
    @Override
    public boolean isMutable() throws ModelException {
        return false;
    }

    /**
     * General method to search for triples.
     * <code>null</code> input for any parameter will match anything.
     * <p>Example: <code>Model result = m.find( null, RDF.type, m.getNodeFactory().createResource("http://...#MyClass") )</code>
     * <p>finds all instances of the class <code>MyClass</code>
     *
     * @param subject
     * @param predicate
     * @param object
     */
    @Override
    public Model find(Resource subject, Resource predicate, RDFNode object) throws ModelException {
        return null;
    }

    /**
     * Clone the model.
     */
    @Override
    public Model duplicate() throws ModelException {
        return null;
    }

    /**
     * Creates empty model of the same Class
     */
    @Override
    public Model create() throws ModelException {
        return null;
    }

    /**
     * Returns the node factory for this model
     */
    @Override
    public NodeFactory getNodeFactory() throws ModelException {
        return null;
    }

    /**
     * Returns a set of all the nodes/resources used in this model.
     *
     * @return
     */
    @Override
    public Set<Object> getNodeResources() {
        return null;
    }

    /**
     * Returns the URI of the resource. Triples and models must implement this method in a standard way.
     *
     * @return the URI of the resource
     * @see org.w3c.rdf.model.Statement
     * @see org.w3c.rdf.model.Model
     */
    @Override
    public String getURI() throws ModelException {
        return null;
    }

    /**
     * Returns the namespace of the resource. May return null.
     *
     * @return the namespace of the resource
     * @see org.w3c.rdf.model.Statement
     * @see org.w3c.rdf.model.Model
     * @since 2000-10-21
     */
    @Override
    public String getNamespace() throws ModelException {
        return null;
    }

    /**
     * Returns the local name of the resource. May not return null.
     *
     * @return the local name of the resource
     * @see org.w3c.rdf.model.Statement
     * @see org.w3c.rdf.model.Model
     * @since 2000-10-21
     */
    @Override
    public String getLocalName() throws ModelException {
        return null;
    }

    /**
     * The formal string label of the node.
     * URI in case of a resource, string in case of a literal.
     */
    @Override
    public String getLabel() throws ModelException {
        return null;
    }
}
