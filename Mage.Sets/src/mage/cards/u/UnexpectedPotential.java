package mage.cards.u;

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
public final class UnexpectedPotential extends CardImpl {

    public UnexpectedPotential(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // You may spend mana as though it were mana of any color to cast spells with the chosen name.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("You may spend mana as though it were mana of any color to cast spells with the chosen name.")
        ));
    }

    private UnexpectedPotential(final UnexpectedPotential card) {
        super(card);
    }

    @Override
    public UnexpectedPotential copy() {
        return new UnexpectedPotential(this);
    }
}
