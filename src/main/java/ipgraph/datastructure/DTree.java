package ipgraph.datastructure;

import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import ipgraph.parser.pcfg.StanfordPCFGParser;
import ipgraph.utils.LangTools;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright 2014-2015 maochen.org
 * Author: Maochen.G   contact@maochen.org
 * For the detail information about license, check the LICENSE.txt
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA  02111-1307 USA
 * <p>
 * This follows CoNLL-X shared task: Multi-Lingual Dependency Parsing Format
 * <p>
 * Created by Maochen on 12/8/14.
 */
public class DTree extends ArrayList<DNode> {
    private DNode padding;
    private String sentenceType = StringUtils.EMPTY;
    private String originalSentence = StringUtils.EMPTY;

    @Override
    public String toString() {
        return this.stream()
                .filter(x -> x != padding)
                .map(x -> x.toString() + System.lineSeparator())
                .reduce((x, y) -> x + y).get();
    }

    @Override
    public boolean add(DNode node) {
        if (node == null) return false;
        if (this.contains(node)) return false;

        node.setTree(this);
        return super.add(node);
    }

    public List<DNode> getRoots() {
        return this.stream().parallel().filter(x -> x.getDepLabel().equals(LangLib.DEP_ROOT)).distinct().collect(Collectors.toList());
    }

    public DNode getPaddingNode() {
        return padding;
    }

    public String getOriginalSentence() {
        return originalSentence;
    }

    public void setOriginalSentence(String originalSentence) {
        this.originalSentence = originalSentence;
    }

    public String getSentenceType() {
        return sentenceType;
    }

    public void setSentenceType(String sentenceType) {
        this.sentenceType = sentenceType;
    }

    public DTree() {
        padding = new DNode();
        padding.setId(0);
        padding.setForm("^");
        padding.setLemma("^");
        this.add(padding);
    }

    /** **************************************************************
     * Return DNode with specific ID
     */
    public DNode getNodeById(int id) {

        for (DNode n : this) {
            if (n.getId() == id)
                return n;
        }
        return null;
    }

    /** **************************************************************
     * Build dependency-tree from a plain sentence
     */
    public static DTree buildTree(String s) {
        StanfordPCFGParser pcfgParser = new StanfordPCFGParser("", false);
        Tree tree = pcfgParser.getLexicalizedParser().parse(s);

        SemanticHeadFinder headFinder = new SemanticHeadFinder(false); // keep copula verbs as head
        GrammaticalStructure egs = new EnglishGrammaticalStructure(tree, string -> true, headFinder, true);

        // notes: typedDependencies() is suggested
        String conllx = EnglishGrammaticalStructure.dependenciesToString(egs, egs.typedDependencies(), tree, true, true);

        DTree dtree = LangTools.getDTreeFromCoNLLXString(conllx, true);
        return dtree;
    }
}
