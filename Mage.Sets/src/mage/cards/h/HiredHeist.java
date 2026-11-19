package mage.cards.h;

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
public final class HiredHeist extends CardImpl {

    public HiredHeist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Whenever a creature you control with the chosen name deals combat damage to a player, you may pay {U}. If you do, draw a card.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever a creature you control with the chosen name deals combat damage to a player, "
                        + "you may pay {U}. If you do, draw a card.")
        ));
    }

    private HiredHeist(final HiredHeist card) {
        super(card);
    }

    @Override
    public HiredHeist copy() {
        return new HiredHeist(this);
    }
}
