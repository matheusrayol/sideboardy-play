package mage.cards.r;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
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
public final class RollerCoaster extends CardImpl {

    public RollerCoaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Creatures you control get +2/+0 until end of turn.
        this.addAbility(new RollerCoasterTriggeredAbility().withFlavorWord("Visit"));
    }

    private RollerCoaster(final RollerCoaster card) {
        super(card);
    }

    @Override
    public RollerCoaster copy() {
        return new RollerCoaster(this);
    }
}

class RollerCoasterTriggeredAbility extends TriggeredAbilityImpl {

    RollerCoasterTriggeredAbility() {
        super(Zone.BATTLEFIELD, new BoostControlledEffect(2, 0, Duration.EndOfTurn), false);
    }

    private RollerCoasterTriggeredAbility(final RollerCoasterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public RollerCoasterTriggeredAbility copy() {
        return new RollerCoasterTriggeredAbility(this);
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
        return "Creatures you control get +2/+0 until end of turn.";
    }
}
