package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class FoamWeaponsKiosk extends CardImpl {

    public FoamWeaponsKiosk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Put a +1/+1 counter on target creature you control. That creature gains vigilance until end of turn.
        Ability ability = new FoamWeaponsKioskTriggeredAbility();
        ability.addEffect(new GainAbilityTargetEffect(VigilanceAbility.getInstance(), Duration.EndOfTurn)
                .setText("That creature gains vigilance until end of turn"));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private FoamWeaponsKiosk(final FoamWeaponsKiosk card) {
        super(card);
    }

    @Override
    public FoamWeaponsKiosk copy() {
        return new FoamWeaponsKiosk(this);
    }
}

class FoamWeaponsKioskTriggeredAbility extends TriggeredAbilityImpl {

    FoamWeaponsKioskTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false);
    }

    private FoamWeaponsKioskTriggeredAbility(final FoamWeaponsKioskTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public FoamWeaponsKioskTriggeredAbility copy() {
        return new FoamWeaponsKioskTriggeredAbility(this);
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
        return "Put a +1/+1 counter on target creature you control. That creature gains vigilance until end of turn.";
    }
}
