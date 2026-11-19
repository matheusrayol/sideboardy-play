package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.combat.MustBeBlockedByAtLeastOneTargetEffect;
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
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BumperCars extends CardImpl {

    public BumperCars(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Target creature must be blocked this turn if able.
        Ability ability = new BumperCarsTriggeredAbility();
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private BumperCars(final BumperCars card) {
        super(card);
    }

    @Override
    public BumperCars copy() {
        return new BumperCars(this);
    }
}

class BumperCarsTriggeredAbility extends TriggeredAbilityImpl {

    BumperCarsTriggeredAbility() {
        super(Zone.BATTLEFIELD, new MustBeBlockedByAtLeastOneTargetEffect(Duration.EndOfTurn), false);
    }

    private BumperCarsTriggeredAbility(final BumperCarsTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public BumperCarsTriggeredAbility copy() {
        return new BumperCarsTriggeredAbility(this);
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
        return result == 2 || result == 3 || result == 6;
    }

    @Override
    public String getRule() {
        return "Target creature must be blocked this turn if able.";
    }
}
