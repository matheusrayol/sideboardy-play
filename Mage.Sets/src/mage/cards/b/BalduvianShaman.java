package mage.cards.b;

import mage.MageInt;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.CumulativeUpkeepAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.s.SleightOfMind;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.common.FilterControlledEnchantmentPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class BalduvianShaman extends CardImpl {

    private static final FilterControlledEnchantmentPermanent filter = new FilterControlledEnchantmentPermanent(
            "white enchantment you control that doesn't have cumulative upkeep"
    );

    static {
        filter.add(new ColorPredicate(ObjectColor.WHITE));
        filter.add(Predicates.not(new AbilityPredicate(CumulativeUpkeepAbility.class)));
    }

    public BalduvianShaman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Change the text of target white enchantment you control that doesn't have cumulative upkeep by replacing all instances of one color word with another. That enchantment gains "Cumulative upkeep {1}."
        Ability ability = new SimpleActivatedAbility(
                new SleightOfMind.ColorWordChangeEffect(
                        Duration.Custom,
                        "change the text of target white enchantment you control that doesn't have cumulative upkeep "
                                + "by replacing all instances of one color word with another"
                ),
                new TapSourceCost()
        );
        ability.addEffect(new GainAbilityTargetEffect(
                new CumulativeUpkeepAbility(new GenericManaCost(1)),
                Duration.Custom,
                "That enchantment gains \"Cumulative upkeep {1}.\""
        ));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private BalduvianShaman(final BalduvianShaman card) {
        super(card);
    }

    @Override
    public BalduvianShaman copy() {
        return new BalduvianShaman(this);
    }
}
