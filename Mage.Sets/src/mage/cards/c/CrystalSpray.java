package mage.cards.c;

import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MindBend;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.target.common.TargetSpellOrPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class CrystalSpray extends CardImpl {

    public CrystalSpray(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{U}");

        // Change the text of target spell or permanent by replacing all instances of one color word with another or one basic land type with another until end of turn.
        this.getSpellAbility().addEffect(new MindBend.ColorOrLandTypeChangeEffect(
                Duration.EndOfTurn,
                "change the text of target spell or permanent by replacing all instances of one color word with another "
                        + "or one basic land type with another until end of turn"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());

        // Draw a card.
        this.getSpellAbility().addEffect(new DrawCardSourceControllerEffect(1).concatBy("<br>"));
    }

    private CrystalSpray(final CrystalSpray card) {
        super(card);
    }

    @Override
    public CrystalSpray copy() {
        return new CrystalSpray(this);
    }
}
