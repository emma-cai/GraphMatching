package ipgraph.experiments;

import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by qingqingcai on 5/22/15.
 */
public class DependencyPatternSummarize {

    private static String baseDir = "src/test/resources/wiki";

    public static void main(String[] args) {

        String jsonpath = baseDir + File.separator + "IRtests.json";
        runPatternFrequency(jsonpath);
    }

    public static void runPatternFrequency(String jsonpath) {

        TreeMap<String, Integer> pattern_fre = new TreeMap<>();
        File jsonTestFile = null;
        try {
            jsonTestFile = new File(jsonpath);
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

                DTree dtree = DTree.buildTree(query);
                DNode whDNode = dtree.getNodeById(1);
                String whForm = whDNode.getForm();
                String whDepLabel = whDNode.getDepLabel();

                DNode whDNodeParent = whDNode.getHead();
                String form_deplabel = whForm + " - " + whDepLabel + " - " + whDNodeParent.getPOS();

                int fre = 1;
                if (pattern_fre.containsKey(form_deplabel)) {
                    fre = pattern_fre.get(form_deplabel) + 1;
                }
                pattern_fre.put(form_deplabel, fre);
                System.out.println("\n" + query);
                System.out.println(whForm + " - " + whDepLabel + " - " + whDNodeParent.getForm() + " - " + whDNodeParent.getPOS());
            }

            TreeMap sorted = sortByValue(pattern_fre);

            System.out.println("\n\n\nSorted Answers:");
            sorted.forEach((k, v) -> {
                System.out.println(k + "\t" + v);
            });
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
    }

    private static TreeMap sortByValue(Map unsortMap) {

        TreeMap sortedMap = new TreeMap();

        unsortMap.forEach((k, v) -> {
            List listOfValues = new ArrayList<>();
            if (sortedMap.containsKey(v)) {
                listOfValues = (List) sortedMap.get(v);
            } else {
                listOfValues = new ArrayList();
            }
            listOfValues.add(k);
            sortedMap.put(v, listOfValues);

        });
        return sortedMap;
    }
}
