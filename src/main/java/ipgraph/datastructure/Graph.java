package ipgraph.datastructure;

import com.interdataworking.mm.alg.NodeComparer;
import ipgraph.matching.similarityflooding.Edge;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.w3c.rdf.model.*;

import java.util.*;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class Graph extends SimpleGraph<Object, DefaultWeightedEdge> implements Model {
    // JERRY: SimpleGraph is an UNdirected graph. Is this a problem?

    public static final Set<String> postagSet = NodeComparer.postagSet;

    public Graph(Class<? extends DefaultWeightedEdge> aClass) {
        super(aClass);
    }

    public Graph(Class<? extends DefaultWeightedEdge> aClass,
                 Set<DNode> vertexSubset, Set<DefaultWeightedEdge> edgeSubset) {

        super(aClass);

        for (DNode v : vertexSubset)
            this.addVertex(v);

        for (DefaultWeightedEdge e : edgeSubset)
            this.addEdge(this.getEdgeSource(e), this.getEdgeTarget(e));
    }


    /**
     * Build a Graph from a set of edges.
     * @param edges
     * @return
     */
    public static Graph buildGraph(Set<Edge> edges) {
        Graph graph = new Graph(DefaultWeightedEdge.class);

        // Initialize DefaultWeightedEdge set
        for (Edge e : edges) {
            Object source = e.source;
            Object target = e.target;
            graph.addVertex(source);
            graph.addVertex(target);
            graph.addEdge(source, target);
        }

        return graph;
    }

    /** **************************************************************
     * Build an undirected graph from dependency tree
     */
    public static Graph buildDGraph(DTree dtree) {

        Graph dgraph = new Graph(DefaultWeightedEdge.class);

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
        Set<DefaultWeightedEdge> edges = this.edgeSet();
        for (DefaultWeightedEdge edge : edges) {
            Object obj = this.getEdgeTarget(edge);

            if(obj instanceof DNode) {
                DNode node = (DNode) obj;
                builder.append(node.getId()).append("\t\t");
                builder.append(node.getForm()).append("\t\t");
                builder.append(node.getLemma()).append("\t\t");
                builder.append(node.getPOS()).append("\t\t");
                builder.append(node.getHead().getId()).append("\t\t");
                builder.append(node.getLevel()).append("\t\t");
                builder.append(node.getDepLabel());
                builder.append(System.lineSeparator());
            }
            else    {
                obj.toString();
            }
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
    public List findShortestPath(DNode from, Object to) {

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
    public Graph getSubgraph(Set<String> posTags) {

        DNode root = this.getNodeById(0);
        Set edgeSubset = new HashSet<>();
        Set vertexSubset = new HashSet<>();
        for (Object vn : this.vertexSet()) {
            if (filterSubGraphByNodeType(vn, posTags)) {
                vertexSubset.add(vn);
                List<DefaultWeightedEdge> path = findShortestPath(root, vn);
                edgeSubset.addAll(path);
                for (DefaultWeightedEdge p : path) {
                    Object sn = this.getEdgeSource(p);
                    Object tn = this.getEdgeTarget(p);
                    vertexSubset.add(sn);
                    vertexSubset.add(tn);
                }
            }
        }
        Subgraph subgraph = new Subgraph(this, vertexSubset, edgeSubset);
        Graph dsubgraph = new Graph(DefaultWeightedEdge.class, vertexSubset, edgeSubset);
        return dsubgraph;
    }

    /**
     * Lets us allow or reject subgraphs according to the type of node object.
     * @param obj
     * @return
     */
    private boolean filterSubGraphByNodeType(Object obj, Set<String> posTags)    {
        if (obj instanceof DNode)   {
            DNode node = (DNode) obj;
            return posTags.contains(node.getPOS());
        }
        return true;
    }

    /** **************************************************************
     * In graph, return the node whose id is equal to targetId
     * FIXME: Returns null if this is not a graph of DNode objects
     */
    public DNode getNodeById(int targetID) {

        Set<Object> vertexSet = this.vertexSet();
        for (Object n : vertexSet) {
            if (! (n instanceof DNode))   {
                return null;
            }
            DNode node = (DNode) n;
            if (targetID == node.getId())
                return node;
        }
        return null;
    }

    /** **************************************************************
     * In graph, return all nodes with id = targetLevel
     * FIXME: Returns null if this is not a graph of DNode objects
     */
    public Set<DNode> getNodesByLevel(int targetLevel) {

        Set<DNode> DNodeWithSpecificLevel = new HashSet<>();
        Set<Object> vertexSet = this.vertexSet();
        for (Object n : vertexSet) {
            if (! (n instanceof DNode))   {
                return null;
            }
            DNode node = (DNode) n;
            if (targetLevel == node.getLevel())
                DNodeWithSpecificLevel.add(node);
        }
        return DNodeWithSpecificLevel;
    }

    /** **************************************************************
     * Compute and add DNode.level (distance from each node to root;
     * For example:
     *   if ROOT-0 --> transported-7, then level_of_transported-7 = 1;
     * Return the maximum level in the graph/tree;
     * FIXME: Returns Integer.MIN_VALUE if this is not a graph of DNode objects
     */
    public int addNodeLevel() {

        int maxium = 0;

        Object obj = getNodeById(0);
        if (! (obj instanceof DNode))   {
            return Integer.MIN_VALUE;
        }


        DNode root = (DNode) obj;
        for (Object n : this.vertexSet()) {
            DNode node = (DNode) n;
            List<DefaultEdge> path = findShortestPath(root, node);
            node.setLevel(path.size());
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
        Graph dgraph = Graph.buildDGraph(dtree);
        System.out.println("\ndgraph = \n" + dgraph.toString());

        // compute and add nodes' levels
        int maxiumlevel = dgraph.addNodeLevel();
        System.out.println("\ndgraph (add levels to nodes) <maximumlevel = " + maxiumlevel + "> = \n" + dgraph.toString());

        // find the path between two nodes
        int sid = 1; int tid = 6;
        DNode sn = dtree.getNodeById(1);
        DNode tn = dtree.getNodeById(6);
        List<DefaultWeightedEdge> paths = dgraph.findShortestPath(sn, tn);
        printPath(dgraph, paths, sn, tn);

        // build subgraph
        Graph dsubgraph_NOUN = dgraph.getSubgraph(postagSet);
        System.out.println("\nsubgraph_for_NOUN = \n" + dsubgraph_NOUN.toString());
    }

    public static void printPath(Graph dgraph,
                                 List<DefaultWeightedEdge> paths, DNode sn, DNode tn) {
        System.out.println("\nshortest path from " + sn.getId() + " to " + tn.getId() + " = ");
        for (int i = 0; i < paths.size(); i++) {
            DefaultWeightedEdge p = paths.get(i);
            Object s = dgraph.getEdgeSource(p);
            Object t = dgraph.getEdgeTarget(p);
            if (s instanceof DNode && t instanceof DNode) {
                DNode u = (DNode) s;
                DNode v = (DNode) t;
                System.out.println(u.getId() + ":" + u.getForm() + " --> " + v.getDepLabel() + " --> " + v.getId() + ":" + v.getForm());
            }
            else    {
                System.out.println(s.toString() + " --> " + p.toString() + " --> " + t.toString());
            }
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
