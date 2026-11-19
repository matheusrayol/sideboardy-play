package mage.cards.c;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.ClownRobotToken;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ComplaintsClerk extends CardImpl {

    public ComplaintsClerk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}");

        this.subtype.add(SubType.SLOTH);
        this.subtype.add(SubType.BEAST);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new ComplaintsClerkAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // Whenever you roll a 1, create a 1/1 white Clown Robot artifact creature token.
        this.addAbility(new ComplaintsClerkTriggeredAbility());
    }

    private ComplaintsClerk(final ComplaintsClerk card) {
        super(card);
    }

    @Override
    public ComplaintsClerk copy() {
        return new ComplaintsClerk(this);
    }
}

class ComplaintsClerkTriggeredAbility extends TriggeredAbilityImpl {

    ComplaintsClerkTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new ClownRobotToken()), false);
    }

    private ComplaintsClerkTriggeredAbility(final ComplaintsClerkTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ComplaintsClerkTriggeredAbility copy() {
        return new ComplaintsClerkTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        return drEvent.getRollDieType() == RollDieType.NUMERICAL && drEvent.getResult() == 1;
    }

    @Override
    public String getRule() {
        return "Whenever you roll a 1, create a 1/1 white Clown Robot artifact creature token.";
    }
}

class ComplaintsClerkAttractionToken extends TokenImpl {

    ComplaintsClerkAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private ComplaintsClerkAttractionToken(final ComplaintsClerkAttractionToken token) {
        super(token);
    }

    @Override
    public ComplaintsClerkAttractionToken copy() {
        return new ComplaintsClerkAttractionToken(this);
    }
}
