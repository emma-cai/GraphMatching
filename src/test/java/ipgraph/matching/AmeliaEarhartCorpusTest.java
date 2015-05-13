package ipgraph.matching;

import com.clearspring.analytics.util.Lists;
import com.google.common.io.Resources;
import com.interdataworking.mm.alg.NodeComparer;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DTree;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Read AmeliaEarhart.txt, using AmeliaEarhartTest.json.
 */
@RunWith(Parameterized.class)
public class AmeliaEarhartCorpusTest {

    public static final Set<String> files = new HashSet();

    public static final Set<String> postagSet = NodeComparer.postagSet;

    private static List<DGraph> graphs = Lists.newArrayList();

    private static List<String> lines = Lists.newArrayList();

    @Parameterized.Parameter(value= 0)
    public String filename;
    @Parameterized.Parameter(value= 1)
    public String query;
    @Parameterized.Parameter(value= 2)
    public String answer;

    @BeforeClass
    public static void readFile()   {

    }

     @Test
    public void test() throws Exception {

         // JERRY: for this particular test, we need read the text file only once

         File inFile = null;

         if(!files.contains(filename)) {
            //reading the lines
            try {
                URI fileURI = Resources.getResource(filename).toURI();
                inFile = new File(fileURI);
                Path path = Paths.get(inFile.getAbsolutePath());

                lines = Files.readAllLines(path);
                //lines = TextFileUtil.readLines("miscellaneous/" + filename, true);
                files.add(filename);
            } catch (Exception e) {
                System.out.println("Couldn't read document: " + inFile + ". Exiting");
                throw e;
            }

            for (String line : lines) {
                DTree dtree = DTree.buildTree(line);
                DGraph dgraph = DGraph.buildDGraph(dtree).getSubgraph(postagSet);
                graphs.add(dgraph);
            }
        }

         DTree QDTree = DTree.buildTree(query);
         DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);

         double minimumCost = Double.MAX_VALUE;
         String actual = null;
         for(int i = 0; i < graphs.size(); i++)  {
             DGraph dgragh = graphs.get(i);
             double graphSimilarity = DMatching.computeMatchingCost(dgragh, QDGraph);

             if (Double.compare(graphSimilarity, minimumCost) < 0) {
                 minimumCost = graphSimilarity;
                 actual = lines.get(i);
             }
         }

         System.out.println("query = " + query);
         System.out.println("expected = " + answer);
         System.out.println("actual = " + actual);
         System.out.println("minimumCost = " + minimumCost);
         assertEquals(answer, actual);
    }

    /******************************************************************************************************
     *
     * @return
     */
    @Parameterized.Parameters(name="{0}:{1}")
    public static Collection<Object[]> prepare(){

        ArrayList<Object[]> result = new ArrayList<Object[]>();

        File jsonTestFile = null;
        try {
            URI fileURI = Resources.getResource("AmeliaEarhartTest.json").toURI();
            jsonTestFile = new File(fileURI);
            String filename = jsonTestFile.getAbsolutePath();
            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new FileReader(filename));
            JSONArray jsonObject = (JSONArray) obj;
            ListIterator<JSONObject> li = jsonObject.listIterator();
            while (li.hasNext()) {
                JSONObject jo = li.next();
                String fname = (String) jo.get("file");
                String query = (String) jo.get("query");
                String answer = (String) jo.get("answer");
                result.add(new Object[]{fname,query,answer});
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): File not found: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FileNotFoundException");
        }
        catch (IOException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): IO exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            throw new RuntimeException("IOException");
        }
        catch (ParseException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): Parse exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            throw new RuntimeException("ParseException");
        }
        catch (Exception e) {
            System.out.println("Error in InferenceWikiTest.prepare(): Parse exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Exception");
        }
        return result;
    }


}