package ipgraph.parser.nn;

import com.google.common.collect.ImmutableSet;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import ipgraph.datastructure.LangLib;
import ipgraph.utils.LangTools;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.*;

/**
* Created by Maochen on 4/6/15.
*/
public class StanfordNNDepParser {

    private static final TokenizerFactory<Word> tf = PTBTokenizer.PTBTokenizerFactory.newTokenizerFactory();
    private static final MaxentTagger posTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
    public static DependencyParser nndepParser = null;

    private List<NERClassifierCombiner> ners = new ArrayList<>();

    // This is for Lemma Tagger
    private static final Set<String> particles = ImmutableSet.of(
            "abroad", "across", "after", "ahead", "along", "aside", "away", "around",
            "back", "down", "forward", "in", "off", "on", "over", "out",
            "round", "together", "through", "up"
    );

    // 1. Tokenize
    private List<CoreLabel> stanfordTokenize(String str) {
        Tokenizer<Word> originalWordTokenizer = tf.getTokenizer(new StringReader(str), "ptb3Escaping=false");
        Tokenizer<Word> tokenizer = tf.getTokenizer(new StringReader(str));

        List<Word> originalTokens = originalWordTokenizer.tokenize();
        List<Word> tokens = tokenizer.tokenize();
        // Curse you Stanford!
        List<CoreLabel> coreLabels = new ArrayList<>(tokens.size());

        for (int i = 0; i < tokens.size(); i++) {
            CoreLabel coreLabel = new CoreLabel();
            coreLabel.setWord(tokens.get(i).word());
            coreLabel.setValue(originalTokens.get(i).word());
            coreLabel.setOriginalText(originalTokens.get(i).word());
            coreLabels.add(coreLabel);
        }

        return coreLabels;
    }

    // 2. POS Tagger
    private void tagPOS(List<CoreLabel> tokenizedSentence) {
        List<TaggedWord> tokens = posTagger.tagSentence(tokenizedSentence);

        for (int i = 0; i < tokens.size(); i++) {
            String pos = tokens.get(i).tag();
            tokenizedSentence.get(i).setTag(pos);
        }
    }

    // 3. Lemma Tagger
    private void tagLemma(List<CoreLabel> tokens) {
        // Not sure if this can be static.
        Morphology morpha = new Morphology();

        for (CoreLabel token : tokens) {
            String lemma;
            if (token.tag().length() > 0) {
                String phrasalVerb = phrasalVerb(morpha, token.word(), token.tag());
                if (phrasalVerb == null) {
                    lemma = morpha.lemma(token.word(), token.tag());
                } else {
                    lemma = phrasalVerb;
                }
            } else {
                lemma = morpha.stem(token.word());
            }

            // LGLibEn.convertUnI only accept cap I.
            if (lemma.equals("i")) {
                lemma = "I";
            }

            token.setLemma(lemma);
        }
    }

    // For Lemma
    private String phrasalVerb(Morphology morpha, String word, String tag) {
        // must be a verb and contain an underscore
        assert (word != null);
        assert (tag != null);
        if (!tag.startsWith(LangLib.POS_VB) || !word.contains("_")) return null;

        // check whether the last part is a particle
        String[] verb = word.split("_");
        if (verb.length != 2) return null;
        String particle = verb[1];
        if (particles.contains(particle)) {
            String base = verb[0];
            String lemma = morpha.lemma(base, tag);
            return lemma + '_' + particle;
        }

        return null;
    }

    // 4. Dependency Label
    private GrammaticalStructure tagDependencies(List<? extends HasWord> taggedWords) {
        GrammaticalStructure gs = nndepParser.predict(taggedWords);
        return gs;
    }

    // 5. Named Entity Tagger
    private void tagNamedEntity(List<CoreLabel> tokens) {
        ners.stream().forEach(ner -> ner.classify(tokens));
    }

    public DTree parse(String sentence) {
        List<CoreLabel> tokenizedSentence = stanfordTokenize(sentence);
        tagPOS(tokenizedSentence);
        tagLemma(tokenizedSentence);
        GrammaticalStructure gs = tagDependencies(tokenizedSentence);
        tagNamedEntity(tokenizedSentence);
        // TODO: use the commented version
        String conllXString = getCoNLLXString(gs.typedDependencies(), tokenizedSentence);
        DTree depTree = LangTools.getDTreeFromCoNLLXString(conllXString, false);
        //        DTree depTree = StanfordTreeBuilder.generate(tokenizedSentence, gs.typedDependencies(), null);
        return depTree;
    }

    public StanfordNNDepParser() {
        this(null, true);
    }

    public StanfordNNDepParser(final String inputModelPath, boolean initNER) {
        String modelPath = inputModelPath == null || inputModelPath.trim().isEmpty() ? DependencyParser.DEFAULT_MODEL : inputModelPath;
        nndepParser = DependencyParser.loadFromModelFile(modelPath);

        if (initNER) {
            // STUPID NER, Throw IOException in the constructor ... : (
            try {
                ners.add(new NERClassifierCombiner("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCoNLLXString(Collection<TypedDependency> deps, List<CoreLabel> tokens) {
        StringBuilder bf = new StringBuilder();

        Map<Integer, TypedDependency> indexedDeps = new HashMap<>(deps.size());
        for (TypedDependency dep : deps) {
            indexedDeps.put(dep.dep().index(), dep);
        }

        int idx = 1;

        for (CoreLabel token : tokens) {
            String word = token.value();
            String pos = token.tag();
            String cPos = (token.get(CoreAnnotations.CoarseTagAnnotation.class) != null) ?
                    token.get(CoreAnnotations.CoarseTagAnnotation.class) : LangTools.getCPOSTag(pos);
            String lemma = token.lemma() != null ? token.lemma() : "_";
            Integer gov = indexedDeps.containsKey(idx) ? indexedDeps.get(idx).gov().index() : 0;
            String reln = indexedDeps.containsKey(idx) ? indexedDeps.get(idx).reln().toString() : "erased";
            String out = String.format("%d\t%s\t%s\t%s\t%s\t_\t%d\t%s\t_\t_\n", idx, word, lemma, cPos, pos, gov, reln);
            bf.append(out);
            idx++;
        }
        bf.append("\n");
        return bf.toString();
    }

    // This is just for debugging.
    public GrammaticalStructure getGrammaticalStructure(String sentence) {
        List<CoreLabel> tokenizedSentence = stanfordTokenize(sentence);
        tagPOS(tokenizedSentence);
        tagLemma(tokenizedSentence);
        return tagDependencies(tokenizedSentence);
    }

    public static void main(String[] args) {
        StanfordNNDepParser parser = new StanfordNNDepParser(DependencyParser.DEFAULT_MODEL, false);
        String text = "Mary can almost (always) tell when movies use fake dinosaurs and make changes.";
        System.out.println(parser.parse(text));
    }

    /**
     * Created by Maochen on 1/19/15.
     */
    @Deprecated
    public static class StanfordTreeDirtyPatch {
        private static final Map<String, DNode> words = new HashMap<String, DNode>() {{
            DNode locate = new DNode(0, "located", "locate", LangLib.CPOSTAG_VERB, LangLib.POS_VBD, StringUtils.EMPTY);
            put(locate.getLemma(), locate);
            DNode working = new DNode(0, "working", "work", LangLib.CPOSTAG_VERB, LangLib.POS_VBG, StringUtils.EMPTY);
            put(working.getLemma(), working);
            DNode to = new DNode(1, "to", "to", LangTools.getCPOSTag(LangLib.POS_IN), LangLib.POS_IN, StringUtils.EMPTY);
            put(to.getLemma(), to);
            DNode in = new DNode(2, "in", "in", LangTools.getCPOSTag(LangLib.POS_IN), LangLib.POS_IN, StringUtils.EMPTY);
            put(in.getLemma(), in);
            DNode on = new DNode(2, "on", "on", LangTools.getCPOSTag(LangLib.POS_IN), LangLib.POS_IN, StringUtils.EMPTY);
            put(on.getLemma(), on);
            DNode blue = new DNode(2, "blue", "blue", LangLib.CPOSTAG_ADJ, LangLib.POS_JJ, StringUtils.EMPTY);
            put(blue.getLemma(), blue);
            DNode red = new DNode(2, "red", "red", LangLib.CPOSTAG_ADJ, LangLib.POS_JJ, StringUtils.EMPTY);
            put(red.getLemma(), red);
            DNode slow = new DNode(2, "slow", "slow", LangLib.CPOSTAG_ADJ, LangLib.POS_JJ, StringUtils.EMPTY);
            put(slow.getLemma(), slow);
            DNode french = new DNode(3, "french", "french", LangLib.CPOSTAG_NOUN, LangLib.POS_NNP, LangLib.DEP_NSUBJ);
            put(french.getLemma(), french);
            DNode insect = new DNode(4, "insect", "insect", LangLib.CPOSTAG_NOUN, LangLib.POS_NN, StringUtils.EMPTY);
            put(insect.getLemma(), insect);
            DNode username = new DNode(5, "username", "username", LangLib.CPOSTAG_NOUN, LangLib.POS_NN, StringUtils.EMPTY);
            put(username.getLemma(), username);
            DNode can = new DNode(6, "can", "can", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(can.getLemma(), can);
            DNode could = new DNode(6, "could", "can", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(could.getLemma(), could);
            DNode coulda = new DNode(6, "coulda", "can", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(coulda.getLemma(), coulda);
            DNode shall = new DNode(6, "shall", "shall", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(shall.getLemma(), shall);
            DNode should = new DNode(6, "should", "shall", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(should.getLemma(), should);
            DNode shoulda = new DNode(6, "shoulda", "shall", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(shoulda.getLemma(), shoulda);
            DNode will = new DNode(6, "will", "will", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(will.getLemma(), will);
            DNode would = new DNode(6, "would", "will", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(would.getLemma(), would);
            DNode may = new DNode(6, "may", "may", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(may.getLemma(), may);
            DNode might = new DNode(6, "might", "may", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(might.getLemma(), might);
            DNode must = new DNode(6, "must", "must", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(must.getLemma(), must);
            DNode musta = new DNode(6, "musta", "must", LangTools.getCPOSTag(LangLib.POS_MD), LangLib.POS_MD, LangLib.DEP_AUX);
            put(musta.getLemma(), musta);
        }};


        private static final Map<String, String> auxVerbFix = new HashMap<String, String>() {
            {
                put("does", LangLib.POS_VBZ);
                put("did", LangLib.POS_VBD);
                put("do", LangLib.POS_VBP);
            }
        };

        // Most things here are stanford parsing issue.
        public static void dirtyPatch(DNode node) {
            // PS1: Don't fix the root to a verb if it is not. Ex: "a car." -> car is root.
            // PS2: "be simulated", actually the whole tree should start with node instead of "be", cannot fix the dep.

            // Dont assign verb that['s] to possessive
            if (node.getLemma().equalsIgnoreCase("'s") && !node.getPOS().startsWith(LangLib.POS_VB)) {
                node.setPOS(LangLib.POS_POS);
            }

            // Inconsistency in VBG and JJ
            // Ex: What is the reason for missing internal account? --> missing can be either JJ or VBG.
            else if (LangLib.POS_JJ.equals(node.getPOS()) && node.getLemma().endsWith("ing")) {
                node.setPOS(LangLib.POS_VBG);
            }

            // Mislabeled VBG as NN.
            else if (node.getPOS().startsWith(LangLib.POS_NN) && node.getLemma().endsWith("ing") && node.getDepLabel().equals(LangLib.DEP_ROOT)) {
                node.setPOS(LangLib.POS_VBG);
            }

            // If root has aux and itself is a Noun, correct it as verb.
            else if (node.getPOS().startsWith(LangLib.POS_NN) && node.getDepLabel().equals(LangLib.DEP_ROOT) && !node.getChildrenByDepLabels(LangLib.DEP_AUX).isEmpty()) {
                node.setPOS(LangLib.POS_VB);
            }

            // Fix root spread as verb
            else if (node.getLemma().toLowerCase().equals("spread") && node.getPOS().startsWith(LangLib.POS_NN) && node.getDepLabel().equals(LangLib.DEP_ROOT)) {
                node.setPOS(LangLib.POS_VBD);
            }

            DNode fixedNode = words.get(node.getLemma().toLowerCase());
            if (fixedNode != null) {
                if (node.getLemma().toLowerCase().equals("to")) {
                    if (node.getDepLabel().equals(LangLib.DEP_PREP)) {
                        node.setPOS(LangLib.POS_IN);
                    } else {
                        // Dont patch
                    }
                }

                // French fix.
                else if ("french".equalsIgnoreCase(node.getLemma()) && node.getDepLabel().startsWith(LangLib.DEP_NSUBJ)) {
                    node.setPOS(LangLib.POS_NNP);
                }

                // General Case
                else {
                    if (fixedNode.getLemma() != null && !node.getLemma().equals(fixedNode.getLemma())) {
                        node.setLemma(fixedNode.getLemma());
                    }

                    if (fixedNode.getPOS() != null && !node.getPOS().equals(fixedNode.getPOS())) {
                        node.setPOS(fixedNode.getPOS());
                    }

                    if (fixedNode.getDepLabel() != null && !node.getDepLabel().equals(fixedNode.getDepLabel())) {
                        node.setDepLabel(fixedNode.getDepLabel());
                    }
                }
            }

            // ---------- Fix the Label ------------
            // Fix the preposition label
            if (LangLib.POS_IN.equals(node.getPOS()) && !LangLib.DEP_MARK.equals(node.getDepLabel())) {
                node.setDepLabel(LangLib.DEP_PREP);
            }

            // For aux verb tagged as Noun.
            if (node.getId() == 1 && auxVerbFix.containsKey(node.getLemma().toLowerCase())) {
                node.setPOS(auxVerbFix.get(node.getLemma().toLowerCase()));
            }

            // hold together -> "together" should be PRT
            if (node.getLemma().equals("together") && node.getHead() != null && node.getHead().getLemma().equals("hold") && !node.getDepLabel().equals(LangLib.DEP_PRT)) {
                node.setDepLabel(LangLib.DEP_PRT);
            }

            // Ex: What bad weather.
            if (node.getPOS().equals(LangLib.POS_WDT) && node.getDepLabel().equals(LangLib.DEP_ATTR)) {
                node.setDepLabel(LangLib.DEP_DET);
            }
        }

        public static void dirtyPatchNER(DNode node) {
            if (!node.getNamedEntity().isEmpty()) {
                // 5pm. -> (. -> Time)
                if (node.getId() == node.getTree().size() - 1 && LangLib.DEP_PUNCT.equals(node.getDepLabel())) {
                    node.setLemma(node.getForm());
                    node.setNamedEntity(StringUtils.EMPTY);
                }

                // "Between XXXX", dont tag "Between"
                else if (LangLib.POS_IN.equals(node.getPOS()) && LangLib.NE_DATE.equalsIgnoreCase(node.getNamedEntity())) {
                    node.setNamedEntity(StringUtils.EMPTY);
                }


                // Dirty Patch for Date.
                else if (node.getLemma().equalsIgnoreCase("and")) {
                    if (node.getNamedEntity().equalsIgnoreCase(LangLib.NE_DATE) || node.getNamedEntity().equalsIgnoreCase(LangLib.NE_PERSON)) {
                        node.setNamedEntity(StringUtils.EMPTY);
                    }
                }

                // Blame for stanford NER. Does Bill know John?  [ORG Does Bill]
                else if (node.getLemma().equalsIgnoreCase("does")) {
                    node.setNamedEntity(StringUtils.EMPTY);
                }

            }
        }


    }
}
