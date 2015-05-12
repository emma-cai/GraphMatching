package com.interdataworking.mm.alg;

import org.junit.Test;
import org.w3c.rdf.model.Model;
import org.w3c.rdf.model.ModelException;
import org.w3c.rdf.model.NodeFactory;
import org.w3c.rdf.model.Resource;
import org.w3c.rdf.util.RDFFactory;
import org.w3c.rdf.util.RDFFactoryImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class DSimplifiedMatchTest {

    private PGNode makePGNode(Resource r1, Resource r2, double d1, double d2, double d3, double d4) {
        //CompareGraphs match = new CompareGraphs();
        PGNode pgNode = new PGNode(r1, r2);

        pgNode.sim = d1;
        pgNode.sim0 = d2;
        pgNode.simN = d3;
        pgNode.simN1 = d4;

        return pgNode;
    }

    /**
     *
     * @param r1
     * @param r2
     * @param s
     *  has the form, e.g. "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318"
     * @return
     */
    private PGNode makePGNode(Resource r1, Resource r2, String s) {
        String[] commaSplit = s.split(",");

        String field = commaSplit[0];
        String[] equalSplit = field.split("=");
        String simVal = equalSplit[1];

        field = commaSplit[1];
        equalSplit = field.split("=");
        String initVal = equalSplit[1];

        field = commaSplit[2];
        equalSplit = field.split("=");
        String nVal = equalSplit[1];

        field = commaSplit[3];
        equalSplit = field.split("=");
        String n1Val = equalSplit[1];

        return makePGNode(r1, r2, Double.parseDouble(simVal), Double.parseDouble(initVal), Double.parseDouble(nVal), Double.parseDouble(n1Val));
    }

    private PGNode makePGNode(String s1, String s2, double d1, double d2, double d3, double d4) throws ModelException {

        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();
        Resource a = nf.createResource(s1);
        Resource b = nf.createResource(s2);

        return makePGNode(a, b, d1, d2, d3, d4);
    }



    /**
     * This test verifies ICDE02Example().
     * @throws org.w3c.rdf.model.ModelException
     */
    @Test
    public void testICDE02Example() throws ModelException {
        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        // Create two tiny sample graphs used in the ICDE'02 paper.

        // create graph/model A
        Model A = rf.createModel();

        Resource a = nf.createResource("a");
        Resource a1 = nf.createResource("a1");
        Resource a2 = nf.createResource("a2");
        Resource l1 = nf.createResource("l1");
        Resource l2 = nf.createResource("l2");

        A.add(nf.createStatement(a, l1, a1));
        A.add(nf.createStatement(a, l1, a2));
        A.add(nf.createStatement(a1, l2, a2));

        // create graph/model B
        Model B = rf.createModel();

        Resource b = nf.createResource("b");
        Resource b1 = nf.createResource("b1");
        Resource b2 = nf.createResource("b2");

        B.add(nf.createStatement(b, l1, b1));
        B.add(nf.createStatement(b, l2, b2));
        B.add(nf.createStatement(b2, l2, b1));

        // create an initial mapping which is just a cross-product with 1's as weights
        List initMap = new ArrayList();

        Iterator itA = A.getNodeResources().iterator();
        while(itA.hasNext())     {
            Resource resA = (Resource) itA.next();

            Iterator itB = B.getNodeResources().iterator();
            while(itB.hasNext())    {
                Resource resB = (Resource) itB.next();
                if (resA.equals(a1) && resB.equals(b1))     {
                    initMap.add(new MapPair(resA, resB, 1.0, true));
                }
                else {
                    initMap.add(new MapPair(resA, resB, 1.0));
                }
            }
        }

        DSimplifiedMatch sf = new DSimplifiedMatch();

        // Two lines below are used to get the same setting as in the example of the ICDE'02 paper.
        // (In general, this formula won't converge! So better stick to the default values instead)
//        sf.formula = CompareGraphs.FORMULA_FFT;
//        sf.FLOW_GRAPH_TYPE = CompareGraphs.FG_PRODUCT;

        MapPair[] actualArray = sf.compareGraphNodes(A, B, initMap);
        MapPair.sort(actualArray);
        DSimplifiedMatch.dump(actualArray);

        // Now create the expected.
        PGNode a_b_MP = makePGNode(a, b, "sim=1.0, init=1.0, N=1.0, N1=1.0");
        PGNode a2_b1_MP = makePGNode(a2, b1, "sim=0.9007839487015484, init=1.0, N=0.9007784258108072, N1=0.9007839487015484");
        PGNode a1_b2_MP = makePGNode(a1, b2, "sim=0.6551331898713546, init=1.0, N=0.6551272183980411, N1=0.6551331898713546");
        PGNode a1_b_MP = makePGNode(a1, b, "sim=0.5835455461730198, init=1.0, N=0.5836725519104299, N1=0.5835455461730198");
        PGNode a2_b2_MP = makePGNode(a2, b2, "sim=0.5835455461730198, init=1.0, N=0.5836725519104299, N1=0.5835455461730198");
        PGNode a1_b1_MP = makePGNode(a1, b1, "sim=0.5269788735835386, init=1.0, N=0.5269814544466794, N1=0.5269788735835386");
        a1_b1_MP.inverse = true;
        PGNode a_b1_MP = makePGNode(a, b1, "sim=0.22584877277873247, init=1.0, N=0.22585290789379314, N1=0.22584877277873247");
        PGNode a_b2_MP = makePGNode(a, b2, "sim=0.22584877277873247, init=1.0, N=0.22585290789379314, N1=0.22584877277873247");
        PGNode a2_b_MP = makePGNode(a2, b, "sim=0.22584877277873247, init=1.0, N=0.22585290789379314, N1=0.22584877277873247");

        List expectedList = new ArrayList();
        expectedList.add(a_b_MP);
        expectedList.add(a2_b1_MP);
        expectedList.add(a1_b2_MP);
        expectedList.add(a1_b_MP);
        expectedList.add(a2_b2_MP);
        expectedList.add(a1_b1_MP);
        expectedList.add(a_b1_MP);
        expectedList.add(a_b2_MP);
        expectedList.add(a2_b_MP);

        for (int ct = 0; ct < expectedList.size(); ct++)    {
            PGNode expected = (PGNode) expectedList.get(ct);
            PGNode actual = (PGNode) actualArray[ct];

            assertEquals("Testing element = " + ct + "; ", expected.toString(), actual.toString());

        }
    }


    /**
     * This test verifies a call to orderedNodesExample(10).
     */
    @Test
    public void testOrderedNodesExample() throws ModelException {

        int num = 10;

        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        // Create two tiny sample graphs

        Resource[] a = new Resource[num];
        Resource[] b = new Resource[num];
        Resource NEXT = nf.createResource("next");

        for(int i=0; i < num; i++) {
            a[i] = nf.createResource("a" + (i+1));
            b[i] = nf.createResource("b" + (i+1));
        }

        // create graphs A and B
        Model A = rf.createModel();
        Model B = rf.createModel();

        // connect lists in A and B
        for(int i=1; i < num; i++) {
            A.add(nf.createStatement(a[i-1], NEXT, a[i]));
            B.add(nf.createStatement(b[i-1], NEXT, b[i]));
        }

        DSimplifiedMatch sf = new DSimplifiedMatch();
        PGNode[] result = sf.compareGraphNodes(A, B, null); // initial mapping is a full cross-product of nodes in A and B
        //MapPair.printMap(new FilterBest().getFilterBest(Arrays.asList(result), true), System.out);
        List actualList = new FilterBest().getFilterBest(Arrays.asList(result), true);
        //List l = new ArrayList(c);
        Object[] actualArray = actualList.toArray();
        MapPair.sort(actualArray);
        DSimplifiedMatch.dump(actualArray);


        // Now create the expected.
        PGNode a5_b5_MP = makePGNode("a5", "b5", 1.0, 0.001, 1.0, 1.0);
        PGNode a6_b6_MP = makePGNode("a6", "b6", 1.0, 0.001, 1.0, 1.0);
        PGNode a4_b4_MP = makePGNode("a4", "b4", 0.9196714699636644, 0.001, 0.9196811395357198, 0.9196714699636644);
        PGNode a7_b7_MP = makePGNode("a7", "b7", 0.9196714699636644, 0.001, 0.9196811395357198, 0.9196714699636644);
        PGNode a3_b3_MP = makePGNode("a3", "b3", 0.7652037714138048, 0.001, 0.7652253202961736, 0.7652037714138048);
        PGNode a8_b8_MP = makePGNode("a8", "b8", 0.7652037714138048, 0.001, 0.7652253202961736, 0.7652037714138048);
        PGNode a2_b2_MP = makePGNode("a2", "b2", 0.5485142368307199, 0.001, 0.5485401707808361, 0.5485142368307199);
        PGNode a9_b9_MP = makePGNode("a9", "b9", 0.5485142368307199, 0.001, 0.5485401707808361, 0.5485142368307199);
        PGNode a1_b1_MP = makePGNode("a1", "b1", 0.2863490249192036, 0.001, 0.2863667236749642, 0.2863490249192036);
        PGNode a10_b10_MP = makePGNode("a10", "b10", 0.2863490249192036, 0.001, 0.28636672367496413, 0.2863490249192036);

        List expectedList = new ArrayList();
        expectedList.add(a5_b5_MP);
        expectedList.add(a6_b6_MP);
        expectedList.add(a4_b4_MP);
        expectedList.add(a7_b7_MP);
        expectedList.add(a3_b3_MP);
        expectedList.add(a8_b8_MP);
        expectedList.add(a2_b2_MP);
        expectedList.add(a9_b9_MP);
        expectedList.add(a1_b1_MP);
        expectedList.add(a10_b10_MP);

        for (int ct = 0; ct < expectedList.size(); ct++)    {
            PGNode expected = (PGNode) expectedList.get(ct);
            PGNode actual = (PGNode) actualArray[ct];

            assertEquals("Testing element = " + ct + "; ", expected.toString(), actual.toString());
        }
    }


    /**
     * This test verifies a call to sequenceExample("GATTACA", "GTAACATCAGAGATTTTGAGACAC").
     */
    @Test
    public void testSequenceExample() throws ModelException {

        String seq1 = "GATTACA";
        String seq2 = "GTAACATCAGAGATTTTGAGACAC";

        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        System.err.println("CompareGraphsing sequences " + seq1 + " and " + seq2);

        // create graphs A and B
        Model A = rf.createModel();
        Model B = rf.createModel();

        DSimplifiedMatch.addSequence(A, nf, seq1, "x");
        DSimplifiedMatch.addSequence(B, nf, seq2, "y");

        DSimplifiedMatch sf = new DSimplifiedMatch();
        PGNode[] result = sf.compareGraphNodes(A, B, null); // initial mapping is a full cross-product of nodes in A and B

        List pruned = new FilterBest().getFilterBest(Arrays.asList(result), true);
        Object[] actualArray = pruned.toArray();
        MapPair.sortGroup(actualArray, false);

        // Now create the expected.
        PGNode x0_y0_MP = makePGNode("x0", "y0", 1.0, 0.001, 1.0, 1.0);
        PGNode x1_y12_MP = makePGNode("x1", "y12", 0.21660899646321097, 0.001, 0.2368652622897955, 0.21660899646321097);
        PGNode x2_y13_MP = makePGNode("x2", "y13", 0.18776333576686896, 0.001, 0.20530577704461345, 0.18776333576686896);
        PGNode x3_y15_MP = makePGNode("x3", "y14", 0.10855412421515549, 0.001, 0.11868225797349849, 0.10855412421515549);

        List expectedList = new ArrayList();
        expectedList.add(x0_y0_MP);
        expectedList.add(x1_y12_MP);
        expectedList.add(x2_y13_MP);
        expectedList.add(x3_y15_MP);

        for (int ct = 0; ct < expectedList.size(); ct++)    {
            PGNode expected = (PGNode) expectedList.get(ct);
            PGNode actual = (PGNode) actualArray[ct];

            assertEquals("Testing element = " + ct + "; ", expected.toString(), actual.toString());
        }
    }

    /**
     * Try graph matching on parser output.
     * @throws org.w3c.rdf.model.ModelException
     */
    @Test
    public void testParseOutputAffirmativeAffirmative() throws ModelException {
        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        // Create the edges.
        Resource e_nn = nf.createResource("nn");
        Resource e_nsubj = nf.createResource("nsubj");
        Resource e_attr = nf.createResource("attr");
        Resource e_amod = nf.createResource("amod");
        Resource e_det = nf.createResource("det");

        // Graph/model A: New York City is a great place.
        Model A = rf.createModel();
        Resource a_new = nf.createResource("New");
        Resource a_york = nf.createResource("York");
        Resource a_city = nf.createResource("City");
        Resource a_is = nf.createResource("is");
        Resource a_a = nf.createResource("a");
        Resource a_great = nf.createResource("great");
        Resource a_place = nf.createResource("place");

        A.add(nf.createStatement(a_new, e_nn, a_york));
        A.add(nf.createStatement(a_york, e_nn, a_city));
        A.add(nf.createStatement(a_city, e_nsubj, a_is));
        A.add(nf.createStatement(a_a, e_det, a_great));
        A.add(nf.createStatement(a_great, e_amod, a_place));
        A.add(nf.createStatement(a_place, e_attr, a_is));

        // Graph/model B: Work is great.
        Model B = rf.createModel();

        Resource b_work = nf.createResource("Work");
        Resource b_is = nf.createResource("is");
        Resource b_great = nf.createResource("great");

        B.add(nf.createStatement(b_work, e_nsubj, b_is));
        B.add(nf.createStatement(b_great, e_attr, b_is));

        // create an initial mapping which is just a cross-product with 1's as weights
        List initMap = new ArrayList();

        Iterator itA = A.getNodeResources().iterator();
        while(itA.hasNext())     {
            Resource resA = (Resource) itA.next();

            Iterator itB = B.getNodeResources().iterator();
            while(itB.hasNext())    {
                Resource resB = (Resource) itB.next();
                //initMap.add(new MapPair(resA, resB, 1.0));
                if (resA.getLabel().equals(resB.getLabel()))  {
                    initMap.add(new MapPair(resA, resB, 1.0));
                }
                else    {
                    initMap.add(new MapPair(resA, resB, 0.5));
                }
            }
        }

        DSimplifiedMatch sf = new DSimplifiedMatch();

        // Two lines below are used to get the same setting as in the example of the ICDE'02 paper.
        // (In general, this formula won't converge! So better stick to the default values instead)
//        sf.formula = CompareGraphs.FORMULA_FFT;
//        sf.FLOW_GRAPH_TYPE = CompareGraphs.FG_PRODUCT;

        MapPair[] actualArray = sf.compareGraphNodes(A, B, initMap);
        MapPair.sort(actualArray);

        DSimplifiedMatch.dump(actualArray);

        // Now create the expected.
        PGNode is_is_MP = makePGNode(a_is, b_is, "sim=1.0, init=1.0, N=1.0, N1=1.0");

        PGNode city_work_MP = makePGNode(a_city, b_work, "sim=0.724744868707717, init=0.5, N=0.7247448979591836, N1=0.724744868707717");
        PGNode place_great_MP = makePGNode(a_place, b_great, "sim=0.724744868707717, init=0.5, N=0.7247448979591836, N1=0.724744868707717");
        PGNode great_great_MP = makePGNode(a_great, b_great, "sim=0.28990368077055384, init=1.0, N=0.2899234693877551, N1=0.28990368077055384");
        PGNode a_is_MP = makePGNode(a_a, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");

        PGNode new_is_MP = makePGNode(a_new, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode a_work_MP = makePGNode(a_a, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode is_work_MP = makePGNode(a_is, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode new_work_MP = makePGNode(a_new, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");

        PGNode city_is_MP = makePGNode(a_city, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode york_is_MP = makePGNode(a_york, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode york_work_MP = makePGNode(a_york, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode a_great_MP = makePGNode(a_a, b_great, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");

        PGNode is_great_MP = makePGNode(a_is, b_great, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode new_great_MP = makePGNode(a_new, b_great, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode city_great_MP = makePGNode(a_city, b_great, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode york_great_MP = makePGNode(a_york, b_great, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");

        PGNode great_is_MP = makePGNode(a_great, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode great_work_MP = makePGNode(a_great, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode place_is_MP = makePGNode(a_place, b_is, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");
        PGNode place_work_MP = makePGNode(a_place, b_work, "sim=0.14495184038527692, init=0.5, N=0.14496173469387755, N1=0.14495184038527692");

        List expectedList = new ArrayList();
        expectedList.add(is_is_MP);

        expectedList.add(city_work_MP);
        expectedList.add(place_great_MP);
        expectedList.add(great_great_MP);
        expectedList.add(a_is_MP);

        expectedList.add(new_is_MP);
        expectedList.add(a_work_MP);
        expectedList.add(is_work_MP);
        expectedList.add(new_work_MP);

        expectedList.add(city_is_MP);
        expectedList.add(york_is_MP);
        expectedList.add(york_work_MP);
        expectedList.add(a_great_MP);

        expectedList.add(is_great_MP);
        expectedList.add(new_great_MP);
        expectedList.add(city_great_MP);
        expectedList.add(york_great_MP);

        expectedList.add(great_is_MP);
        expectedList.add(great_work_MP);
        expectedList.add(place_is_MP);
        expectedList.add(place_work_MP);

        for (int ct = 0; ct < expectedList.size(); ct++)    {
            PGNode expected = (PGNode) expectedList.get(ct);
            PGNode actual = (PGNode) actualArray[ct];

            assertEquals("Testing element = " + ct + "; ", expected.toString(), actual.toString());
        }
    }



    @Test
    public void testParseOutputQuestionAffirmative() throws ModelException {
        RDFFactory rf = new RDFFactoryImpl();
        NodeFactory nf = rf.getNodeFactory();

        // Create the edges.
        Resource e_nn = nf.createResource("nn");
        Resource e_nsubj = nf.createResource("nsubj");
        Resource e_attr = nf.createResource("attr");
        Resource e_amod = nf.createResource("amod");
        Resource e_det = nf.createResource("det");

        // Graph/model A: New York City is a great place.
        Model A = rf.createModel();
        Resource a_new = nf.createResource("New");
        Resource a_york = nf.createResource("York");
        Resource a_city = nf.createResource("City");
        Resource a_is = nf.createResource("is");
        Resource a_a = nf.createResource("a");
        Resource a_great = nf.createResource("great");
        Resource a_place = nf.createResource("place");

        A.add(nf.createStatement(a_new, e_nn, a_york));
        A.add(nf.createStatement(a_york, e_nn, a_city));
        A.add(nf.createStatement(a_city, e_nsubj, a_is));
        A.add(nf.createStatement(a_a, e_det, a_great));
        A.add(nf.createStatement(a_great, e_amod, a_place));
        A.add(nf.createStatement(a_place, e_attr, a_is));

        // Graph/model B: What is a great place?
        Model B = rf.createModel();

        Resource b_what = nf.createResource("What");
        Resource b_is = nf.createResource("is");
        Resource b_a = nf.createResource("a");
        Resource b_great = nf.createResource("great");
        Resource b_place = nf.createResource("place");

        B.add(nf.createStatement(b_what, e_nsubj, b_is));
        B.add(nf.createStatement(b_a, e_det, b_great));
        B.add(nf.createStatement(b_great, e_amod, b_place));
        B.add(nf.createStatement(b_place, e_attr, b_is));

        // create an initial mapping which is just a cross-product with 1's as weights
        List initMap = new ArrayList();

        Iterator itA = A.getNodeResources().iterator();
        while(itA.hasNext())     {
            Resource resA = (Resource) itA.next();

            Iterator itB = B.getNodeResources().iterator();
            while(itB.hasNext())    {
                Resource resB = (Resource) itB.next();
                //initMap.add(new MapPair(resA, resB, 1.0));
                if (resA.getLabel().equals(resB.getLabel()))  {
                    initMap.add(new MapPair(resA, resB, 1.0));
                }
                else    {
                    initMap.add(new MapPair(resA, resB, 0.5));
                }
            }
        }

        DSimplifiedMatch sf = new DSimplifiedMatch();

        // Two lines below are used to get the same setting as in the example of the ICDE'02 paper.
        // (In general, this formula won't converge! So better stick to the default values instead)
//        sf.formula = CompareGraphs.FORMULA_FFT;
//        sf.FLOW_GRAPH_TYPE = CompareGraphs.FG_PRODUCT;

        MapPair[] actualArray = sf.compareGraphNodes(A, B, initMap);
        MapPair.sort(actualArray);

        DSimplifiedMatch.dump(actualArray);

        // Now create the expected.
        PGNode place_place_MP = makePGNode(a_place, a_place, "sim=1.0, init=1.0, N=1.0, N1=1.0");

        PGNode great_great_MP = makePGNode(a_great, b_great, "sim=0.9631842495076888, init=1.0, N=0.9632012056942844, N1=0.9631842495076888");
        PGNode is_is_MP = makePGNode(a_is, b_is, "sim=0.831399945022848, init=1.0, N=0.8313835451125151, N1=0.831399945022848");
        PGNode a_a_MP = makePGNode(a_a, b_a, "sim=0.618034409439937, init=1.0, N=0.6180515587202603, N1=0.618034409439937");
        PGNode city_what_MP = makePGNode(a_city, b_what, "sim=0.4862501049550962, init=0.5, N=0.48623389813849105, N1=0.4862501049550962");

        PGNode a_is_MP = makePGNode(a_a, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode is_a_MP = makePGNode(a_is, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode new_a_MP = makePGNode(a_new, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode new_is_MP = makePGNode(a_new, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode a_what_MP = makePGNode(a_a, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode is_what_MP = makePGNode(a_is, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode new_what_MP = makePGNode(a_new, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode city_a_MP = makePGNode(a_city, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode city_is_MP = makePGNode(a_city, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode york_a_MP = makePGNode(a_york, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode york_is_MP = makePGNode(a_york, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode york_what_MP = makePGNode(a_york, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode a_great_MP = makePGNode(a_a, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode is_great_MP = makePGNode(a_is, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode new_great_MP = makePGNode(a_new, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode a_place_MP = makePGNode(a_a, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode is_place_MP = makePGNode(a_is, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode new_place_MP = makePGNode(a_new, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode city_great_MP = makePGNode(a_city, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode york_great_MP = makePGNode(a_york, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode city_place_MP = makePGNode(a_city, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode york_place = makePGNode(a_york, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode great_a_MP = makePGNode(a_great, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode great_is_MP = makePGNode(a_great, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode great_what_MP = makePGNode(a_great, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode place_a_MP = makePGNode(a_place, b_a, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode place_is_MP = makePGNode(a_place, b_is, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode place_what_MP = makePGNode(a_place, b_what, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");

        PGNode great_place_MP = makePGNode(a_great, b_place, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");
        PGNode place_great_MP = makePGNode(a_place, b_great, "sim=0.10428451439503318, init=0.5, N=0.1042854568587514, N1=0.10428451439503318");


        List expectedList = new ArrayList();
        expectedList.add(place_place_MP);
        expectedList.add(great_great_MP);
        expectedList.add(is_is_MP);
        expectedList.add(a_a_MP);
        expectedList.add(city_what_MP);
        expectedList.add(a_is_MP);
        expectedList.add(is_a_MP);
        expectedList.add(new_a_MP);
        expectedList.add(new_is_MP);
        expectedList.add(a_what_MP);
        expectedList.add(is_what_MP);
        expectedList.add(new_what_MP);
        expectedList.add(city_a_MP);
        expectedList.add(city_is_MP);
        expectedList.add(york_a_MP);
        expectedList.add(york_is_MP);
        expectedList.add(york_what_MP);
        expectedList.add(a_great_MP);
        expectedList.add(is_great_MP);
        expectedList.add(new_great_MP);
        expectedList.add(a_place_MP);
        expectedList.add(is_place_MP);
        expectedList.add(new_place_MP);
        expectedList.add(city_great_MP);
        expectedList.add(york_great_MP);
        expectedList.add(city_place_MP);
        expectedList.add(york_place);
        expectedList.add(great_a_MP);
        expectedList.add(great_is_MP);
        expectedList.add(great_what_MP);
        expectedList.add(place_a_MP);
        expectedList.add(place_is_MP);
        expectedList.add(place_what_MP);
        expectedList.add(great_place_MP);
        expectedList.add(place_great_MP);

        for (int ct = 0; ct < expectedList.size(); ct++)    {
            PGNode expected = (PGNode) expectedList.get(ct);
            PGNode actual = (PGNode) actualArray[ct];

            assertEquals("Testing element = " + ct + "; ", expected.toString(), actual.toString());
        }
    }
}
