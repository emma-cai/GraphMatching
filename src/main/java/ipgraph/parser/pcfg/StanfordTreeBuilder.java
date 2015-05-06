package ipgraph.parser.pcfg;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.TypedDependency;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import ipgraph.datastructure.LangLib;
import ipgraph.utils.LangTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
* For the Stanford parse tree.
* Created by Maochen on 10/28/14.
*/

// Should only be used in PCFG Parser.
public class StanfordTreeBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(StanfordTreeBuilder.class);

    // This is because of the difference of the stanford vs clearnlp standards. It is not err.
    private static void patchTree(DNode node) {
        if (node.getLemma().equalsIgnoreCase("-LRB-")) {
            node.setForm("(");
        } else if (node.getLemma().equalsIgnoreCase("-RRB-")) {
            node.setForm(")");
        }

        // This is due to the inconsistency of stanford parser.
        if (node.getDepLabel().equals(LangLib.DEP_NUMBER)) {
            node.setDepLabel(LangLib.DEP_NUM);
        }
    }

    private static void convertCopHead(DTree tree) {
        if (tree.getRoots().isEmpty()) {
            return;
        }
        DNode originalRoot = tree.getRoots().get(0); // JJ mostly.
        List<DNode> cops = originalRoot.getChildrenByDepLabels(LangLib.DEP_COP);

        if (!originalRoot.getPOS().startsWith(LangLib.POS_VB) && !cops.isEmpty() && cops.get(0) != originalRoot) {
            DNode cop = cops.get(0);

            cop.setDepLabel(LangLib.DEP_ROOT);

            // Label might be corrected by dirty patch.
            if (originalRoot.getDepLabel().equals(LangLib.DEP_ROOT)) {
                originalRoot.setDepLabel(LangLib.DEP_DEP);
            }

            cop.setHead(tree.getPaddingNode());
            originalRoot.setHead(cop);

            tree.getPaddingNode().removeChild(originalRoot.getId());
            tree.getPaddingNode().addChild(cop);


            // Add original deps to cop
            for (DNode child : originalRoot.getChildren()) {
                if (child.getPOS().equals(LangLib.POS_IN)) {
                    continue;
                }
                originalRoot.removeChild(child.getId());

                if (child != cop) {
                    cop.addChild(child);
                    child.setHead(cop);
                }
            }

            cop.addChild(originalRoot);
        }
    }

    // cPOSTag is not used here.
    public static DTree generate(List<CoreLabel> tokens, Collection<TypedDependency> dependencies, List<Label> cPOSTag) {
        DTree depTree = new DTree();

        for (int i = 0; i < tokens.size(); i++) {
            CoreLabel token = tokens.get(i);
            String cPOSTagValue = cPOSTag == null ? LangTools.getCPOSTag(token.tag()) : cPOSTag.get(i).value();
            DNode node = new DNode(i + 1, token.originalText(), token.lemma(), cPOSTagValue, token.tag(), StringUtils.EMPTY, 0);
            depTree.add(node);
            setNamedEntity(node, token);
        }

        // 0 is _R_ here
        dependencies.parallelStream().filter(td -> td.gov().index() != -1).forEach(td -> {
            int sourceIndex = td.gov().index();
            int targetIndex = td.dep().index();
            String childDEPLabel = td.reln().toString();

            DNode child = depTree.get(targetIndex);
            DNode parent = depTree.get(sourceIndex);

            if (child == null) {
                LOG.error(parent.getForm() + " doesn't have proper child.");
            } else {
                // ClearNLP has different possessive handling.
                if (child.getPOS().equals(LangLib.POS_POS) && !childDEPLabel.equals(LangLib.DEP_POSSESSIVE)) {
                    childDEPLabel = LangLib.DEP_POSSESSIVE;
                    parent.setDepLabel(LangLib.DEP_POSS);
                }

                child.setDepLabel(childDEPLabel);
                child.setHead(parent);
                parent.addChild(child);
            }
        });


        depTree.stream().forEach(node -> {
            if (node.getDepLabel() == null) {
                if (node.getForm().matches("\\p{Punct}+")) { // Attach Punctuation
                    DNode rootVerb = depTree.getRoots().stream().findFirst().orElse(null);
                    node.setDepLabel(LangLib.DEP_PUNCT);
                    node.setHead(rootVerb);
                    depTree.get(rootVerb.getId()).addChild(node);
                } else {
                    LOG.error("node does not have label. ->", node.toString());
                }
            }

            patchTree(node);
            ipgraph.parser.nn.StanfordNNDepParser.StanfordTreeDirtyPatch.dirtyPatchNER(node);
            //            StanfordTreeDirtyPatch.dirtyPatch(node);
            LangTools.generateName(node);
        });

        // Dont put it before dirty patch.
        // Ex: Is the car slow? -> slow, VBZ should be correct to JJ first and then convert tree.
        convertCopHead(depTree);
        swapPossessives(depTree);
        return depTree;
    }

    private static void swapPossessives(DTree depTree) {
        Predicate<DNode> pred = (x) -> {
            if (x.getHead() == null) {
                return false;
            }
            boolean needAlter = x.getHead().isRoot();
            needAlter &= x.getPOS().startsWith(LangLib.POS_NN);
            return needAlter;
        };

        DNode originalParent = depTree.stream().parallel().filter(pred).findFirst().orElse(null);
        if (originalParent == null) {
            return;
        }


        DNode possessiveChild = originalParent.getChildren().stream().parallel().filter(x -> x.getLemma().equals("'s")).findFirst().orElse(null);
        if (possessiveChild == null) {
            return;
        }

        DNode nounChild = originalParent.getChildren().stream().parallel().filter(x -> x.getPOS().startsWith(LangLib.POS_NN)).findFirst().orElse(null);
        if (nounChild == null) {
            return;
        }

        DNode det = originalParent.getChildrenByDepLabels(LangLib.DEP_DET).stream().findFirst().orElse(null);
        if (det != null) {
            originalParent.removeChild(det.getId());
            nounChild.addChild(det);
            det.setHead(nounChild);
        }

        DNode originalGrandParent = originalParent.getHead();
        originalParent.removeChild(nounChild.getId());
        originalGrandParent.removeChild(originalParent.getId());

        // Swap DEP Labels.
        nounChild.setDepLabel(originalParent.getDepLabel());
        // Must be poss, dont use child's deplabel, it might be attr which is not accurate
        originalParent.setDepLabel(LangLib.DEP_POSS);

        nounChild.setHead(originalGrandParent);
        originalGrandParent.addChild(nounChild);

        originalParent.setHead(nounChild);
        nounChild.addChild(originalParent);
    }

    private static void setNamedEntity(DNode node, CoreLabel token) {
        if (token.ner() != null && !token.ner().equals("O")) {
            // Resolve Time
            if (token.ner().equalsIgnoreCase(LangLib.NE_TIME) || token.ner().equalsIgnoreCase(LangLib.NE_DATE)) {
                String normalizedTime = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                if (normalizedTime != null) {
                    node.setLemma(normalizedTime);
                } else {
                    LOG.warn("Time NamedEntity but doesn't has proper parsed time. " + token.originalText());
                }
            }
            node.setNamedEntity(token.ner());
        }
    }

}
