package mage.cards.m;

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
public final class MuzziosPreparations extends CardImpl {

    public MuzziosPreparations(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Each creature you control with the chosen name enters with an additional +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Each creature you control with the chosen name enters with an additional +1/+1 counter on it.")
        ));
    }

    private MuzziosPreparations(final MuzziosPreparations card) {
        super(card);
    }

    @Override
    public MuzziosPreparations copy() {
        return new MuzziosPreparations(this);
    }
}
