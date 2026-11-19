package mage.cards.w;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.ruleModifying.CombatDamageByToughnessControlledEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class WeightAdvantage extends CardImpl {

    public WeightAdvantage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CONSPIRACY}, "");

        // (Start the game with this conspiracy face up in the command zone.)
        this.addAbility(new SimpleStaticAbility(
                Zone.COMMAND,
                new InfoEffect("(Start the game with this conspiracy face up in the command zone.)")
        ));

        // Each creature you control assigns combat damage equal to its toughness rather than its power.
        this.addAbility(new SimpleStaticAbility(Zone.COMMAND, new CombatDamageByToughnessControlledEffect()));
    }

    private WeightAdvantage(final WeightAdvantage card) {
        super(card);
    }

    @Override
    public WeightAdvantage copy() {
        return new WeightAdvantage(this);
    }
}
