package model;

import boardifier.model.Model;
import org.junit.jupiter.api.Test;
import testutil.TablutTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TablutStageModelTest {

    @Test
    void gettersAndSettersExposeStoredState() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = new TablutStageModel("tablut", model);
        TablutBoard board = new TablutBoard(0, 0, stage);

        stage.setMode(TablutStageModel.MODE_VIEW_GAME);
        stage.setState(TablutStageModel.STATE_SELECTDEST);
        stage.setRuleSet(RuleSets.RULESET_CORNER_KING_ESCAPES);
        stage.setWinMessage("won");
        stage.setBoard(board);

        assertEquals(TablutStageModel.MODE_VIEW_GAME, stage.getMode());
        assertEquals(TablutStageModel.STATE_SELECTDEST, stage.getState());
        assertEquals(RuleSets.RULESET_CORNER_KING_ESCAPES, stage.getRuleSet());
        assertEquals("won", stage.getWinMessage());
        assertSame(board, stage.getBoard());
        assertInstanceOf(TablutStageFactory.class, stage.getDefaultElementFactory());
    }

    @Test
    void computePartyResultSetsWinnerWhenKingReachesEdge() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setIdPlayer(0);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 0, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_MOSCOVITE, 8, 0);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_SOLDIER, 0, 0);

        board.setKingX(0);
        board.setKingY(4);

        stage.computePartyResult(0, 0);

//        TablutTestUtils.invokePrivateNoArgs(stage, "computePartyResult");

        assertEquals(0, model.getIdWinner());
        assertEquals("the king has reached an edge", stage.getWinMessage());
    }

    @Test
    void computePartyResultDetectsCornerEscapeUnderCornerRuleSet() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_CORNER_KING_ESCAPES);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setIdPlayer(0);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 0, 0);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_MOSCOVITE, 8, 8);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_SOLDIER, 0, 1);

        board.setKingX(0);
        board.setKingY(0);

        stage.computePartyResult(0, 0);

//        TablutTestUtils.invokePrivateNoArgs(stage, "computePartyResult");

        assertEquals(0, model.getIdWinner());
        assertEquals("the king has reached an edge", stage.getWinMessage());
    }

    @Test
    void computePartyResultDetectsEncirclementInNormalRules() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setIdPlayer(1);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 4, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_MOSCOVITE, 4, 3);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 4, 5);
        TablutTestUtils.placePawn(board, stage, 4, Pawn.PAWN_MOSCOVITE, 3, 4);
        TablutTestUtils.placePawn(board, stage, 5, Pawn.PAWN_MOSCOVITE, 5, 4);
        TablutTestUtils.placePawn(board, stage, 6, Pawn.PAWN_SOLDIER, 0, 0);
        TablutTestUtils.placePawn(board, stage, 7, Pawn.PAWN_MOSCOVITE, 8, 8);

        board.setKingX(4);
        board.setKingY(4);

        stage.computePartyResult(3, 4);

//        TablutTestUtils.invokePrivateNoArgs(stage, "computePartyResult");

        assertEquals(1, model.getIdWinner());
        assertEquals("the king has been encircled", stage.getWinMessage());
    }

    @Test
    void computePartyResultDetectsNoLegalMovesForSwedishSide() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setIdPlayer(0);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 0, 0);

        stage.computePartyResult(0, 0);

        assertEquals(1, model.getIdWinner(), "Moscovite should win when Swedish has no legal moves");
        assertEquals("Swedish has no legal moves", stage.getWinMessage());
    }

    @Test
    void computePartyResultDetectsNoLegalMovesForMoscoviteSide() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setIdPlayer(0);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 4, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 0, 0);

        stage.computePartyResult(0, 0);

        assertEquals(0, model.getIdWinner(), "Swedish should win when Moscovite has no legal moves");
        assertEquals("Moscovite has no legal moves", stage.getWinMessage());
    }
}
