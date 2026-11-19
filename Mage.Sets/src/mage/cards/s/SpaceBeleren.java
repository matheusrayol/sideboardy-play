package mage.cards.s;

import mage.abilities.LoyaltyAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.DestroyAllEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.combat.CantBeBlockedAllEffect;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SpaceBeleren extends CardImpl {

    public SpaceBeleren(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.JACE);
        this.setStartingLoyalty(3);

        // Space sculptor (Space Beleren divides the battlefield into alpha, beta, and gamma sectors. If a creature isn't assigned to a sector, its controller assigns it to one. Opponents assign first.)
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Space sculptor (Space Beleren divides the battlefield into alpha, beta, and gamma sectors. "
                        + "If a creature isn't assigned to a sector, its controller assigns it to one. Opponents assign first.)"
        )));

        // +1: Creatures in each sector can be blocked this turn only by creatures in the same sector.
        this.addAbility(new LoyaltyAbility(new CantBeBlockedAllEffect(
                StaticFilters.FILTER_PERMANENT_CREATURES, Duration.EndOfTurn
        ).setText("Creatures in each sector can be blocked this turn only by creatures in the same sector"), 1));

        // -1: Put a +1/+1 counter on each creature in the sector of your choice.
        this.addAbility(new LoyaltyAbility(new AddCountersAllEffect(
                CounterType.P1P1.createInstance(), StaticFilters.FILTER_CONTROLLED_CREATURE
        ).setText("Put a +1/+1 counter on each creature in the sector of your choice"), -1));

        // -5: Destroy all creatures in the sector of your choice.
        this.addAbility(new LoyaltyAbility(new DestroyAllEffect(
                StaticFilters.FILTER_PERMANENT_CREATURES
        ).setText("Destroy all creatures in the sector of your choice"), -5));
    }

    private SpaceBeleren(final SpaceBeleren card) {
        super(card);
    }

    @Override
    public SpaceBeleren copy() {
        return new SpaceBeleren(this);
    }
}
