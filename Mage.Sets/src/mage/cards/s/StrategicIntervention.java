package mage.cards.s;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author muz
 */
public final class StrategicIntervention extends CardImpl {

    public StrategicIntervention(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{W}");

        // Whenever a creature you control attacks alone, it gets +1/+1 until end of turn. Tap up to one target creature defending player controls.
        this.addAbility(new StrategicInterventionTriggeredAbility());
    }

    private StrategicIntervention(final StrategicIntervention card) {
        super(card);
    }

    @Override
    public StrategicIntervention copy() {
        return new StrategicIntervention(this);
    }
}

class StrategicInterventionTriggeredAbility extends TriggeredAbilityImpl {

    StrategicInterventionTriggeredAbility() {
        super(Zone.BATTLEFIELD, new StrategicInterventionEffect(), false);
        setTriggerPhrase("Whenever a creature you control attacks alone, ");
    }

    private StrategicInterventionTriggeredAbility(final StrategicInterventionTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public StrategicInterventionTriggeredAbility copy() {
        return new StrategicInterventionTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getCombat().attacksAlone()) {
            return false;
        }
        UUID attackerId = game.getCombat().getAttackers().stream().findFirst().orElse(null);
        Permanent attacker = attackerId != null ? game.getPermanent(attackerId) : null;
        if (attacker == null || !attacker.isControlledBy(getControllerId())) {
            return false;
        }
        UUID defendingPlayerId = game.getCombat().getDefendingPlayerId(attacker.getId(), game);
        if (defendingPlayerId == null) {
            return false;
        }
        getEffects().setValue("attackerId", attacker.getId());
        FilterCreaturePermanent filter = new FilterCreaturePermanent("creature defending player controls");
        filter.add(new ControllerIdPredicate(defendingPlayerId));
        this.getTargets().clear();
        this.addTarget(new TargetPermanent(0, 1, filter));
        return true;
    }
}

class StrategicInterventionEffect extends OneShotEffect {

    StrategicInterventionEffect() {
        super(Outcome.BoostCreature);
        staticText = "it gets +1/+1 until end of turn. Tap up to one target creature defending player controls";
    }

    private StrategicInterventionEffect(final StrategicInterventionEffect effect) {
        super(effect);
    }

    @Override
    public StrategicInterventionEffect copy() {
        return new StrategicInterventionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent attacker = game.getPermanent((UUID) getValue("attackerId"));
        if (attacker != null) {
            ContinuousEffect boost = new BoostTargetEffect(1, 1, Duration.EndOfTurn);
            boost.setTargetPointer(new FixedTarget(attacker, game));
            game.addEffect(boost, source);
        }
        Permanent targetCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetCreature != null) {
            targetCreature.tap(source, game);
        }
        return true;
    }
}
