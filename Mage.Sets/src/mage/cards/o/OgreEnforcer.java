package mage.cards.o;

import mage.MageInt;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.DeathtouchAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.StackObject;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class OgreEnforcer extends CardImpl {

    public OgreEnforcer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{R}");

        this.subtype.add(SubType.OGRE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Ogre Enforcer can't be destroyed by lethal damage unless lethal damage dealt by a single source is marked on it.
        Ability ability = new SimpleStaticAbility(new OgreEnforcerEffect());
        ability.addWatcher(new OgreEnforcerWatcher());
        this.addAbility(ability);
    }

    private OgreEnforcer(final OgreEnforcer card) {
        super(card);
    }

    @Override
    public OgreEnforcer copy() {
        return new OgreEnforcer(this);
    }
}

class OgreEnforcerEffect extends ReplacementEffectImpl {

    OgreEnforcerEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "{this} can't be destroyed by lethal damage unless lethal damage dealt by a single source is marked on it";
    }

    private OgreEnforcerEffect(final OgreEnforcerEffect effect) {
        super(effect);
    }

    @Override
    public OgreEnforcerEffect copy() {
        return new OgreEnforcerEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DESTROY_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return event.getTargetId().equals(source.getSourceId()) && event.getSourceId() == null;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        OgreEnforcerWatcher watcher = game.getState().getWatcher(OgreEnforcerWatcher.class);
        return watcher == null || !watcher.hasLethalFromSingleSource(permanent, game);
    }
}

class OgreEnforcerWatcher extends Watcher {

    private final Map<MageObjectReference, Map<UUID, Integer>> damagePerSource = new HashMap<>();
    private final Set<MageObjectReference> deathtouchDamaged = new HashSet<>();

    OgreEnforcerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.DAMAGED_PERMANENT || event.getAmount() <= 0) {
            return;
        }
        Permanent target = game.getPermanent(event.getTargetId());
        if (target == null) {
            return;
        }
        MageObjectReference targetRef = new MageObjectReference(target, game);
        UUID sourceId = event.getSourceId();
        if (sourceId != null) {
            damagePerSource
                    .computeIfAbsent(targetRef, k -> new HashMap<>())
                    .merge(sourceId, event.getAmount(), Integer::sum);
            if (hasDeathtouch(sourceId, game)) {
                deathtouchDamaged.add(targetRef);
            }
        }
    }

    private static boolean hasDeathtouch(UUID sourceId, Game game) {
        MageObject sourceObj = game.getPermanentOrLKIBattlefield(sourceId);
        if (sourceObj == null) {
            StackObject stackObj = game.getStack().getStackObject(sourceId);
            if (stackObj != null) {
                sourceObj = stackObj.getStackAbility().getSourceObject(game);
            } else {
                sourceObj = game.getObject(sourceId);
            }
        }
        if (sourceObj == null) {
            return false;
        }
        Abilities<?> abilities = sourceObj instanceof Card
                ? ((Card) sourceObj).getAbilities(game)
                : sourceObj.getAbilities();
        return abilities != null && abilities.containsKey(DeathtouchAbility.getInstance().getId());
    }

    boolean hasLethalFromSingleSource(Permanent permanent, Game game) {
        MageObjectReference targetRef = new MageObjectReference(permanent, game);
        if (deathtouchDamaged.contains(targetRef) && permanent.isDeathtouched()) {
            return true;
        }
        Map<UUID, Integer> sources = damagePerSource.get(targetRef);
        if (sources == null || sources.isEmpty()) {
            return false;
        }
        int toughness = permanent.getToughness().getValue();
        for (int dmg : sources.values()) {
            if (dmg >= toughness && dmg <= permanent.getDamage()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        damagePerSource.clear();
        deathtouchDamaged.clear();
    }
}
