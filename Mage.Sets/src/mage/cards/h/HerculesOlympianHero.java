package mage.cards.h;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.dynamicvalue.common.SavedDamageValue;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HerculesOlympianHero extends CardImpl {

    public HerculesOlympianHero(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DEMIGOD);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever Hercules attacks, put a +1/+1 counter on him. He gains indestructible until end of turn.
        Ability attackAbility = new AttacksTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false
        );
        attackAbility.addEffect(new GainAbilitySourceEffect(
                IndestructibleAbility.getInstance(), Duration.EndOfTurn
        ).setText("He gains indestructible until end of turn"));
        this.addAbility(attackAbility);

        // Whenever Hercules is dealt damage for the first time each turn, put that many +1/+1 counters on him.
        this.addAbility(new HerculesDamagedTriggeredAbility(), new HerculesDamageWatcher());
    }

    private HerculesOlympianHero(final HerculesOlympianHero card) {
        super(card);
    }

    @Override
    public HerculesOlympianHero copy() {
        return new HerculesOlympianHero(this);
    }
}

class HerculesDamagedTriggeredAbility extends TriggeredAbilityImpl {

    HerculesDamagedTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(
                CounterType.P1P1.createInstance(), SavedDamageValue.MANY, false
        ).setText("put that many +1/+1 counters on him"), false);
        setTriggerPhrase("Whenever {this} is dealt damage for the first time each turn, ");
    }

    private HerculesDamagedTriggeredAbility(final HerculesDamagedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public HerculesDamagedTriggeredAbility copy() {
        return new HerculesDamagedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PERMANENT;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.getTargetId().equals(getSourceId())) {
            return false;
        }
        Permanent permanent = getSourcePermanentIfItStillExists(game);
        HerculesDamageWatcher watcher = game.getState().getWatcher(HerculesDamageWatcher.class);
        if (permanent == null || watcher == null || !watcher.isFirstDamageEvent(permanent, event.getId(), game)) {
            return false;
        }
        getEffects().setValue("damage", event.getAmount());
        return true;
    }
}

class HerculesDamageWatcher extends Watcher {

    private final Map<MageObjectReference, UUID> firstDamageEventId = new HashMap<>();

    HerculesDamageWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.DAMAGED_PERMANENT) {
            return;
        }
        Permanent permanent = game.getPermanentOrLKIBattlefield(event.getTargetId());
        if (permanent != null) {
            firstDamageEventId.putIfAbsent(new MageObjectReference(permanent, game), event.getId());
        }
    }

    @Override
    public void reset() {
        super.reset();
        firstDamageEventId.clear();
    }

    boolean isFirstDamageEvent(Permanent permanent, UUID eventId, Game game) {
        return eventId.equals(firstDamageEventId.get(new MageObjectReference(permanent, game)));
    }
}
