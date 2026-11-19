package mage.cards.h;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.counters.Counters;
import mage.game.Game;
import mage.game.events.DamageEvent;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HeroicSacrifice extends CardImpl {

    public HeroicSacrifice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // Choose target creature you control.
        // Until end of turn, all damage that would be dealt to you and creatures you control is dealt to the chosen creature instead (if it's still on the battlefield).
        // When that creature dies this turn, put its counters on up to one target creature you control and draw a card.
        this.getSpellAbility().addEffect(new HeroicSacrificeEffect());
        this.getSpellAbility().addTarget(new TargetControlledCreaturePermanent());
    }

    private HeroicSacrifice(final HeroicSacrifice card) {
        super(card);
    }

    @Override
    public HeroicSacrifice copy() {
        return new HeroicSacrifice(this);
    }
}

class HeroicSacrificeEffect extends OneShotEffect {

    HeroicSacrificeEffect() {
        super(Outcome.RedirectDamage);
        this.staticText = "choose target creature you control. Until end of turn, all damage that would be dealt to you and creatures you control "
                + "is dealt to the chosen creature instead (if it's still on the battlefield). "
                + "When that creature dies this turn, put its counters on up to one target creature you control and draw a card";
    }

    private HeroicSacrificeEffect(final HeroicSacrificeEffect effect) {
        super(effect);
    }

    @Override
    public HeroicSacrificeEffect copy() {
        return new HeroicSacrificeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent chosen = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (chosen == null) {
            return false;
        }
        MageObjectReference chosenMor = new MageObjectReference(chosen, game);
        game.addEffect(new HeroicSacrificeRedirectEffect(chosenMor), source);
        game.addDelayedTriggeredAbility(new HeroicSacrificeDelayedTriggeredAbility(chosenMor), source);
        return true;
    }
}

class HeroicSacrificeRedirectEffect extends ReplacementEffectImpl {

    private final MageObjectReference chosenMor;

    HeroicSacrificeRedirectEffect(MageObjectReference chosenMor) {
        super(Duration.EndOfTurn, Outcome.RedirectDamage);
        this.chosenMor = chosenMor;
    }

    private HeroicSacrificeRedirectEffect(final HeroicSacrificeRedirectEffect effect) {
        super(effect);
        this.chosenMor = effect.chosenMor;
    }

    @Override
    public HeroicSacrificeRedirectEffect copy() {
        return new HeroicSacrificeRedirectEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGE_PLAYER
                || event.getType() == GameEvent.EventType.DAMAGE_PERMANENT;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent chosen = chosenMor.getPermanent(game);
        if (chosen == null) {
            discard();
            return false;
        }
        if (event.getType() == GameEvent.EventType.DAMAGE_PLAYER) {
            return event.getPlayerId().equals(source.getControllerId());
        }
        Permanent targetPerm = game.getPermanent(event.getTargetId());
        return targetPerm != null
                && targetPerm.isControlledBy(source.getControllerId())
                && targetPerm.isCreature(game)
                && !targetPerm.getId().equals(chosen.getId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent chosen = chosenMor.getPermanent(game);
        if (chosen == null) {
            return false;
        }
        DamageEvent damageEvent = (DamageEvent) event;
        chosen.damage(
                damageEvent.getAmount(),
                damageEvent.getSourceId(),
                source,
                game,
                damageEvent.isCombatDamage(),
                damageEvent.isPreventable(),
                event.getAppliedEffects()
        );
        return true;
    }
}

class HeroicSacrificeDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference chosenMor;

    HeroicSacrificeDelayedTriggeredAbility(MageObjectReference chosenMor) {
        super(new HeroicSacrificeCountersAndDrawEffect(), Duration.EndOfTurn, true, false);
        this.chosenMor = chosenMor;
        this.addTarget(new TargetControlledCreaturePermanent(0, 1));
        setTriggerPhrase("When that creature dies this turn, ");
    }

    private HeroicSacrificeDelayedTriggeredAbility(final HeroicSacrificeDelayedTriggeredAbility ability) {
        super(ability);
        this.chosenMor = ability.chosenMor;
    }

    @Override
    public HeroicSacrificeDelayedTriggeredAbility copy() {
        return new HeroicSacrificeDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (!zEvent.isDiesEvent() || zEvent.getTarget() == null) {
            return false;
        }
        if (chosenMor.refersTo(zEvent.getTarget(), game)) {
            this.getEffects().setValue("counters", zEvent.getTarget().getCounters(game).copy());
            return true;
        }
        return false;
    }
}

class HeroicSacrificeCountersAndDrawEffect extends OneShotEffect {

    HeroicSacrificeCountersAndDrawEffect() {
        super(Outcome.Benefit);
        this.staticText = "put its counters on up to one target creature you control and draw a card";
    }

    private HeroicSacrificeCountersAndDrawEffect(final HeroicSacrificeCountersAndDrawEffect effect) {
        super(effect);
    }

    @Override
    public HeroicSacrificeCountersAndDrawEffect copy() {
        return new HeroicSacrificeCountersAndDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        Object countersObj = getValue("counters");
        if (target != null && countersObj instanceof Counters) {
            for (Counter counter : ((Counters) countersObj).values()) {
                target.addCounters(counter.copy(), source.getControllerId(), source, game);
            }
        }
        controller.drawCards(1, source, game);
        return true;
    }
}
