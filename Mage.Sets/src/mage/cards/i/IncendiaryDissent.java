package mage.cards.i;

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
public final class IncendiaryDissent extends CardImpl {

    public IncendiaryDissent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Creatures you control with the chosen name have "{R}: This creature gets +1/+0 until end of turn."
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Creatures you control with the chosen name have \"{R}: This creature gets +1/+0 until end of turn.\"")
        ));
    }

    private IncendiaryDissent(final IncendiaryDissent card) {
        super(card);
    }

    @Override
    public IncendiaryDissent copy() {
        return new IncendiaryDissent(this);
    }
}
