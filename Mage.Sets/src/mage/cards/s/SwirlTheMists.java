package mage.cards.s;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SwirlTheMists extends CardImpl {

    public SwirlTheMists(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}{U}");

        // As this enchantment enters, choose a color word.
        this.addAbility(new AsEntersBattlefieldAbility(
                new ChooseColorEffect(Outcome.Neutral)
                        .setText("choose a color word")
        ));

        // All instances of color words in the text of spells and permanents are changed to the chosen color word.
        this.addAbility(new SimpleStaticAbility(new SwirlTheMistsEffect()));
    }

    private SwirlTheMists(final SwirlTheMists card) {
        super(card);
    }

    @Override
    public SwirlTheMists copy() {
        return new SwirlTheMists(this);
    }
}

class SwirlTheMistsEffect extends ContinuousEffectImpl {

    SwirlTheMistsEffect() {
        super(Duration.WhileOnBattlefield, Layer.TextChangingEffects_3, SubLayer.NA, Outcome.Neutral);
        this.staticText = "all instances of color words in the text of spells and permanents are changed to the chosen color word";
    }

    private SwirlTheMistsEffect(final SwirlTheMistsEffect effect) {
        super(effect);
    }

    @Override
    public SwirlTheMistsEffect copy() {
        return new SwirlTheMistsEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        ObjectColor chosenColor = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
        if (chosenColor == null) {
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents()) {
            for (ObjectColor color : ObjectColor.getAllColors()) {
                if (!color.equals(chosenColor)) {
                    SleightOfMind.applyColorWordChange(permanent, color, chosenColor);
                }
            }
        }
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell) {
                for (ObjectColor color : ObjectColor.getAllColors()) {
                    if (!color.equals(chosenColor)) {
                        SleightOfMind.applyColorWordChange(stackObject, color, chosenColor);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }
}
