package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.PhaseOutTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
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
public final class FerrisWheel extends CardImpl {

    public FerrisWheel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Choose target creature that hasn't been phased out with this Attraction. That creature phases out until you roll a 3 or less while rolling to visit your Attractions.
        Ability ability = new FerrisWheelTriggeredAbility();
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private FerrisWheel(final FerrisWheel card) {
        super(card);
    }

    @Override
    public FerrisWheel copy() {
        return new FerrisWheel(this);
    }
}

class FerrisWheelTriggeredAbility extends TriggeredAbilityImpl {

    FerrisWheelTriggeredAbility() {
        super(Zone.BATTLEFIELD, new PhaseOutTargetEffect()
                .setText("Choose target creature that hasn't been phased out with this Attraction. "
                        + "That creature phases out until you roll a 3 or less while rolling to visit your Attractions"), false);
    }

    private FerrisWheelTriggeredAbility(final FerrisWheelTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public FerrisWheelTriggeredAbility copy() {
        return new FerrisWheelTriggeredAbility(this);
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
        return result == 4 || result == 5 || result == 6;
    }

    @Override
    public String getRule() {
        return "Choose target creature that hasn't been phased out with this Attraction. "
                + "That creature phases out until you roll a 3 or less while rolling to visit your Attractions.";
    }
}
