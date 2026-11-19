package mage.cards.c;

import mage.abilities.TriggeredAbilityImpl;
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

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ClownExtruder extends CardImpl {

    public ClownExtruder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Create a 1/1 white Clown Robot artifact creature token.
        this.addAbility(new ClownExtruderTriggeredAbility().withFlavorWord("Visit"));
    }

    private ClownExtruder(final ClownExtruder card) {
        super(card);
    }

    @Override
    public ClownExtruder copy() {
        return new ClownExtruder(this);
    }
}

class ClownExtruderTriggeredAbility extends TriggeredAbilityImpl {

    ClownExtruderTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new ClownRobotToken()), false);
    }

    private ClownExtruderTriggeredAbility(final ClownExtruderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ClownExtruderTriggeredAbility copy() {
        return new ClownExtruderTriggeredAbility(this);
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
        if (drEvent.getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        int result = drEvent.getResult();
        return result == 2 || result == 6;
    }

    @Override
    public String getRule() {
        return "Create a 1/1 white Clown Robot artifact creature token.";
    }
}
