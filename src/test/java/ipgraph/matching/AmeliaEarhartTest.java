package ipgraph.matching;

import com.clearspring.analytics.util.Lists;
import com.google.common.io.Resources;
import com.interdataworking.mm.alg.MapPair;
import com.interdataworking.mm.alg.SimplifiedMatch;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Read AmeliaEarhart.txt, using AmeliaEarhartTest.json.
 */
@RunWith(Parameterized.class)
public class AmeliaEarhartTest {

    public static final Set<String> files = new HashSet();


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
         List<String> lines = null;
         List<DTree> parses = Lists.newArrayList();
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
                parses.add(dtree);
            }

//            //loading the assertions
//            interpreter.question = false;
//            for (String line:lines) {
//                System.out.println("\nAsserting: " + line);
//                String response = interpreter.interpretSingle(line);
//                System.out.println("Response: " + response);
//            }

        }

        System.out.println("\nQuestion: " + query);
         DTree qTree = DTree.buildTree(query);
         for(DTree dtree : parses)  {
             //SimplifiedMatch compareGraphs = new SimplifiedMatch();
             //Matching matching = new Matching(compareGraphs);

             // computeGraphSimilarity on each, storing into list
             //MapPair[] actualPairs =  matching.computeNodeSimilarity(dgraph1, dgraph2);
         }

         // sort graph similarity scores; test on those


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