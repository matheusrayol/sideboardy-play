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
public final class SummonersBond extends CardImpl {

    public SummonersBond(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Double agenda (Start the game with this conspiracy face down in the command zone and secretly choose two different card names. You may turn this conspiracy face up any time and reveal those names.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Double agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose two different card names. You may turn this conspiracy face up any time and reveal those names.)</i>")
        ));

        // Whenever you cast a creature spell with one of the chosen names, you may search your library for a creature card with the other chosen name, reveal it, put it into your hand, then shuffle.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever you cast a creature spell with one of the chosen names, you may search your library "
                        + "for a creature card with the other chosen name, reveal it, put it into your hand, then shuffle.")
        ));
    }

    private SummonersBond(final SummonersBond card) {
        super(card);
    }

    @Override
    public SummonersBond copy() {
        return new SummonersBond(this);
    }
}
