package mage.cards.z;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.LookAtTopCardOfLibraryAnyTimeEffect;
import mage.abilities.effects.common.continuous.PlayFromTopOfLibraryEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class ZeframCochraneWarpPioneer extends CardImpl {

    public ZeframCochraneWarpPioneer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // You may look at the top card of your library any time.
        this.addAbility(new SimpleStaticAbility(new LookAtTopCardOfLibraryAnyTimeEffect()));

        // Once during each of your turns, you may cast an artifact spell from the top of your library.
        this.addAbility(new SimpleStaticAbility(new ZeframCochraneCastFromTopEffect()), new ZeframCochraneWatcher());

        // Spacecraft you control enter with two additional charge counters on them.
        this.addAbility(new SimpleStaticAbility(new ZeframCochraneCountersEffect()));
    }

    private ZeframCochraneWarpPioneer(final ZeframCochraneWarpPioneer card) {
        super(card);
    }

    @Override
    public ZeframCochraneWarpPioneer copy() {
        return new ZeframCochraneWarpPioneer(this);
    }
}

class ZeframCochraneCastFromTopEffect extends PlayFromTopOfLibraryEffect {

    ZeframCochraneCastFromTopEffect() {
        super(new FilterArtifactCard("cast an artifact spell"));
        this.staticText = "Once during each of your turns, you may cast an artifact spell from the top of your library";
    }

    private ZeframCochraneCastFromTopEffect(final ZeframCochraneCastFromTopEffect effect) {
        super(effect);
    }

    @Override
    public ZeframCochraneCastFromTopEffect copy() {
        return new ZeframCochraneCastFromTopEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        if (!game.isActivePlayer(source.getControllerId())) {
            return false;
        }
        ZeframCochraneWatcher watcher = game.getState().getWatcher(ZeframCochraneWatcher.class);
        if (watcher != null && watcher.isAbilityUsed(new MageObjectReference(source.getSourceId(), game))) {
            return false;
        }
        return super.applies(objectId, affectedAbility, source, game, playerId);
    }
}

class ZeframCochraneWatcher extends Watcher {

    private final Set<MageObjectReference> usedFrom = new HashSet<>();

    ZeframCochraneWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SPELL_CAST
                && event.getApprovingObject() != null
                && event.getApprovingObject().getApprovingAbility() != null) {
            usedFrom.add(new MageObjectReference(event.getApprovingObject().getApprovingAbility().getSourceId(), game));
        }
    }

    @Override
    public void reset() {
        super.reset();
        usedFrom.clear();
    }

    boolean isAbilityUsed(MageObjectReference mor) {
        return usedFrom.contains(mor);
    }
}

class ZeframCochraneCountersEffect extends ReplacementEffectImpl {

    ZeframCochraneCountersEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "Spacecraft you control enter with two additional charge counters on them";
    }

    private ZeframCochraneCountersEffect(final ZeframCochraneCountersEffect effect) {
        super(effect);
    }

    @Override
    public ZeframCochraneCountersEffect copy() {
        return new ZeframCochraneCountersEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        return permanent != null
                && permanent.isControlledBy(source.getControllerId())
                && permanent.hasSubtype(SubType.SPACECRAFT, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        if (permanent != null) {
            permanent.addCounters(CounterType.CHARGE.createInstance(2), source.getControllerId(), source, game, event.getAppliedEffects());
        }
        return false;
    }
}
