package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SpinneretteArachnobat extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(
            SubType.ATTRACTION, "three or more Attractions"
    );
    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(
            filter, ComparisonType.MORE_THAN, 2
    );

    public SpinneretteArachnobat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPIDER);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // When Spinnerette enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new SpinneretteAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // As long as you control three or more Attractions, Spinnerette gets +2/+0 and has menace.
        Ability ability = new SimpleStaticAbility(new ConditionalContinuousEffect(
                new BoostSourceEffect(2, 0, Duration.WhileOnBattlefield),
                condition,
                "As long as you control three or more Attractions, {this} gets +2/+0"
        ));
        ability.addEffect(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(new MenaceAbility(false), Duration.WhileOnBattlefield),
                condition,
                "and has menace"
        ));
        this.addAbility(ability);
    }

    private SpinneretteArachnobat(final SpinneretteArachnobat card) {
        super(card);
    }

    @Override
    public SpinneretteArachnobat copy() {
        return new SpinneretteArachnobat(this);
    }
}

class SpinneretteAttractionToken extends TokenImpl {

    SpinneretteAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private SpinneretteAttractionToken(final SpinneretteAttractionToken token) {
        super(token);
    }

    @Override
    public SpinneretteAttractionToken copy() {
        return new SpinneretteAttractionToken(this);
    }
}
