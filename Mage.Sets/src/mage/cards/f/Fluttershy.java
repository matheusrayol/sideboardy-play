package mage.cards.f;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Fluttershy extends CardImpl {

    public Fluttershy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PEGASUS);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // {1}, {T}: Put a +1/+1 counter on each creature with a tail target player controls. Stare down up to one target creature until end of turn.
        Ability ability = new SimpleActivatedAbility(
                new AddCountersAllEffect(CounterType.P1P1.createInstance(), StaticFilters.FILTER_CONTROLLED_CREATURE)
                        .setText("Put a +1/+1 counter on each creature with a tail target player controls. "
                                + "Stare down up to one target creature until end of turn"),
                new ManaCostsImpl<>("{1}")
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);
    }

    private Fluttershy(final Fluttershy card) {
        super(card);
    }

    @Override
    public Fluttershy copy() {
        return new Fluttershy(this);
    }
}
