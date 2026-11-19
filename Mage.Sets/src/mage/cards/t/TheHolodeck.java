package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheHolodeck extends CardImpl {

    public TheHolodeck(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{7}");

        this.supertype.add(SuperType.LEGENDARY);

        // Once during each of your turns, you may play a permanent card you own from outside the game or from your graveyard.
        SimpleStaticAbility ability = new SimpleStaticAbility(new TheHolodeckPlayEffect());
        this.addAbility(ability, new TheHolodeckWatcher(ability.getOriginalId()));
    }

    private TheHolodeck(final TheHolodeck card) {
        super(card);
    }

    @Override
    public TheHolodeck copy() {
        return new TheHolodeck(this);
    }
}

class TheHolodeckPlayEffect extends AsThoughEffectImpl {

    TheHolodeckPlayEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "Once during each of your turns, you may play a permanent card you own from outside the game or from your graveyard.";
    }

    private TheHolodeckPlayEffect(final TheHolodeckPlayEffect effect) {
        super(effect);
    }

    @Override
    public TheHolodeckPlayEffect copy() {
        return new TheHolodeckPlayEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        throw new IllegalArgumentException("Wrong code usage: can't call applies method on empty affectedAbility");
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        TheHolodeckWatcher watcher = game.getState().getWatcher(TheHolodeckWatcher.class);
        Player controller = game.getPlayer(playerId);
        Card card = game.getCard(objectId);
        if (sourcePermanent == null || watcher == null || controller == null || card == null
                || !game.isActivePlayer(playerId)
                || !source.isControlledBy(playerId)
                || !card.isOwnedBy(playerId)
                || watcher.isUsed(new MageObjectReference(sourcePermanent, game))) {
            return false;
        }
        Zone zone = game.getState().getZone(card.getId());
        boolean validZone = zone == Zone.GRAVEYARD
                || zone == Zone.OUTSIDE
                || controller.getSideboard().contains(card.getId());
        if (!validZone) {
            return false;
        }
        Card characteristicsCard = card;
        if (affectedAbility instanceof SpellAbility) {
            characteristicsCard = ((SpellAbility) affectedAbility).getCharacteristics(game);
        }
        return characteristicsCard.isPermanent(game);
    }
}

class TheHolodeckWatcher extends Watcher {

    private final UUID abilityOriginalId;
    private final Set<MageObjectReference> usedThisTurn = new HashSet<>();
    private Zone landFromZone;

    TheHolodeckWatcher(UUID abilityOriginalId) {
        super(WatcherScope.GAME);
        this.abilityOriginalId = abilityOriginalId;
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PLAY_LAND) {
            landFromZone = game.getState().getZone(event.getTargetId());
            return;
        }
        if (event.getApprovingObject() == null || event.getApprovingObject().getApprovingAbility() == null) {
            return;
        }
        if (!abilityOriginalId.equals(event.getApprovingObject().getApprovingAbility().getOriginalId())) {
            return;
        }
        if (event.getType() == GameEvent.EventType.LAND_PLAYED
                && (landFromZone == Zone.GRAVEYARD || landFromZone == Zone.OUTSIDE)) {
            usedThisTurn.add(event.getApprovingObject().getApprovingMageObjectReference());
        } else if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getSpell(event.getTargetId());
            if (spell != null && (spell.getFromZone() == Zone.GRAVEYARD || spell.getFromZone() == Zone.OUTSIDE)) {
                usedThisTurn.add(event.getApprovingObject().getApprovingMageObjectReference());
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        usedThisTurn.clear();
        landFromZone = null;
    }

    boolean isUsed(MageObjectReference mor) {
        return usedThisTurn.contains(mor);
    }
}
