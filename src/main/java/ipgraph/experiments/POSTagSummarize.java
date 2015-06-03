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
 * Created by qingqingcai on 5/28/15.
 */
public class POSTagSummarize {

    private static String baseDir = "src/test/resources/wiki";

    public static void main(String[] args) {

        String jsonpath = baseDir + File.separator + "IRtests.json";
        runPatternFrequency(jsonpath);
    }

    public static void runPatternFrequency(String jsonpath) {

        TreeMap<String, HashMap<String, Integer>> posTag_words_frequency = new TreeMap<>();
        TreeMap<String, Integer> posTag_fre = new TreeMap<>();

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
                System.out.println("\n" + query);

                ListIterator<DNode> iter = dtree.listIterator();
                while (iter.hasNext()) {
                    DNode dnode = (DNode) iter.next();
                    String posTag = dnode.getPOS();
                //    String form = dnode1.getForm();         // check form frequency
                    String form = dnode.getForm() + ":" + Integer.toString(dnode.getId());               // check ID frequency

                    HashMap<String, Integer> words_frequency = new HashMap<>();
                    if (posTag_words_frequency.containsKey(posTag)) {
                        words_frequency = posTag_words_frequency.get(posTag);
                        int frequency = 0;
                        if (words_frequency.containsKey(form)) {
                            frequency = words_frequency.get(form);
                        }
                        frequency = frequency + 1;
                        words_frequency.put(form, frequency);
                    } else {
                        words_frequency.put(form, 1);
                    }
                    posTag_words_frequency.put(posTag, words_frequency);

                    System.out.println(dnode.getForm()+":"+ dnode.getId() + " - " + dnode.getPOS());
                }
            }

   //         TreeMap sorted = sortByValue(pattern_fre);

            System.out.println("\n\n\nPOSTag and list of words with the same POSTag: ");
            posTag_words_frequency.forEach((posTag, words_frequency) -> {
                System.out.println(posTag);
//                words_frequency.forEach((word, frequency) -> {
//                    System.out.println("\t" + word + " - " + frequency);
//                });
            });

            System.out.println("\nWDT = " + posTag_words_frequency.get("WDT"));
            System.out.println("\nWP = " + posTag_words_frequency.get("WP"));
            System.out.println("\nWRB = " + posTag_words_frequency.get("WRB"));
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
