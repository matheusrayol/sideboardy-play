package mage.cards.l;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.FixedTargets;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Sideboardy
 */
public final class LoveOnTheBattlefield extends CardImpl {

    public LoveOnTheBattlefield(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{U}{R}");

        // Whenever you attack with exactly two creatures, those creatures gain first strike until end of turn, then draw a card.
        // Whenever either of those creatures deals combat damage to a player this combat, put a +1/+1 counter on it.
        this.addAbility(new LoveOnTheBattlefieldTriggeredAbility());
    }

    private LoveOnTheBattlefield(final LoveOnTheBattlefield card) {
        super(card);
    }

    @Override
    public LoveOnTheBattlefield copy() {
        return new LoveOnTheBattlefield(this);
    }
}

class LoveOnTheBattlefieldTriggeredAbility extends TriggeredAbilityImpl {

    LoveOnTheBattlefieldTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LoveOnTheBattlefieldEffect(), false);
        setTriggerPhrase("Whenever you attack with exactly two creatures, ");
    }

    private LoveOnTheBattlefieldTriggeredAbility(final LoveOnTheBattlefieldTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public LoveOnTheBattlefieldTriggeredAbility copy() {
        return new LoveOnTheBattlefieldTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(game.getCombat().getAttackingPlayerId())) {
            return false;
        }
        List<Permanent> attackers = game.getCombat()
                .getAttackers()
                .stream()
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (attackers.size() != 2) {
            return false;
        }
        this.getEffects().setTargetPointer(new FixedTargets(attackers, game));
        return true;
    }
}

class LoveOnTheBattlefieldEffect extends OneShotEffect {

    LoveOnTheBattlefieldEffect() {
        super(Outcome.Benefit);
        this.staticText = "those creatures gain first strike until end of turn, then draw a card. "
                + "Whenever either of those creatures deals combat damage to a player this combat, put a +1/+1 counter on it";
    }

    private LoveOnTheBattlefieldEffect(final LoveOnTheBattlefieldEffect effect) {
        super(effect);
    }

    @Override
    public LoveOnTheBattlefieldEffect copy() {
        return new LoveOnTheBattlefieldEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent creature = game.getPermanent(targetId);
            if (creature == null) {
                continue;
            }
            game.addEffect(new GainAbilityTargetEffect(FirstStrikeAbility.getInstance(), Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(creature, game)), source);
            game.addDelayedTriggeredAbility(new LoveOnTheBattlefieldDelayedTriggeredAbility(
                    new MageObjectReference(creature, game)
            ), source);
        }
        controller.drawCards(1, source, game);
        return true;
    }
}

class LoveOnTheBattlefieldDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference mor;

    LoveOnTheBattlefieldDelayedTriggeredAbility(MageObjectReference mor) {
        super(new AddCountersTargetEffect(CounterType.P1P1.createInstance()), Duration.EndOfCombat, false, false);
        this.mor = mor;
        setTriggerPhrase("Whenever either of those creatures deals combat damage to a player this combat, ");
    }

    private LoveOnTheBattlefieldDelayedTriggeredAbility(final LoveOnTheBattlefieldDelayedTriggeredAbility ability) {
        super(ability);
        this.mor = ability.mor;
    }

    @Override
    public LoveOnTheBattlefieldDelayedTriggeredAbility copy() {
        return new LoveOnTheBattlefieldDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!((DamagedEvent) event).isCombatDamage() || !mor.refersTo(event.getSourceId(), game)) {
            return false;
        }
        Permanent perm = mor.getPermanent(game);
        if (perm == null) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(perm, game));
        return true;
    }
}
