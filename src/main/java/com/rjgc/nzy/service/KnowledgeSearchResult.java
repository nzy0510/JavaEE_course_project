package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeAtom;

public class KnowledgeSearchResult {

    private final KnowledgeAtom atom;
    private final int score;

    public KnowledgeSearchResult(KnowledgeAtom atom, int score) {
        this.atom = atom;
        this.score = score;
    }

    public KnowledgeAtom getAtom() {
        return atom;
    }

    public int getScore() {
        return score;
    }
}
