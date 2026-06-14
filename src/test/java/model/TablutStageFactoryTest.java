package model;

import boardifier.model.Model;
import org.junit.jupiter.api.Test;
import testutil.TablutTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TablutStageFactoryTest {

    @Test
    void setupBuildsTheWholeStageModel() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);

        new TablutStageFactory(stage).setup();

        assertNotNull(stage.getBoard());
        assertEquals("TABLUT", stage.getTitleText().getText());
        assertEquals("The king escapes, or the attackers win\n by encirclement.", stage.getSubtitleText().getText());
        assertEquals(16, stage.getMoscovitePawns().length);
        assertEquals(8, stage.getSoldierPawns().length);
        assertEquals(1, stage.getKingPawns().length);
        assertSame(stage, stage.getBoard().getGameStage());
        assertSame(stage.getKingPawns()[0], stage.getBoard().getElement(4, 4));
    }

    @Test
    void defaultElementFactoryReturnsFactoryInstance() {
        Model model = TablutTestUtils.newModelWithPlayers();
        TablutStageModel stage = TablutTestUtils.newStage(model);

        assertInstanceOf(TablutStageFactory.class, stage.getDefaultElementFactory());
    }
}
