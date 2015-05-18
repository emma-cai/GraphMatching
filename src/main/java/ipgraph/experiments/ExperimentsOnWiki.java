package ipgraph.experiments;

import com.interdataworking.mm.alg.NodeComparer;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DTree;
import ipgraph.matching.DMatching;
import ipgraph.parser.pcfg.StanfordPCFGParser;
import ipgraph.utils.LangTools;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by qingqingcai on 5/18/15.
 */
public class ExperimentsOnWiki {
    public static final Set<String> postagSet = NodeComparer.postagSet;

    private static String baseDir = "src/test/resources/wiki";

    private static String textDir = baseDir;

    private static String conllxDir = baseDir + File.separator + "wikiconllx";

    private static HashMap<String, List<String>> file_texts = new HashMap<>();

    private static HashMap<String, List<DGraph>> file_graphs = new HashMap<>();

    private static int TESTNUM = 0;

    private static int CORRECTNUM = 0;

    public static void main(String[] args) throws IOException {

        String jsonpath = baseDir + File.separator + "IRtests.json";

        //    runTextToConllx(textDir, conllxDir);

        runPerformanceTest(jsonpath, true);
    }

    /** **************************************************************
     * Read plain text, convert the text to conllx (which will be used
     * to build a tree quickly)
     * @param textDir Path to plain text
     * @param conllxDir Path to conllx
     * @throws IOException
     */
    public static void runTextToConllx(String textDir, String conllxDir) throws IOException {

        File inFile = null;
        try {
            File folder = new File(textDir);
            File[] listOfFiles = folder.listFiles();
            for (File f : listOfFiles) {
                if (f.isFile() && f.getName().endsWith(".txt")) {
                    String conllxPath = conllxDir + File.separator + f.getName().substring(0, f.getName().indexOf(".txt")) + ".conllx";
                    HashMap<String, String> text_conllx = new HashMap<>();

                    inFile = new File(f.getAbsolutePath());
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        if (line.trim().isEmpty())
                            continue;
                        StanfordPCFGParser pcfgParser = new StanfordPCFGParser("", false);
                        Tree tree = pcfgParser.getLexicalizedParser().parse(line);
                        SemanticHeadFinder headFinder = new SemanticHeadFinder(false); // keep copula verbs as head
                        GrammaticalStructure egs = new EnglishGrammaticalStructure(tree, string -> true, headFinder, true);
                        System.out.println("current = " + line);
                        if (egs != null) {
                            String conllx = EnglishGrammaticalStructure.dependenciesToString(egs, egs.typedDependencies(), tree, true, true);
                            text_conllx.put(line, conllx);
                        }
                    }

                    File outFile = new File(conllxPath);
                    if (!outFile.exists())
                        outFile.createNewFile();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
                    text_conllx.forEach((text, conllx) -> {
                        try {
                            //    bw.write("\"text:\"" + text + "\"conllx:\"" + conllx);
                            bw.write(text + "\n" + conllx);
                            bw.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Couldn't read document: " + inFile + ". Exiting");
            throw e;
        }
    }

    /** **************************************************************
     * Compute the QA performance: read json file firstly and decide
     * which test file should be read;
     * @param jsonpath Path to json file (containing questions)
     * @param fromConllx Set fromConllx = true if you want to read from
     *                   conllx (will be faster); Set fromConllx = false
     *                   if you want to read from plain text (takes time
     *                   since it firstly run Stanford dependency tree)
     */
    public static void runPerformanceTest(String jsonpath, boolean fromConllx) {

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

                runQA(fname.substring(0, fname.indexOf(".txt")), query, answer, fromConllx);
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
    }

    /** **************************************************************
     * For each "query", find the best answer from file "fname"
     * @param fname Path to text file containing statements
     * @param query Query
     * @param answer Expected answer
     * @param fromConllx True if read conllex; False if read plain text
     * @throws Exception
     */
    public static void runQA(String fname, String query, String answer, boolean fromConllx) throws Exception {

        File inFile = null;

        if(!file_texts.containsKey(fname)) {
            //reading the lines
            try {
                if (fromConllx == false) {
                    inFile = new File(textDir + File.separator + fname + ".txt");
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> texts = Files.readAllLines(path);
                    List<DGraph> graphs = new ArrayList<>();
                    for (String text : texts) {
                        DTree dtree = DTree.buildTree(text);
                        DGraph dgraph = DGraph.buildDGraph(dtree).getSubgraph(postagSet);
                        graphs.add(dgraph);
                    }
                    file_texts.put(fname, texts);
                    file_graphs.put(fname, graphs);
                } else {
                    inFile = new File(conllxDir + File.separator + fname + ".conllx");
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path);
                    boolean afterText = false;

                    List<String> texts = new ArrayList<>();
                    List<DGraph> graphs = new ArrayList<>();
                    String text = "";
                    String conllx = "";
                    for (String line : lines) {
                        if (afterText == false) {
                            text = line;
                            conllx = "";
                            afterText = true;
                        } else if (line.trim().isEmpty()) {
                            DTree dtree = LangTools.getDTreeFromCoNLLXString(conllx, true);
                            DGraph dgraph = DGraph.buildDGraph(dtree).getSubgraph(postagSet);
                            texts.add(text);
                            graphs.add(dgraph);
                            afterText = false;
                        } else {
                            conllx += line + "\n";
                        }
                    }
                    file_texts.put(fname, texts);
                    file_graphs.put(fname, graphs);
                }

            } catch (Exception e) {
                System.out.println("Couldn't read document: " + inFile + ". Exiting");
                throw e;
            }
        }

        List<DGraph> graphs = file_graphs.get(fname);
        List<String> texts = file_texts.get(fname);

        DTree QDTree = DTree.buildTree(query);
        DGraph QDGraph = DGraph.buildDGraph(QDTree).getSubgraph(postagSet);

        double minimumCost = Double.MAX_VALUE;
        String actual = null;
        for(int i = 0; i < graphs.size(); i++)  {
            DGraph dgragh = graphs.get(i);
            double graphSimilarity = DMatching.computeMatchingCost(dgragh, QDGraph);

            if (Double.compare(graphSimilarity, minimumCost) < 0) {
                minimumCost = graphSimilarity;
                actual = texts.get(i);
            }
        }

        TESTNUM++;
        boolean correctAnswer = judgeAnswer(answer, actual);
        if (correctAnswer)
            CORRECTNUM++;

        System.out.println("\n--------------------------------------------------");
        System.out.println("query = " + query);
        System.out.println("expected = " + answer);
        System.out.println("actual = " + actual);
        System.out.println("minimumCost = " + minimumCost);
        System.out.println("isCorrect = " + correctAnswer);
        System.out.println("Precision = " + (CORRECTNUM * 1.0) / (TESTNUM * 1.0));
        System.out.println("--------------------------------------------------");
    }

    /** **************************************************************
     * Compare the actual answer with expected answer
     * @param expected Expected answer in json file
     * @param actual Actual answer returned by GraphMatching
     * @return
     */
    public static boolean judgeAnswer(String expected, String actual) {

        return (expected.contains(actual) || actual.contains(expected));
    }
}
