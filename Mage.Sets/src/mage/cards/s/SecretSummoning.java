package mage.cards.s;

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
public final class SecretSummoning extends CardImpl {

    public SecretSummoning(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Whenever a creature you control with the chosen name enters, you may search your library for any number of cards with that name, reveal them, put them into your hand, then shuffle.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever a creature you control with the chosen name enters, you may search your library "
                        + "for any number of cards with that name, reveal them, put them into your hand, then shuffle.")
        ));
    }

    private SecretSummoning(final SecretSummoning card) {
        super(card);
    }

    @Override
    public SecretSummoning copy() {
        return new SecretSummoning(this);
    }
}
