package model;

import boardifier.model.Model;
import boardifier.view.GameStageView;
import control.TablutController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.TablutTestUtils;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TablutBoardTest {

    private Model model;
    private TablutStageModel stage;
    private TablutBoard board;

    @BeforeEach
    void setUp() {
        model = TablutTestUtils.newModelWithPlayers();
        stage = TablutTestUtils.newStage(model);
        board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
    }

    @Test
    void getStringRepresentationUsesPieceCodes() {
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 0, 0);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 0, 1);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_KING, 0, 2);

        String rep = board.getStringRepresentation();

        assertEquals('M', rep.charAt(0));
        assertEquals('S', rep.charAt(9));
        assertEquals('K', rep.charAt(18));
        assertEquals(81, rep.length());
    }

    @Test
    void computeValidCellsStopsAtOccupiedSquaresAndTheCenter() {
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        Pawn pawn = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 2, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_MOSCOVITE, 5, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 2, 2);

        List<Point> cells = board.computeValidCells(pawn.getNumber());

        assertTrue(cells.contains(new Point(3, 4)));
        assertFalse(cells.contains(new Point(4, 4)), "The center square must remain forbidden");
        assertFalse(cells.contains(new Point(6, 4)), "The occupied square should stop the ray");
    }

    @Test
    void computeValidCellsLimitsTheKingToFourSquaresWhenConfigured() {
        stage.setRuleSet(RuleSets.RULESET_CONSTRAINED_KING_MOVES);
        Pawn king = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 2, 2);

        List<Point> cells = board.computeValidCells(king.getNumber());

        assertTrue(cells.contains(new Point(2, 6)), "The king should be able to move four squares");
        assertFalse(cells.contains(new Point(2, 7)), "The king should not move more than four squares");
    }

    @Test
    void computeValidCellsBlocksKingLandingOnConstrainedSquares() {
        stage.setRuleSet(RuleSets.RULESET_CONSTRAINED_KING_SQUARES);
        Pawn king = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_KING, 3, 1);

        List<Point> cells = board.computeValidCells(king.getNumber());

        assertFalse(cells.contains(new Point(3, 0)), "The king should not be allowed to land on D1");
        assertTrue(cells.contains(new Point(3, 2)), "The king should still be able to move elsewhere");
    }

    @Test
    void computeValidCellsBlocksCampSquaresForNonCampMoscovitesAndAllowsThemFromCamp() {
        stage.setRuleSet(RuleSets.RULESET_ASHTON_RULES);

        Pawn outsideCamp = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 0, 6);
        List<Point> blocked = board.computeValidCells(outsideCamp.getNumber());
        assertTrue(blocked.contains(new Point(0, 5)));
        assertTrue(blocked.contains(new Point(0, 4)));
        assertFalse(blocked.contains(new Point(0, 3)), "A non-camp moscovite should stop before the camp square");

        board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        Pawn insideCamp = TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_MOSCOVITE, 0, 3);
        List<Point> allowed = board.computeValidCells(insideCamp.getNumber());
        assertTrue(allowed.contains(new Point(0, 4)), "A camp-starting moscovite can move to another camp square");
    }

    @Test
    void checkCapturesHandlesCenterAndCampAllySquares() {
        stage.setRuleSet(RuleSets.RULESET_ASHTON_RULES);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        List<Integer> centerCapture = board.checkCaptures(true, 2, 3, 4, 4);
        assertEquals(List.of(4 * 9 + 4), centerCapture, "The center square should count as an ally for capture");

        TablutBoard campBoard = new TablutBoard(0, 0, stage);
        stage.setBoard(campBoard);
        TablutTestUtils.placePawn(campBoard, stage, 4, Pawn.PAWN_MOSCOVITE, 6, 0);
        TablutTestUtils.placePawn(campBoard, stage, 5, Pawn.PAWN_SOLDIER, 4, 0);

        List<Integer> campCapture = campBoard.checkCaptures(true, 6, 5, 0, 0);
        assertEquals(List.of(0), campCapture, "Camp squares should count as ally squares under Ashton rules");
    }

    @Test
    void applyMoveUpdatesKingCoordinatesAndRemovesCapturedPawns() {
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        Pawn moving = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        Move move = new Move(board, 2, 4, 3, 4);
        board.applyMove(move);

        assertSame(moving, board.getElement(4, 3));
        assertNull(board.getElement(4, 4), "Captured pawn should be removed");
        assertEquals(3, moving.getBoardX());
        assertEquals(4, moving.getBoardY());
        assertEquals(1, move.getCaptures().size());
    }

    @Test
    void applyMoveUsesBoardLookupWhenMoveMetadataDoesNotStoreCapturedPawns() {
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        Pawn moving = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        Move move = new Move(board, 2, 4, 3, 4);
        TablutTestUtils.setPrivateField(move, "capturedPawns", new java.util.ArrayList<Pawn>());

        board.applyMove(move);

        assertSame(moving, board.getElement(4, 3));
        assertNull(board.getElement(4, 4));
        assertEquals(1, move.getCaptures().size());
    }

    @Test
    void undoMoveRestoresPiecesWhenCaptureMetadataIsAvailable() {
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        Pawn moving = TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        Pawn captured = TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        Move move = new Move(board, 2, 4, 3, 4);
        board.applyMove(move);

        TablutController control = mock(TablutController.class);
        when(control.getMapElementLook()).thenReturn(new HashMap<>());
        GameStageView gameStageView = mock(GameStageView.class);

        board.undoMove(control, gameStageView, model.getGameStage(), move);

        assertSame(moving, board.getElement(4, 2));
        assertSame(captured, board.getElement(4, 4));
        assertEquals(2, moving.getBoardX());
        assertEquals(4, moving.getBoardY());
    }

    @Test
    void undoMoveRecreatesCapturedPawnWhenMetadataIsMissing() {
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_MOSCOVITE, 2, 4);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 4, 4);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_MOSCOVITE, 5, 4);

        Move move = new Move(board, 2, 4, 3, 4);
        TablutTestUtils.setPrivateField(move, "capturedPawns", new java.util.ArrayList<Pawn>());
        board.applyMove(move);

        TablutController control = mock(TablutController.class);
        HashMap<boardifier.model.GameElement, boardifier.view.ElementLook> map = new HashMap<>();
        when(control.getMapElementLook()).thenReturn(map);
        GameStageView gameStageView = mock(GameStageView.class);

        board.undoMove(control, gameStageView, model.getGameStage(), move);

        assertEquals(1, map.size(), "The recreated pawn should be registered in the look map");
        assertEquals(Pawn.PAWN_SOLDIER, ((Pawn) board.getElement(4, 4)).getColor());
        verify(gameStageView).addLook(any());
    }
}
