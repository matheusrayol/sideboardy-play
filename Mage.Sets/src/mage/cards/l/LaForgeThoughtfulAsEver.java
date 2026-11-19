package mage.cards.l;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.FaerieToken;
import mage.watchers.Watcher;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class LaForgeThoughtfulAsEver extends CardImpl {

    public LaForgeThoughtfulAsEver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}: For each permanent you control that you've put a counter on this turn, create a 1/1 blue Faerie creature token with flying.
        this.addAbility(new SimpleActivatedAbility(new LaForgeEffect(), new TapSourceCost()), new LaForgeWatcher());
    }

    private LaForgeThoughtfulAsEver(final LaForgeThoughtfulAsEver card) {
        super(card);
    }

    @Override
    public LaForgeThoughtfulAsEver copy() {
        return new LaForgeThoughtfulAsEver(this);
    }
}

class LaForgeEffect extends OneShotEffect {

    LaForgeEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "for each permanent you control that you've put a counter on this turn, "
                + "create a 1/1 blue Faerie creature token with flying";
    }

    private LaForgeEffect(final LaForgeEffect effect) {
        super(effect);
    }

    @Override
    public LaForgeEffect copy() {
        return new LaForgeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        LaForgeWatcher watcher = game.getState().getWatcher(LaForgeWatcher.class);
        if (watcher == null) {
            return false;
        }
        Set<MageObjectReference> targets = watcher.getPermanents(source.getControllerId());
        if (targets.isEmpty()) {
            return true;
        }
        int count = 0;
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_CONTROLLED_PERMANENT, source.getControllerId(), game
        )) {
            if (targets.contains(new MageObjectReference(permanent, game))) {
                count++;
            }
        }
        if (count > 0) {
            new CreateTokenEffect(new FaerieToken(), count).apply(game, source);
        }
        return true;
    }
}

class LaForgeWatcher extends Watcher {

    private final Map<UUID, Set<MageObjectReference>> playerToPermanents = new HashMap<>();

    LaForgeWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.COUNTERS_ADDED) {
            return;
        }
        UUID playerId = event.getPlayerId();
        if (playerId == null) {
            Permanent sourcePerm = game.getPermanentOrLKIBattlefield(event.getSourceId());
            if (sourcePerm != null) {
                playerId = sourcePerm.getControllerId();
            }
        }
        Permanent target = game.getPermanent(event.getTargetId());
        int offset = 0;
        if (target == null) {
            target = game.getPermanentEntering(event.getTargetId());
            offset++;
        }
        if (playerId != null && target != null) {
            playerToPermanents
                    .computeIfAbsent(playerId, k -> new HashSet<>())
                    .add(new MageObjectReference(target, game, offset));
        }
    }

    @Override
    public void reset() {
        super.reset();
        playerToPermanents.clear();
    }

    Set<MageObjectReference> getPermanents(UUID playerId) {
        return playerToPermanents.getOrDefault(playerId, Collections.emptySet());
    }
}
