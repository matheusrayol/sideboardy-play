package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.LoseLifeSourceControllerEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DiscourtesyClerk extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(
            SubType.ATTRACTION, "you control three or more Attractions"
    );
    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(
            filter, ComparisonType.MORE_THAN, 2
    );

    public DiscourtesyClerk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new DiscourtesyClerkAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // At the beginning of your end step, if you control three or more Attractions, you draw a card and you lose 1 life.
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                TargetController.YOU, new DrawCardSourceControllerEffect(1), false, condition
        );
        ability.addEffect(new LoseLifeSourceControllerEffect(1).concatBy("and"));
        this.addAbility(ability);
    }

    private DiscourtesyClerk(final DiscourtesyClerk card) {
        super(card);
    }

    @Override
    public DiscourtesyClerk copy() {
        return new DiscourtesyClerk(this);
    }
}

class DiscourtesyClerkAttractionToken extends TokenImpl {

    DiscourtesyClerkAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private DiscourtesyClerkAttractionToken(final DiscourtesyClerkAttractionToken token) {
        super(token);
    }

    @Override
    public DiscourtesyClerkAttractionToken copy() {
        return new DiscourtesyClerkAttractionToken(this);
    }
}
