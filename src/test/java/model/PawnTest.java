package model;

import boardifier.model.Model;
import boardifier.model.animation.Animation;
import boardifier.model.animation.AnimationStep;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testutil.TablutTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PawnTest {

    @Test
    void toStringUsesColorNameAndNumber() {
        Pawn moscovite = new Pawn(7, Pawn.PAWN_MOSCOVITE, new TablutStageModel("stage", new Model()));
        Pawn soldier = new Pawn(8, Pawn.PAWN_SOLDIER, new TablutStageModel("stage", new Model()));
        Pawn king = new Pawn(9, Pawn.PAWN_KING, new TablutStageModel("stage", new Model()));

        assertEquals("Pawn : moscovite, number 7\n", moscovite.toString());
        assertEquals("Pawn : soldier, number 8\n", soldier.toString());
        assertEquals("Pawn : king, number 9\n", king.toString());
    }

    @Test
    void updateDoesNothingWhenAnimationIsNull() {
        Pawn pawn = new Pawn(1, Pawn.PAWN_SOLDIER, new TablutStageModel("stage", new Model()));
        pawn.setLocation(12, 34);

        pawn.update();

        assertEquals(12, pawn.getX());
        assertEquals(34, pawn.getY());
        assertNull(pawn.getAnimation());
    }

    @Test
    void updateClearsAnimationWhenNextReturnsNull() {
        Pawn pawn = new Pawn(1, Pawn.PAWN_SOLDIER, new TablutStageModel("stage", new Model()));
        Animation animation = mock(Animation.class);
        when(animation.next()).thenReturn(null);
        pawn.setAnimation(animation);

        pawn.update();

        assertNull(pawn.getAnimation(), "Animation should be cleared when it ends");
        verify(animation).next();
    }

    @Test
    void updateIgnoresNoOpStepWithoutMoving() {
        Pawn pawn = new Pawn(1, Pawn.PAWN_SOLDIER, new TablutStageModel("stage", new Model()));
        pawn.setLocation(5, 6);
        Animation animation = mock(Animation.class);
        when(animation.next()).thenReturn(Animation.NOPStep);
        pawn.setAnimation(animation);

        pawn.update();

        assertSame(animation, pawn.getAnimation(), "NOP step should keep the animation active");
        assertEquals(5, pawn.getX());
        assertEquals(6, pawn.getY());
        verify(animation).next();
    }
}
