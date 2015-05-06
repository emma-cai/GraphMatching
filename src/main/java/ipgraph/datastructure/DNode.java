package ipgraph.datastructure;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * This follows CoNLL-U shared task: Multi-Lingual Dependency Parsing Format
 * http://universaldependencies.github.io/docs/format.html
 * <p>
 * Created by Maochen on 12/8/14.
 */
public class DNode {
    private int id;
    private String form;
    private String lemma;
    private String cPOSTag;
    private String pos;
    private Map<String, String> feats = new HashMap<>();
    private DNode head;
    private String depLabel;
    private int level;
    // Still considering item 9 and 10.

    // Key - id
    private Map<Integer, DNode> children = new HashMap<>();
    // Parent Node, Semantic Head Label
    private Map<DNode, String> semanticHeads = new HashMap<>();
    private DTree tree = null; // Refs to the whole dependency tree
    private static final String NAMED_ENTITY_KEY = "named_entity";

    public DNode() {
        id = 0;
        form = StringUtils.EMPTY;
        lemma = StringUtils.EMPTY;
        cPOSTag = StringUtils.EMPTY;
        pos = StringUtils.EMPTY;
        head = null;
        depLabel = StringUtils.EMPTY;
        level = 0;
    }

    public DNode(int id, String form, String lemma, String cPOSTag,
                 String pos, String depLabel) {
        this();
        this.id = id;
        this.form = form;
        this.lemma = lemma;
        this.cPOSTag = cPOSTag;
        this.pos = pos;
        this.depLabel = depLabel;
    }

    public DNode(int id, String form, String lemma, String cPOSTag,
                 String pos, String depLabel, int level) {
        this();
        this.id = id;
        this.form = form;
        this.lemma = lemma;
        this.cPOSTag = cPOSTag;
        this.pos = pos;
        this.depLabel = depLabel;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getcPOSTag() {
        return cPOSTag;
    }

    public void setcPOSTag(String cPOSTag) {
        this.cPOSTag = cPOSTag;
    }

    public String getPOS() {
        return pos;
    }

    public void setPOS(String pos) {
        this.pos = pos;
    }

    public String getDepLabel() {
        return depLabel;
    }

    public void setDepLabel(String depLabel) {
        this.depLabel = depLabel;
    }

    public DNode getHead() {
        return head;
    }

    public void setHead(DNode head) {
        this.head = head;
    }

    public int getLevel() { return level; };

    public void setLevel(int level) { this.level = level; }

    public List<DNode> getChildren() {
        return children.values().stream().collect(Collectors.toList());
    }

    public void addChild(DNode child) {
        this.children.put(child.getId(), child);
    }

    public void removeChild(int id) {
        children.remove(id);
    }

    public void addFeature(String key, String value) {
        feats.put(key, value);
    }

    public String getFeature(String key) {
        return feats.get(key);
    }

    public void removeFeature(String key) {
        feats.remove(key);
    }

    public List<DNode> getChildrenByDepLabels(final String... labels) {
        return children.values().stream().parallel().filter(x -> Arrays.asList(labels).contains(x.getDepLabel())).collect(Collectors.toList());
    }

    public String getNamedEntity() {
        return feats.get(NAMED_ENTITY_KEY) == null ? StringUtils.EMPTY : feats.get(NAMED_ENTITY_KEY);
    }

    public void setNamedEntity(String namedEntity) {
        if (namedEntity != null) {
            feats.put(NAMED_ENTITY_KEY, namedEntity);
        }
    }

    public boolean isRoot() {
        return this.depLabel.equals(LangLib.DEP_ROOT);
    }

    public void addSemanticHead(DNode parent, String label) {
        semanticHeads.put(parent, label);
    }

    public Map<DNode, String> getSemanticHeads() {
        return semanticHeads;
    }

    public DTree getTree() {
        return tree;
    }

    public void setTree(DTree tree) {
        this.tree = tree;
    }

    // This is CoNLL-U format.
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id).append("\t");
        builder.append(form).append("\t");
        builder.append(lemma).append("\t");

        // For now, cPOSTag is not differentiate with the POS.
        if (cPOSTag.isEmpty()) {
            builder.append(pos).append("\t");
        } else {
            builder.append(cPOSTag).append("\t");
        }

        builder.append(pos).append("\t");
        if (feats.isEmpty()) {
            builder.append("_").append("\t");
        } else {
            builder.append(feats).append("\t");
        }

        if (head != null) {
            builder.append(head.id).append("\t");
        } else {
            builder.append("NULL").append("\t");
        }

        builder.append(depLabel).append("\t");

        // These two corresponds to the 9 and 10 in the standard.
        builder.append("_").append("\t");
        builder.append("_").append("\t");
        return builder.toString().trim();
    }
}
