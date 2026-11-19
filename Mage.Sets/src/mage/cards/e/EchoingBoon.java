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
public final class EchoingBoon extends CardImpl {

    public EchoingBoon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Whenever you cast an instant or sorcery spell, if it targets a creature you control with the chosen name, you may copy that spell and may choose new targets for the copy.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever you cast an instant or sorcery spell, if it targets a creature you control "
                        + "with the chosen name, you may copy that spell and may choose new targets for the copy.")
        ));
    }

    private EchoingBoon(final EchoingBoon card) {
        super(card);
    }

    @Override
    public EchoingBoon copy() {
        return new EchoingBoon(this);
    }
}
