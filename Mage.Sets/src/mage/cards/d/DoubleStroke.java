package mage.cards.d;

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
public final class DoubleStroke extends CardImpl {

    public DoubleStroke(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // Hidden agenda (Start the game with this conspiracy face down in the command zone and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Hidden agenda <i>(Start the game with this conspiracy face down in the command zone "
                        + "and secretly choose a card name. You may turn this conspiracy face up any time and reveal that name.)</i>")
        ));

        // Whenever you cast an instant or sorcery spell with the chosen name, you may copy it. You may choose new targets for the copy.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("Whenever you cast an instant or sorcery spell with the chosen name, you may copy it. You may choose new targets for the copy.")
        ));
    }

    private DoubleStroke(final DoubleStroke card) {
        super(card);
    }

    @Override
    public DoubleStroke copy() {
        return new DoubleStroke(this);
    }
}
