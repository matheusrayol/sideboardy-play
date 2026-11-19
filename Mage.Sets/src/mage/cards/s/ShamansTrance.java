package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ShamansTrance extends CardImpl {

    public ShamansTrance(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{R}");

        // Other players can't play lands or cast spells from their graveyards this turn.
        this.getSpellAbility().addEffect(new ShamansTranceCantPlayEffect());

        // You may play lands and cast spells from other players' graveyards this turn as though those cards were in your graveyard.
        this.getSpellAbility().addEffect(new ShamansTrancePlayEffect());
    }

    private ShamansTrance(final ShamansTrance card) {
        super(card);
    }

    @Override
    public ShamansTrance copy() {
        return new ShamansTrance(this);
    }
}

class ShamansTranceCantPlayEffect extends ContinuousRuleModifyingEffectImpl {

    ShamansTranceCantPlayEffect() {
        super(Duration.EndOfTurn, Outcome.Detriment);
        staticText = "Other players can't play lands or cast spells from their graveyards this turn";
    }

    private ShamansTranceCantPlayEffect(final ShamansTranceCantPlayEffect effect) {
        super(effect);
    }

    @Override
    public ShamansTranceCantPlayEffect copy() {
        return new ShamansTranceCantPlayEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLAY_LAND
                || event.getType() == GameEvent.EventType.CAST_SPELL;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (source.isControlledBy(event.getPlayerId())) {
            return false;
        }
        Card card = game.getCard(event.getSourceId());
        if (card == null) {
            return false;
        }
        UUID mainCardId = CardUtil.getMainCardId(game, card.getId());
        return game.getState().getZone(mainCardId) == Zone.GRAVEYARD && card.isOwnedBy(event.getPlayerId());
    }
}

class ShamansTrancePlayEffect extends AsThoughEffectImpl {

    ShamansTrancePlayEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfTurn, Outcome.Benefit);
        staticText = "You may play lands and cast spells from other players' graveyards this turn as though those cards were in your graveyard";
    }

    private ShamansTrancePlayEffect(final ShamansTrancePlayEffect effect) {
        super(effect);
    }

    @Override
    public ShamansTrancePlayEffect copy() {
        return new ShamansTrancePlayEffect(this);
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
        if (!source.isControlledBy(playerId)) {
            return false;
        }
        Card cardToCheck = game.getCard(objectId);
        if (cardToCheck == null || cardToCheck.isOwnedBy(playerId)) {
            return false;
        }
        Player owner = game.getPlayer(cardToCheck.getOwnerId());
        if (owner == null) {
            return false;
        }
        UUID mainCardId = cardToCheck.getMainCard().getId();
        if (owner.getGraveyard().getCards(game).stream().noneMatch(c -> c.getId().equals(mainCardId))) {
            return false;
        }
        if (affectedAbility instanceof SpellAbility) {
            cardToCheck = ((SpellAbility) affectedAbility).getCharacteristics(game);
            return !cardToCheck.isLand(game) && !cardToCheck.getManaCost().isEmpty();
        }
        return cardToCheck.isLand(game);
    }
}
