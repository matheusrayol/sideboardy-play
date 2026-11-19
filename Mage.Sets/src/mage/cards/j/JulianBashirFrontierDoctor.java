package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.DrawNthCardTriggeredAbility;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class JulianBashirFrontierDoctor extends CardImpl {

    public JulianBashirFrontierDoctor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Seek out — Whenever you draw your second card each turn, put a +1/+1 counter on Julian Bashir.
        this.addAbility(new DrawNthCardTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                false, 2
        ).withFlavorWord("Seek out"));

        // Whenever Julian Bashir attacks, return target creature card with mana value X or less from your graveyard to the battlefield, where X is Julian Bashir's power.
        Ability ability = new AttacksTriggeredAbility(
                new ReturnFromGraveyardToBattlefieldTargetEffect()
                        .setText("return target creature card with mana value X or less from your graveyard to the battlefield, where X is {this}'s power"),
                false
        );
        ability.setTargetAdjuster(JulianBashirTargetAdjuster.instance);
        this.addAbility(ability);
    }

    private JulianBashirFrontierDoctor(final JulianBashirFrontierDoctor card) {
        super(card);
    }

    @Override
    public JulianBashirFrontierDoctor copy() {
        return new JulianBashirFrontierDoctor(this);
    }
}

enum JulianBashirTargetAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        Permanent sourcePermanent = ability.getSourcePermanentOrLKI(game);
        int maxMv = sourcePermanent != null ? sourcePermanent.getPower().getValue() : 0;
        FilterCard filter = new FilterCreatureCard("creature card with mana value " + maxMv + " or less from your graveyard");
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, maxMv + 1));
        ability.addTarget(new TargetCardInYourGraveyard(filter));
    }
}
