package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.keyword.CantBeBlockedSourceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class WillieLumpkinPostman extends CardImpl {

    public WillieLumpkinPostman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CITIZEN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Willie Lumpkin can't be blocked.
        this.addAbility(new CantBeBlockedSourceAbility());

        // Whenever Willie Lumpkin deals combat damage to an opponent, you draw a card and that player may draw a card.
        // If they do, that player can't attack you or permanents you control during their next turn.
        this.addAbility(new WillieLumpkinTriggeredAbility());
    }

    private WillieLumpkinPostman(final WillieLumpkinPostman card) {
        super(card);
    }

    @Override
    public WillieLumpkinPostman copy() {
        return new WillieLumpkinPostman(this);
    }
}

class WillieLumpkinTriggeredAbility extends DealsCombatDamageToAPlayerTriggeredAbility {

    WillieLumpkinTriggeredAbility() {
        super(new WillieLumpkinEffect(), false, true);
        setTriggerPhrase("Whenever {this} deals combat damage to an opponent, ");
    }

    private WillieLumpkinTriggeredAbility(final WillieLumpkinTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public WillieLumpkinTriggeredAbility copy() {
        return new WillieLumpkinTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return super.checkTrigger(event, game)
                && game.getOpponents(getControllerId()).contains(event.getPlayerId());
    }
}

class WillieLumpkinEffect extends OneShotEffect {

    WillieLumpkinEffect() {
        super(Outcome.Benefit);
        this.staticText = "you draw a card and that player may draw a card. "
                + "If they do, that player can't attack you or permanents you control during their next turn";
    }

    private WillieLumpkinEffect(final WillieLumpkinEffect effect) {
        super(effect);
    }

    @Override
    public WillieLumpkinEffect copy() {
        return new WillieLumpkinEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (opponent != null
                && opponent.chooseUse(Outcome.DrawCard, "Draw a card?", source, game)
                && opponent.drawCards(1, source, game) > 0) {
            game.addEffect(new WillieLumpkinCantAttackEffect(opponent.getId()), source);
        }
        return true;
    }
}

class WillieLumpkinCantAttackEffect extends RestrictionEffect {

    private final UUID opponentId;

    WillieLumpkinCantAttackEffect(UUID opponentId) {
        super(Duration.UntilEndOfYourNextTurn);
        this.opponentId = opponentId;
        this.staticText = "";
    }

    private WillieLumpkinCantAttackEffect(final WillieLumpkinCantAttackEffect effect) {
        super(effect);
        this.opponentId = effect.opponentId;
    }

    @Override
    public WillieLumpkinCantAttackEffect copy() {
        return new WillieLumpkinCantAttackEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        if (opponentId != null) {
            setStartingControllerAndTurnNum(game, opponentId, game.getActivePlayerId());
        } else {
            discard();
        }
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return permanent.isControlledBy(opponentId) && game.isActivePlayer(opponentId);
    }

    @Override
    public boolean canAttack(Permanent attacker, UUID defenderId, Ability source, Game game, boolean canUseChooseDialogs) {
        UUID controllerId = source.getControllerId();
        if (defenderId == null || controllerId == null) {
            return true;
        }
        Permanent defender = game.getPermanent(defenderId);
        if (defender != null) {
            return !defender.isControlledBy(controllerId);
        }
        return !defenderId.equals(controllerId);
    }
}
