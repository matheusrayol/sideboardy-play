package mage.cards.s;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.common.FilterControlledLandPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SovereignsRealm extends CardImpl {

    private static final FilterControlledLandPermanent filter = new FilterControlledLandPermanent("basic lands you control");

    static {
        filter.add(SuperType.BASIC.getPredicate());
    }

    public SovereignsRealm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        // Your starting deck can't have basic land cards and your starting hand size is five.
        // Exile a card from your hand: This turn, you may play basic lands from outside the game.
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.) "
                        + "Your starting deck can't have basic land cards and your starting hand size is five. "
                        + "Exile a card from your hand: This turn, you may play basic lands from outside the game - not implemented.")
        ));

        // Basic lands you control have "{T}: Add one mana of any color."
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new GainAbilityControlledEffect(new AnyColorManaAbility(), Duration.WhileOnBattlefield, filter)
        ));
    }

    private SovereignsRealm(final SovereignsRealm card) {
        super(card);
    }

    @Override
    public SovereignsRealm copy() {
        return new SovereignsRealm(this);
    }
}
