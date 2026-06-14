package control;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.GameStageView;
import boardifier.view.View;
import model.Move;
import model.Pawn;
import model.RuleSets;
import model.TablutBoard;
import model.TablutStageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testutil.TablutTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoveHistoryTest {

    @Test
    void addUndoAndSettersWorkAsExpected() {
        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class));

        history.setSwedishPlayer("Alice");
        history.setMoscovitePlayer("Bob");
        history.setStartingSide(1);
        history.setWinningSide(0);
        history.setRuleSet(42);

        assertEquals("Alice", history.getSwedishPlayer());
        assertEquals("Bob", history.getMoscovitePlayer());
        assertEquals(1, history.getStartingSide());
        assertEquals(0, history.getWinningSide());
        assertEquals(42, history.getRuleSet());

        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 0, 0);

        Move move = new Move(board, 0, 0, 0, 1);
        history.addMove(move);
        assertEquals(1, history.getMoves().size());

        history.undoLastMove();
        assertTrue(history.getMoves().isEmpty());

        history.addMove(move);
        history.addMove(move);
        history.undoLastMoves(2);
        assertTrue(history.getMoves().isEmpty());
    }

    @Test
    void undoLastMoveAndUndoLastMovesThrowWhenHistoryIsTooShort() {
        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class));

        assertThrows(java.util.NoSuchElementException.class, history::undoLastMove);
        assertThrows(java.util.NoSuchElementException.class, () -> history.undoLastMoves(1));
    }

    @Test
    void buildGameStringUsesHeaderAndResultFormatting() {
        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class),
                "Swedish", "Moscovite", 0, RuleSets.RULESET_NORMAL);
        history.setWinningSide(0);

        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 0, 0);
        Move move = new Move(board, 0, 0, 0, 1);
        history.addMove(move);

        String gameString = history.buildGameString();

        assertTrue(gameString.contains("[SwedishPlayer \"Swedish\"]"));
        assertTrue(gameString.contains("[MoscovitePlayer \"Moscovite\"]"));
        assertTrue(gameString.contains("[Result \"1-0\"]"));
        assertTrue(gameString.contains("1. A1A2 "));
        assertTrue(gameString.endsWith("1-0\n"));
    }

    @Test
    void buildGameStringUsesDefaultLossResultWhenWinnerIsNotZero() {
        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class));
        String gameString = history.buildGameString();

        assertTrue(gameString.contains("[Result \"0-1\"]"));
        assertTrue(gameString.endsWith("0-1\n"));
    }

    @Test
    void parseGameFileHeaderReadsKnownFieldsAndIgnoresUnknownFields(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("game.pgn");
        Files.writeString(file, """
                [SwedishPlayer "Anna"]
                [MoscovitePlayer "Bertil"]
                [StartingSide "1"]
                [Ruleset "8"]
                [Result "1-0"]
                [Unknown "Ignored"]
                """);

        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class));
        history.parseGameFileHeader(file.toString());

        assertEquals("Anna", history.getSwedishPlayer());
        assertEquals("Bertil", history.getMoscovitePlayer());
        assertEquals(1, history.getStartingSide());
        assertEquals(8, history.getRuleSet());
        assertEquals(0, history.getWinningSide());
    }

    @Test
    void parseGameFileHeaderFallsBackToDefaultsWhenHeaderIsMissing(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("game.pgn");
        Files.writeString(file, "This file has no PGN headers\n");

        MoveHistory history = new MoveHistory(mock(Controller.class), mock(Model.class), mock(View.class));
        history.parseGameFileHeader(file.toString());

        assertEquals("Player 1", history.getSwedishPlayer());
        assertEquals("Player 2", history.getMoscovitePlayer());
        assertEquals(0, history.getStartingSide());
        assertEquals(1, history.getRuleSet());
        assertEquals(0, history.getWinningSide());
    }

    @Test
    void parseGameFileMovesReplaysAndThenRewindsTheGame(@TempDir Path tempDir) throws IOException {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);
        stage.setRuleSet(RuleSets.RULESET_NORMAL);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        model.setGameStage(stage);

        TablutTestUtils.placePawn(board, stage, 1, Pawn.PAWN_SOLDIER, 0, 0);
        TablutTestUtils.placePawn(board, stage, 2, Pawn.PAWN_SOLDIER, 1, 0);
        TablutTestUtils.placePawn(board, stage, 3, Pawn.PAWN_SOLDIER, 2, 0);

        String initial = board.getStringRepresentation();

        Path file = tempDir.resolve("game.pgn");
        Files.writeString(file, """
                [SwedishPlayer "Anna"]
                [MoscovitePlayer "Bertil"]
                [StartingSide "0"]
                [Ruleset "1"]
                [Result "1-0"]
                1. A1A2 B1B2
                2. C1C2
                1-0
                """);

        TablutController controller = mock(TablutController.class);
        View view = mock(View.class);
        GameStageView gameStageView = mock(GameStageView.class);
        when(view.getGameStageView()).thenReturn(gameStageView);

        MoveHistory history = new MoveHistory(controller, model, view);
        ListIterator<Move> iterator = history.getMoves().listIterator();
        when(controller.getMoveHistoryIterator()).thenReturn(iterator);

        history.parseGameFileHeader(file.toString());
        history.parseGameFileMoves(file.toString());

        assertEquals(3, history.getMoves().size());
        assertEquals(initial, board.getStringRepresentation(), "Board should be restored after replay");
        assertFalse(iterator.hasPrevious(), "Iterator should be rewound to the beginning");
        assertTrue(iterator.hasNext(), "Iterator should be ready for forward replay again");
        assertEquals(0, history.getWinningSide());
    }
}
