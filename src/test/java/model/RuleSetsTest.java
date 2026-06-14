package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleSetsTest {

    @Test
    void campSquareChecksCoverLowHighAndFalseCases() {
        assertTrue(RuleSets.isCampSquare(3), "Low-bit camp square should be detected");
        assertTrue(RuleSets.isCampSquare(67), "High-bit camp square should be detected");
        assertFalse(RuleSets.isCampSquare(10), "Non-camp square should not be detected");
    }

    @Test
    void ruleBitChecksRespectIndividualFlags() {
        int ruleSet = RuleSets.RULESET_NORMAL
                | RuleSets.RULESET_CONSTRAINED_KING_SQUARES
                | RuleSets.RULESET_CORNER_KING_ESCAPES;

        assertTrue(RuleSets.isNormal(ruleSet));
        assertTrue(RuleSets.isConstrainedKingSquares(ruleSet));
        assertFalse(RuleSets.isConstrainedKingMoves(ruleSet));
        assertTrue(RuleSets.isCornerKingEscapes(ruleSet));
        assertFalse(RuleSets.isAshtonRules(ruleSet));
    }
}
