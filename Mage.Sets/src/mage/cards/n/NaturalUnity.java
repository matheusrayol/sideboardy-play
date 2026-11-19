package mage.cards.n;

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
public final class NaturalUnity extends CardImpl {

    public NaturalUnity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Creatures you control with the chosen name have "At the beginning of combat on your turn, you may pay {G}. If you do, put a +1/+1 counter on this creature."
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Creatures you control with the chosen name have \"At the beginning of combat on your turn, "
                        + "you may pay {G}. If you do, put a +1/+1 counter on this creature.\"")
        ));
    }

    private NaturalUnity(final NaturalUnity card) {
        super(card);
    }

    @Override
    public NaturalUnity copy() {
        return new NaturalUnity(this);
    }
}
