package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.TapTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SpinnyRide extends CardImpl {

    public SpinnyRide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Tap target creature an opponent controls.
        Ability ability = new SpinnyRideTriggeredAbility();
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE));
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private SpinnyRide(final SpinnyRide card) {
        super(card);
    }

    @Override
    public SpinnyRide copy() {
        return new SpinnyRide(this);
    }
}

class SpinnyRideTriggeredAbility extends TriggeredAbilityImpl {

    SpinnyRideTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TapTargetEffect(), false);
    }

    private SpinnyRideTriggeredAbility(final SpinnyRideTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SpinnyRideTriggeredAbility copy() {
        return new SpinnyRideTriggeredAbility(this);
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
        return "Tap target creature an opponent controls.";
    }
}
