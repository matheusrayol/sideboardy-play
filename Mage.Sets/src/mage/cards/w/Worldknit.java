package mage.cards.w;

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
public final class Worldknit extends CardImpl {

    public Worldknit(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // As long as every card in your card pool started the game in your library or in the command zone, lands you control have "{T}: Add one mana of any color."
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "As long as every card in your card pool started the game in your library or in the command zone, "
                        + "lands you control have \"{T}: Add one mana of any color.\"")
        ));
    }

    private Worldknit(final Worldknit card) {
        super(card);
    }

    @Override
    public Worldknit copy() {
        return new Worldknit(this);
    }
}
