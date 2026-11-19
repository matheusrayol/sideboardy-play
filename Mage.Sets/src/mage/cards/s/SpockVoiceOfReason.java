package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.hint.ValueHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.EnteredThisTurnPredicate;
import mage.filter.predicate.permanent.TokenPredicate;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class SpockVoiceOfReason extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("nontoken permanent you control that entered this turn");

    static {
        filter.add(TokenPredicate.FALSE);
        filter.add(EnteredThisTurnPredicate.instance);
    }

    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(filter);

    public SpockVoiceOfReason(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VULCAN);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // {2}, {T}: Draw a card for each nontoken permanent you control that entered this turn.
        Ability ability = new SimpleActivatedAbility(
                new DrawCardSourceControllerEffect(xValue)
                        .setText("draw a card for each nontoken permanent you control that entered this turn"),
                new GenericManaCost(2)
        );
        ability.addCost(new TapSourceCost());
        ability.addHint(new ValueHint("Nontoken permanents you control that entered this turn", xValue));
        this.addAbility(ability);
    }

    private SpockVoiceOfReason(final SpockVoiceOfReason card) {
        super(card);
    }

    @Override
    public SpockVoiceOfReason copy() {
        return new SpockVoiceOfReason(this);
    }
}
