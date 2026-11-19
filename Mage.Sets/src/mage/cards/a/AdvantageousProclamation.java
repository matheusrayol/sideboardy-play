package mage.cards.a;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class AdvantageousProclamation extends CardImpl {

    public AdvantageousProclamation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // Your minimum deck size is reduced by five.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "Your minimum deck size is reduced by five.")
        ));
    }

    private AdvantageousProclamation(final AdvantageousProclamation card) {
        super(card);
    }

    @Override
    public AdvantageousProclamation copy() {
        return new AdvantageousProclamation(this);
    }
}
