package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ValiantTriggeredAbility;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class BetaRayBill extends CardImpl {

    private static final FilterPermanent godFilter = new FilterControlledPermanent(SubType.GOD, "God you control");

    public BetaRayBill(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G/W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Valiant — Whenever Beta Ray Bill becomes the target of a spell or ability you control for the first time each turn, put a +1/+1 counter on him and up to one target God you control.
        Ability ability = new ValiantTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance())
        );
        ability.addEffect(new AddCountersTargetEffect(CounterType.P1P1.createInstance())
                .setText("and up to one target God you control"));
        ability.addTarget(new TargetPermanent(0, 1, godFilter));
        this.addAbility(ability);
    }

    private BetaRayBill(final BetaRayBill card) {
        super(card);
    }

    @Override
    public BetaRayBill copy() {
        return new BetaRayBill(this);
    }
}
