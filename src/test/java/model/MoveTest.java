package model;

import boardifier.model.Model;
import org.junit.jupiter.api.Test;
import testutil.TablutTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void moveFromStringIsCaseInsensitiveAndToStringRoundTrips() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 0, 0);

        Move move = Move.fromString(board, "a1b2");

        assertEquals(0, move.srcX());
        assertEquals(0, move.srcY());
        assertEquals(1, move.dstX());
        assertEquals(1, move.dstY());
        assertEquals("A1B2", move.toString());
    }

    @Test
    void moveConstructorCollectsCapturesAndReturnsImmutableCopies() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);

        Pawn moving = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        Pawn captured = TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        Pawn ally = TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        Move move = new Move(board, 2, 4, 3, 4);

        assertEquals(1, move.getCaptures().size(), "One capture should be recorded");
        assertEquals(1, move.getCapturedPawns().size(), "Captured pawn should be preserved");
        assertEquals(4, move.getCaptures().get(0).x());
        assertEquals(4, move.getCaptures().get(0).y());
        assertEquals(Pawn.PAWN_SOLDIER, move.getCaptures().get(0).piece());
        assertSame(captured, move.getCapturedPawns().get(0));
        assertThrows(UnsupportedOperationException.class, () -> move.getCaptures().add(null));
        assertThrows(UnsupportedOperationException.class, () -> move.getCapturedPawns().add(ally));
    }

    @Test
    void moveFromStringAndConstructorWorkTogetherWithNoCapture() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 0, 0);

        Move move = Move.fromString(board, "A1A2");

        assertEquals(List.of(), move.getCaptures());
        assertEquals(List.of(), move.getCapturedPawns());
    }
}
