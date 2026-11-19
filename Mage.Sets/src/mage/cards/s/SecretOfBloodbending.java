package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.condition.common.WaterbendedCondition;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileSpellEffect;
import mage.abilities.effects.common.turn.ControlTargetPlayerNextTurnEffect;
import mage.abilities.keyword.WaterbendAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class SecretOfBloodbending extends CardImpl {

    public SecretOfBloodbending(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{U}{U}{U}{U}");

        this.subtype.add(SubType.LESSON);

        // As an additional cost to cast this spell, you may waterbend {10}.
        this.addAbility(new WaterbendAbility(10));

        // You control target opponent during their next combat phase. If this spell's additional cost was paid, you control that player during their next turn instead.
        this.getSpellAbility().addEffect(new SecretOfBloodbendingEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());

        // Exile Secret of Bloodbending.
        this.getSpellAbility().addEffect(new ExileSpellEffect());
    }

    private SecretOfBloodbending(final SecretOfBloodbending card) {
        super(card);
    }

    @Override
    public SecretOfBloodbending copy() {
        return new SecretOfBloodbending(this);
    }
}

class SecretOfBloodbendingEffect extends OneShotEffect {

    SecretOfBloodbendingEffect() {
        super(Outcome.Benefit);
        this.staticText = "You control target opponent during their next combat phase. "
                + "If this spell's additional cost was paid, you control that player during their next turn instead. "
                + "<i>(You see all cards that player could see and make all decisions for them.)</i>";
    }

    private SecretOfBloodbendingEffect(final SecretOfBloodbendingEffect effect) {
        super(effect);
    }

    @Override
    public SecretOfBloodbendingEffect copy() {
        return new SecretOfBloodbendingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (WaterbendedCondition.instance.apply(game, source)) {
            return new ControlTargetPlayerNextTurnEffect().apply(game, source);
        }
        Player targetPlayer = game.getPlayer(source.getFirstTarget());
        if (targetPlayer == null) {
            return false;
        }
        game.addEffect(new SecretOfBloodbendingCombatControlEffect(targetPlayer.getId()), source);
        return true;
    }
}

class SecretOfBloodbendingCombatControlEffect extends ContinuousRuleModifyingEffectImpl {

    private final UUID targetPlayerId;
    private boolean controllingCombat = false;

    SecretOfBloodbendingCombatControlEffect(UUID targetPlayerId) {
        super(Duration.Custom, Outcome.Benefit, false, false);
        this.targetPlayerId = targetPlayerId;
    }

    private SecretOfBloodbendingCombatControlEffect(final SecretOfBloodbendingCombatControlEffect effect) {
        super(effect);
        this.targetPlayerId = effect.targetPlayerId;
        this.controllingCombat = effect.controllingCombat;
    }

    @Override
    public SecretOfBloodbendingCombatControlEffect copy() {
        return new SecretOfBloodbendingCombatControlEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COMBAT_PHASE_PRE
                || event.getType() == GameEvent.EventType.COMBAT_PHASE_POST
                || event.getType() == GameEvent.EventType.END_TURN_STEP_POST;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!game.isActivePlayer(targetPlayerId)) {
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        Player targetPlayer = game.getPlayer(targetPlayerId);
        if (controller == null || targetPlayer == null || !controller.isInGame() || !targetPlayer.isInGame()) {
            this.discard();
            return false;
        }
        if (event.getType() == GameEvent.EventType.COMBAT_PHASE_PRE && !controllingCombat) {
            if (targetPlayer.isGameUnderControl()) {
                controller.controlPlayersTurn(game, targetPlayerId, " (during combat phase)");
                controllingCombat = true;
            } else {
                this.discard();
            }
        } else if ((event.getType() == GameEvent.EventType.COMBAT_PHASE_POST
                || event.getType() == GameEvent.EventType.END_TURN_STEP_POST) && controllingCombat) {
            targetPlayer.setGameUnderYourControl(game, true);
            controller.getPlayersUnderYourControl().remove(targetPlayerId);
            this.discard();
        }
        return false;
    }
}
