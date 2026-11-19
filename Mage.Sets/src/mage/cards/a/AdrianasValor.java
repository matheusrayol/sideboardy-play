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
public final class AdrianasValor extends CardImpl {

    public AdrianasValor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Whenever a creature you control with the chosen name attacks, you may pay {W}. If you do, that creature gains indestructible until end of turn.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever a creature you control with the chosen name attacks, you may pay {W}. "
                        + "If you do, that creature gains indestructible until end of turn.")
        ));
    }

    private AdrianasValor(final AdrianasValor card) {
        super(card);
    }

    @Override
    public AdrianasValor copy() {
        return new AdrianasValor(this);
    }
}
