package mage.cards.b;

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
public final class BackupPlan extends CardImpl {

    public BackupPlan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // Draw an additional hand of seven cards as the game begins. Before taking mulligans, shuffle all but one of your hands into your library.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "Draw an additional hand of seven cards as the game begins. Before taking mulligans, shuffle all but one of your hands into your library.")
        ));
    }

    private BackupPlan(final BackupPlan card) {
        super(card);
    }

    @Override
    public BackupPlan copy() {
        return new BackupPlan(this);
    }
}
