package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.WinGameSourceControllerEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class PeaceInOurTime extends CardImpl {

    public PeaceInOurTime(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{W}");

        // At the beginning of combat on each player's turn, that player may draw a card. If they do, they can't attack this turn and you put a peace counter on this enchantment.
        this.addAbility(new BeginningOfCombatTriggeredAbility(
                TargetController.ANY, new PeaceInOurTimeCombatEffect(), false
        ));

        // At the beginning of your upkeep, if this enchantment has twelve or more peace counters on it, you win the game.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new WinGameSourceControllerEffect())
                .withInterveningIf(PeaceInOurTimeCondition.instance));
    }

    private PeaceInOurTime(final PeaceInOurTime card) {
        super(card);
    }

    @Override
    public PeaceInOurTime copy() {
        return new PeaceInOurTime(this);
    }
}

class PeaceInOurTimeCombatEffect extends OneShotEffect {

    PeaceInOurTimeCombatEffect() {
        super(Outcome.DrawCard);
        this.staticText = "that player may draw a card. If they do, they can't attack this turn and you put a peace counter on {this}";
    }

    private PeaceInOurTimeCombatEffect(final PeaceInOurTimeCombatEffect effect) {
        super(effect);
    }

    @Override
    public PeaceInOurTimeCombatEffect copy() {
        return new PeaceInOurTimeCombatEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player activePlayer = game.getPlayer(game.getActivePlayerId());
        if (activePlayer == null) {
            return false;
        }
        if (activePlayer.chooseUse(Outcome.DrawCard, "Draw a card? (If you do, you can't attack this turn)", source, game)
                && activePlayer.drawCards(1, source, game) > 0) {
            game.addEffect(new PeaceInOurTimeCantAttackEffect(activePlayer.getId()), source);
            new AddCountersSourceEffect(new Counter("peace")).apply(game, source);
            return true;
        }
        return false;
    }
}

class PeaceInOurTimeCantAttackEffect extends RestrictionEffect {

    private final UUID playerId;

    PeaceInOurTimeCantAttackEffect(UUID playerId) {
        super(Duration.EndOfTurn);
        this.playerId = playerId;
    }

    private PeaceInOurTimeCantAttackEffect(final PeaceInOurTimeCantAttackEffect effect) {
        super(effect);
        this.playerId = effect.playerId;
    }

    @Override
    public PeaceInOurTimeCantAttackEffect copy() {
        return new PeaceInOurTimeCantAttackEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return permanent.isControlledBy(playerId);
    }

    @Override
    public boolean canAttack(Permanent attacker, UUID defenderId, Ability source, Game game, boolean canUseChooseDialogs) {
        return false;
    }
}

enum PeaceInOurTimeCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent != null && permanent.getCounters(game).getCount("peace") >= 12;
    }

    @Override
    public String toString() {
        return "{this} has twelve or more peace counters on it";
    }
}
