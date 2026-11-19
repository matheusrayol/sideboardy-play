package mage.cards.a;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.condition.common.BeforeAttackersAreDeclaredCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.AttacksIfAbleTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.ControlledFromStartOfControllerTurnPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.common.AttackedThisTurnWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ArcumsWhistle extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent(
            "non-Wall creature the active player has controlled continuously since the beginning of the turn"
    );

    static {
        filter.add(Predicates.not(SubType.WALL.getPredicate()));
        filter.add(TargetController.ACTIVE.getControllerPredicate());
        filter.add(new ControlledFromStartOfControllerTurnPredicate());
    }

    public ArcumsWhistle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // {3}, {T}: Choose target non-Wall creature the active player has controlled continuously since the beginning of the turn. That player may pay {X}, where X is that creature's mana value. If they don't pay, the creature attacks this turn if able, and at the beginning of the next end step, destroy it if it didn't attack this turn. Activate only before attackers are declared.
        Ability ability = new ActivateIfConditionActivatedAbility(
                new ArcumsWhistleEffect(),
                new GenericManaCost(3),
                BeforeAttackersAreDeclaredCondition.instance
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private ArcumsWhistle(final ArcumsWhistle card) {
        super(card);
    }

    @Override
    public ArcumsWhistle copy() {
        return new ArcumsWhistle(this);
    }
}

class ArcumsWhistleEffect extends OneShotEffect {

    ArcumsWhistleEffect() {
        super(Outcome.Detriment);
        this.staticText = "choose target non-Wall creature the active player has controlled continuously since the beginning of the turn. "
                + "That player may pay {X}, where X is that creature's mana value. If they don't pay, the creature attacks this turn if able, "
                + "and at the beginning of the next end step, destroy it if it didn't attack this turn";
    }

    private ArcumsWhistleEffect(final ArcumsWhistleEffect effect) {
        super(effect);
    }

    @Override
    public ArcumsWhistleEffect copy() {
        return new ArcumsWhistleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetCreature == null) {
            return false;
        }
        Player activePlayer = game.getPlayer(game.getActivePlayerId());
        if (activePlayer == null) {
            return false;
        }
        int manaValue = targetCreature.getManaValue();
        GenericManaCost cost = new GenericManaCost(manaValue);
        if (cost.canPay(source, source, activePlayer.getId(), game)
                && activePlayer.chooseUse(
                        Outcome.Benefit,
                        "Pay {" + manaValue + "} so " + targetCreature.getLogName() + " isn't forced to attack?",
                        source,
                        game
                )
                && cost.pay(source, game, source, activePlayer.getId(), false, null)) {
            return true;
        }
        game.addEffect(new AttacksIfAbleTargetEffect(Duration.EndOfTurn)
                .setTargetPointer(new FixedTarget(targetCreature, game)), source);
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new ArcumsWhistleDestroyEffect().setTargetPointer(new FixedTarget(targetCreature, game))
        ), source);
        return true;
    }
}

class ArcumsWhistleDestroyEffect extends OneShotEffect {

    ArcumsWhistleDestroyEffect() {
        super(Outcome.DestroyPermanent);
        this.staticText = "destroy it if it didn't attack this turn";
    }

    private ArcumsWhistleDestroyEffect(final ArcumsWhistleDestroyEffect effect) {
        super(effect);
    }

    @Override
    public ArcumsWhistleDestroyEffect copy() {
        return new ArcumsWhistleDestroyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetCreature == null) {
            return false;
        }
        AttackedThisTurnWatcher watcher = game.getState().getWatcher(AttackedThisTurnWatcher.class);
        if (watcher != null && watcher.getAttackedThisTurnCreatures().contains(new MageObjectReference(targetCreature, game))) {
            return true;
        }
        return targetCreature.destroy(source, game, false);
    }
}
