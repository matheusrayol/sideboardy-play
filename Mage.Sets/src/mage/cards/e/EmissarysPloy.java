package mage.cards.e;

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
public final class EmissarysPloy extends CardImpl {

    public EmissarysPloy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // Before drawing your opening hand, choose 1, 2, or 3.
        // You may spend mana as though it were mana of any color to cast creature spells with mana value equal to the chosen number.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "Before drawing your opening hand, choose 1, 2, or 3. "
                        + "You may spend mana as though it were mana of any color to cast creature spells with mana value equal to the chosen number.")
        ));
    }

    private EmissarysPloy(final EmissarysPloy card) {
        super(card);
    }

    @Override
    public EmissarysPloy copy() {
        return new EmissarysPloy(this);
    }
}
