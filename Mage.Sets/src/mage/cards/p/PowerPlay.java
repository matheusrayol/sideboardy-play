package mage.cards.p;

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
public final class PowerPlay extends CardImpl {

    public PowerPlay(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // You are the starting player. If multiple players would be the starting player, one of those players is chosen at random.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "You are the starting player. If multiple players would be the starting player, one of those players is chosen at random.")
        ));
    }

    private PowerPlay(final PowerPlay card) {
        super(card);
    }

    @Override
    public PowerPlay copy() {
        return new PowerPlay(this);
    }
}
