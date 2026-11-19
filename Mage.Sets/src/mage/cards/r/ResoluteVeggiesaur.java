package mage.cards.r;

import mage.MageInt;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ResoluteVeggiesaur extends CardImpl {

    public ResoluteVeggiesaur(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.subtype.add(SubType.DINOSAUR);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Whenever you roll your third die each turn, put a +1/+1 counter on this creature.
        this.addAbility(new ResoluteVeggiesaurTriggeredAbility(), new ResoluteVeggiesaurWatcher());
    }

    private ResoluteVeggiesaur(final ResoluteVeggiesaur card) {
        super(card);
    }

    @Override
    public ResoluteVeggiesaur copy() {
        return new ResoluteVeggiesaur(this);
    }
}

class ResoluteVeggiesaurWatcher extends Watcher {

    private final Map<UUID, Integer> rolls = new HashMap<>();

    ResoluteVeggiesaurWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DIE_ROLLED) {
            DieRolledEvent drEvent = (DieRolledEvent) event;
            if (drEvent.getRollDieType() == RollDieType.NUMERICAL) {
                rolls.merge(event.getTargetId(), 1, Integer::sum);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        rolls.clear();
    }

    int getRolls(UUID playerId) {
        return rolls.getOrDefault(playerId, 0);
    }
}

class ResoluteVeggiesaurTriggeredAbility extends TriggeredAbilityImpl {

    ResoluteVeggiesaurTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
    }

    private ResoluteVeggiesaurTriggeredAbility(final ResoluteVeggiesaurTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ResoluteVeggiesaurTriggeredAbility copy() {
        return new ResoluteVeggiesaurTriggeredAbility(this);
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
        ResoluteVeggiesaurWatcher watcher = game.getState().getWatcher(ResoluteVeggiesaurWatcher.class);
        return watcher != null && watcher.getRolls(event.getTargetId()) == 3;
    }

    @Override
    public String getRule() {
        return "Whenever you roll your third die each turn, put a +1/+1 counter on {this}.";
    }
}
