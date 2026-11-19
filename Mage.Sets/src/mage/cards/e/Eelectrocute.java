package mage.cards.e;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.common.TargetAnyTarget;
import mage.watchers.Watcher;
import mage.watchers.common.CastFromGraveyardWatcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Eelectrocute extends CardImpl {

    public Eelectrocute(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{R}");

        // Eelectrocute deals 2 damage to any target.
        this.getSpellAbility().addEffect(new DamageTargetEffect(2));
        this.getSpellAbility().addTarget(new TargetAnyTarget());

        // You may cast this card from your graveyard as long as you've rolled a 6 this turn. If you cast it this way and it would be put into your graveyard, exile it instead.
        this.addAbility(new SimpleStaticAbility(Zone.GRAVEYARD, new EelectrocuteCastEffect()), new EelectrocuteWatcher());
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new EelectrocuteExileEffect()), new CastFromGraveyardWatcher());
    }

    private Eelectrocute(final Eelectrocute card) {
        super(card);
    }

    @Override
    public Eelectrocute copy() {
        return new Eelectrocute(this);
    }
}

class EelectrocuteWatcher extends Watcher {

    private final Set<UUID> rolledSix = new HashSet<>();

    EelectrocuteWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DIE_ROLLED) {
            DieRolledEvent drEvent = (DieRolledEvent) event;
            if (drEvent.getRollDieType() == RollDieType.NUMERICAL && drEvent.getResult() == 6) {
                rolledSix.add(event.getTargetId());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        rolledSix.clear();
    }

    boolean rolledSix(UUID playerId) {
        return rolledSix.contains(playerId);
    }
}

class EelectrocuteCastEffect extends AsThoughEffectImpl {

    EelectrocuteCastEffect() {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfGame, Outcome.Benefit);
        staticText = "You may cast this card from your graveyard as long as you've rolled a 6 this turn";
    }

    private EelectrocuteCastEffect(final EelectrocuteCastEffect effect) {
        super(effect);
    }

    @Override
    public EelectrocuteCastEffect copy() {
        return new EelectrocuteCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        if (!objectId.equals(source.getSourceId()) || !source.isControlledBy(affectedControllerId)) {
            return false;
        }
        if (game.getState().getZone(objectId) != Zone.GRAVEYARD) {
            return false;
        }
        EelectrocuteWatcher watcher = game.getState().getWatcher(EelectrocuteWatcher.class);
        return watcher != null && watcher.rolledSix(affectedControllerId);
    }
}

class EelectrocuteExileEffect extends ReplacementEffectImpl {

    EelectrocuteExileEffect() {
        super(Duration.EndOfGame, Outcome.Exile);
        staticText = "If you cast it this way and it would be put into your graveyard, exile it instead";
    }

    private EelectrocuteExileEffect(final EelectrocuteExileEffect effect) {
        super(effect);
    }

    @Override
    public EelectrocuteExileEffect copy() {
        return new EelectrocuteExileEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        ((ZoneChangeEvent) event).setToZone(Zone.EXILED);
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getToZone() != Zone.GRAVEYARD
                || zEvent.getFromZone() != Zone.STACK
                || !event.getTargetId().equals(source.getSourceId())) {
            return false;
        }
        CastFromGraveyardWatcher watcher = game.getState().getWatcher(CastFromGraveyardWatcher.class);
        return watcher != null
                && watcher.spellWasCastFromGraveyard(
                event.getTargetId(),
                game.getState().getZoneChangeCounter(event.getTargetId())
        );
    }
}
