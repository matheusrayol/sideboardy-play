package mage.cards.r;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.AttacksCreatureYouControlTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.effects.mana.BasicManaEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class RainbowDash extends CardImpl {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent(
            "creature you control with flying and/or haste"
    );

    static {
        filter.add(Predicates.or(
                new AbilityPredicate(FlyingAbility.class),
                new AbilityPredicate(HasteAbility.class)
        ));
    }

    public RainbowDash(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PEGASUS);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Whenever a creature you control with flying and/or haste attacks, you get 20% cooler. (You start at 0% coolness.)
        this.addAbility(new AttacksCreatureYouControlTriggeredAbility(
                new GetTicketCountersControllerEffect(1)
                        .setText("you get 20% cooler. (You start at 0% coolness.)"),
                false,
                filter
        ));

        // Sonic Rainboom — {T}: If you're at least 100% cool, add {W}{U}{B}{R}{G}, draw a card, and reset your coolness.
        Ability ability = new SimpleActivatedAbility(
                new BasicManaEffect(new Mana(1, 1, 1, 1, 1, 0, 0, 0)),
                new TapSourceCost()
        );
        ability.addEffect(new DrawCardSourceControllerEffect(1).concatBy(","));
        this.addAbility(ability.withFlavorWord("Sonic Rainboom"));
    }

    private RainbowDash(final RainbowDash card) {
        super(card);
    }

    @Override
    public RainbowDash copy() {
        return new RainbowDash(this);
    }
}
