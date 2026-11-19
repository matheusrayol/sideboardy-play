package mage.cards.p;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.common.PlayerAttackedWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PickUpThePace extends CardImpl {

    public PickUpThePace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{R}");

        // Whenever a creature you control that entered this turn attacks, exile the top card of your library.
        this.addAbility(new PickUpThePaceTriggeredAbility(), new PlayerAttackedWatcher());

        // You may play cards exiled with this enchantment as long as you attacked this turn.
        this.addAbility(new SimpleStaticAbility(new PickUpThePacePlayEffect()));
    }

    private PickUpThePace(final PickUpThePace card) {
        super(card);
    }

    @Override
    public PickUpThePace copy() {
        return new PickUpThePace(this);
    }
}

class PickUpThePaceTriggeredAbility extends TriggeredAbilityImpl {

    PickUpThePaceTriggeredAbility() {
        super(Zone.BATTLEFIELD, new PickUpThePaceExileEffect(), false);
        setTriggerPhrase("Whenever a creature you control that entered this turn attacks, ");
    }

    private PickUpThePaceTriggeredAbility(final PickUpThePaceTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public PickUpThePaceTriggeredAbility copy() {
        return new PickUpThePaceTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent attacker = game.getPermanent(event.getSourceId());
        return attacker != null
                && attacker.isControlledBy(getControllerId())
                && attacker.getTurnsOnBattlefield() == 0;
    }
}

class PickUpThePaceExileEffect extends OneShotEffect {

    PickUpThePaceExileEffect() {
        super(Outcome.DrawCard);
        this.staticText = "exile the top card of your library";
    }

    private PickUpThePaceExileEffect(final PickUpThePaceExileEffect effect) {
        super(effect);
    }

    @Override
    public PickUpThePaceExileEffect copy() {
        return new PickUpThePaceExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Card topCard = controller.getLibrary().getFromTop(game);
        if (topCard == null) {
            return true;
        }
        MageObject sourceObject = source.getSourceObject(game);
        String exileName = sourceObject == null ? "Pick Up the Pace" : sourceObject.getIdName();
        return controller.moveCardsToExile(
                topCard, source, game, true,
                CardUtil.getExileZoneId(game, source), exileName
        );
    }
}

class PickUpThePacePlayEffect extends AsThoughEffectImpl {

    PickUpThePacePlayEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "You may play cards exiled with this enchantment as long as you attacked this turn";
    }

    private PickUpThePacePlayEffect(final PickUpThePacePlayEffect effect) {
        super(effect);
    }

    @Override
    public PickUpThePacePlayEffect copy() {
        return new PickUpThePacePlayEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        if (!source.isControlledBy(affectedControllerId)) {
            return false;
        }
        PlayerAttackedWatcher watcher = game.getState().getWatcher(PlayerAttackedWatcher.class);
        if (watcher == null || watcher.getNumberOfAttackersCurrentTurn(affectedControllerId) <= 0) {
            return false;
        }
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        return exileZone != null && exileZone.contains(CardUtil.getMainCardId(game, objectId));
    }
}
