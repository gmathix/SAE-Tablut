package testutil;

import boardifier.model.Model;
import boardifier.model.GameStageModel;
import boardifier.view.View;
import boardifier.view.GameStageView;
import model.Pawn;
import model.TablutBoard;
import model.TablutStageModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class TablutTestUtils {
    private TablutTestUtils() {}

    public static Model newModelWithPlayers() {
        Model model = new Model();
        model.addHumanPlayer("Swedish");
        model.addHumanPlayer("Moscovite");
        return model;
    }

    public static TablutStageModel newStage(Model model) {
        TablutStageModel stage = new TablutStageModel("tablut", model);
        model.setGameStage(stage);
        return stage;
    }

    public static TablutStageModel newStageWithBoard(Model model, int ruleSet, int idPlayer) {
        TablutStageModel stage = newStage(model);
        stage.setRuleSet(ruleSet);
        model.setIdPlayer(idPlayer);
        TablutBoard board = new TablutBoard(0, 0, stage);
        stage.setBoard(board);
        return stage;
    }

    public static Pawn placePawn(TablutBoard board, TablutStageModel stage, int number, int color, int x, int y) {
        Pawn pawn = new Pawn(number, color, stage);
        pawn.setBoardX(x);
        pawn.setBoardY(y);
        board.addElement(pawn, y, x);
        return pawn;
    }


    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
