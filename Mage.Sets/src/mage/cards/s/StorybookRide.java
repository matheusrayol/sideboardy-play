package mage.cards.s;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.ExileTopXMayPlayUntilEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class StorybookRide extends CardImpl {

    public StorybookRide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Exile the top X cards of your library, where X is the number of Attractions you've visited this turn (including this one). You may play those cards this turn. At the beginning of the next end step, if any of those cards remain exiled, put them on the bottom of your library in any order.
        this.addAbility(new StorybookRideTriggeredAbility().withFlavorWord("Visit"));
    }

    private StorybookRide(final StorybookRide card) {
        super(card);
    }

    @Override
    public StorybookRide copy() {
        return new StorybookRide(this);
    }
}

class StorybookRideTriggeredAbility extends TriggeredAbilityImpl {

    StorybookRideTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ExileTopXMayPlayUntilEffect(1, Duration.EndOfTurn)
                .setText("Exile the top X cards of your library, where X is the number of Attractions you've visited this turn (including this one). "
                        + "You may play those cards this turn. At the beginning of the next end step, if any of those cards remain exiled, put them on the bottom of your library in any order"), false);
    }

    private StorybookRideTriggeredAbility(final StorybookRideTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public StorybookRideTriggeredAbility copy() {
        return new StorybookRideTriggeredAbility(this);
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
        return result == 2 || result == 5 || result == 6;
    }

    @Override
    public String getRule() {
        return "Exile the top X cards of your library, where X is the number of Attractions you've visited this turn (including this one). "
                + "You may play those cards this turn. At the beginning of the next end step, if any of those cards remain exiled, put them on the bottom of your library in any order.";
    }
}
