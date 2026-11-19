package mage.cards.s;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.AdditionalCombatPhaseEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
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
public final class SwingingShip extends CardImpl {

    public SwingingShip(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — After the first combat phase this turn, there's an additional combat phase. At the beginning of that combat, untap all creatures that attacked this turn.
        this.addAbility(new SwingingShipTriggeredAbility().withFlavorWord("Visit"));
    }

    private SwingingShip(final SwingingShip card) {
        super(card);
    }

    @Override
    public SwingingShip copy() {
        return new SwingingShip(this);
    }
}

class SwingingShipTriggeredAbility extends TriggeredAbilityImpl {

    SwingingShipTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AdditionalCombatPhaseEffect()
                .setText("After the first combat phase this turn, there's an additional combat phase. "
                        + "At the beginning of that combat, untap all creatures that attacked this turn"), false);
    }

    private SwingingShipTriggeredAbility(final SwingingShipTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SwingingShipTriggeredAbility copy() {
        return new SwingingShipTriggeredAbility(this);
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
        return "After the first combat phase this turn, there's an additional combat phase. "
                + "At the beginning of that combat, untap all creatures that attacked this turn.";
    }
}
