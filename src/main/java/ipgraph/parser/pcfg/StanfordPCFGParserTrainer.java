package ipgraph.parser.pcfg;

import edu.stanford.nlp.io.ExtensionFileFilter;
import edu.stanford.nlp.io.NumberRangeFileFilter;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.*;
import ipgraph.datastructure.DTree;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
* This is an EXPERIMENTAL CLASS, NO use, modify or redistribution under any cases.
* <p>
* Created by Maochen on 1/9/15.
*/
public class StanfordPCFGParserTrainer {
    public static final String wsj = "/Users/Maochen/Desktop/treebank_3/parsed/mrg/wsj/";
    public static final String extra = "/Users/Maochen/Desktop/extra/treebank_extra_data/";
    public static final String taggedFiles = extra + "/train-tech-english";
    public static final String modelOutputFolder = "/Users/Maochen/Desktop/";

    public static void trainEngine(String trainDirPath, int startRange, int endRange, String train2DirPath, String train2FileExtension, double extraTrainingSetWeight, String modelPath, int maxLength, String taggedFiles) {
        File f = new File(modelPath);
        if (f.exists()) {
            System.out.println("Delete the existing model in " + f.getAbsolutePath());
            f.delete();
        }


        List<String> para = new ArrayList<>();
        para.add("-goodPCFG");
        para.add("-maxLength");
        para.add(String.valueOf(maxLength));
        para.add("-trainingThreads");
        para.add(String.valueOf(Runtime.getRuntime().availableProcessors()));
        para.add("-wordFunction");
        para.add("edu.stanford.nlp.process.AmericanizeFunction");

        if (taggedFiles != null) {
            para.add("-taggedFiles");
            para.add("tagSeparator=_," + taggedFiles);
        }

        Options op = new Options();
        op.setOptions(para.stream().toArray(String[]::new));

        DiskTreebank trainTreeBank = new DiskTreebank();
        FileFilter trainTreeBankFilter = new NumberRangeFileFilter(startRange, endRange, true);
        trainTreeBank.loadPath(trainDirPath, trainTreeBankFilter);

        DiskTreebank extraTreeBank = null;
        if (train2DirPath != null) {
            extraTreeBank = new DiskTreebank();
            FileFilter extraTreeBankFilter = new ExtensionFileFilter(train2FileExtension, true);
            extraTreeBank.loadPath(train2DirPath, extraTreeBankFilter);
        }

        LexicalizedParser.getParserFromTreebank(trainTreeBank, extraTreeBank, extraTrainingSetWeight, null, op, null, null).saveParserToSerialized(modelPath);
    }

    public static void printParseTree(LexicalizedParser parser, String sentence) {
        // Parse right after get through tokenizer.Is the dog awesome when the dog is wet?
        Tree tree = parser.parse(sentence);
        System.out.println(tree.pennString());

        SemanticHeadFinder headFinder = new SemanticHeadFinder(false); // keep copula verbs as head
        Collection<TypedDependency> dependencies = new EnglishGrammaticalStructure(tree, string -> true, headFinder).typedDependencies();
        dependencies.stream().forEach(System.out::println);

        List<CoreLabel> tokens = tree.taggedLabeledYield();
        tokens.parallelStream().forEach(x -> {
            x.setOriginalText(x.word());
            x.setLemma(x.word());
        });
        DTree dtree = StanfordTreeBuilder.generate(tokens, dependencies, null);
        System.out.println(dtree);
    }

    public static String trainPCFGModel() throws IOException {
        String modelPath = modelOutputFolder + "/englishPCFG.ser.gz";
        trainEngine(wsj, 1, 2502, extra, ".mrg", 1.0, modelPath, 40, taggedFiles);
        return modelPath;
    }

    public static void main(String[] args) throws IOException {
        String modelPath = trainPCFGModel();
        LexicalizedParser parser = LexicalizedParser.loadModel(modelPath, new ArrayList<>());
        Scanner scan = new Scanner(System.in);
        String input = StringUtils.EMPTY;
        String quitRegex = "q|quit|exit";
        while (!input.matches(quitRegex)) {
            System.out.println("Please enter sentence:");
            input = scan.nextLine();
            if (!input.trim().isEmpty() && !input.matches(quitRegex)) {
                printParseTree(parser, input);
            }
        }

    }

}
