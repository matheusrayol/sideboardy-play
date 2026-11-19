package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.CyclingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DontBlink extends CardImpl {

    public DontBlink(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Until end of turn, if one or more creatures would enter from exile or after being cast from exile, their owners shuffle them into their libraries instead.
        this.getSpellAbility().addEffect(new DontBlinkReplacementEffect());

        // Cycling {2}
        this.addAbility(new CyclingAbility(new ManaCostsImpl<>("{2}")));
    }

    private DontBlink(final DontBlink card) {
        super(card);
    }

    @Override
    public DontBlink copy() {
        return new DontBlink(this);
    }
}

class DontBlinkReplacementEffect extends ReplacementEffectImpl {

    DontBlinkReplacementEffect() {
        super(Duration.EndOfTurn, Outcome.Detriment);
        staticText = "Until end of turn, if one or more creatures would enter from exile or after being cast from exile, their owners shuffle them into their libraries instead";
    }

    private DontBlinkReplacementEffect(final DontBlinkReplacementEffect effect) {
        super(effect);
    }

    @Override
    public DontBlinkReplacementEffect copy() {
        return new DontBlinkReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Card card = game.getCard(event.getTargetId());
        if (card != null) {
            Player owner = game.getPlayer(card.getOwnerId());
            if (owner != null) {
                owner.moveCards(card, Zone.LIBRARY, source, game);
                owner.shuffleLibrary(source, game);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getToZone() != Zone.BATTLEFIELD) {
            return false;
        }
        if (zEvent.getFromZone() == Zone.EXILED) {
            Card card = game.getCard(zEvent.getTargetId());
            if (card == null) {
                return false;
            }
            Object entersTransformed = game.getState().getValue(
                    TransformingDoubleFacedCard.VALUE_KEY_ENTER_TRANSFORMED + zEvent.getTargetId() + card.getZoneChangeCounter(game)
            );
            if (entersTransformed instanceof Boolean && (Boolean) entersTransformed && card.getSecondCardFace() != null) {
                card = card.getSecondCardFace();
            }
            return card != null && card.isCreature(game);
        }
        if (zEvent.getFromZone() == Zone.STACK) {
            Spell spell = game.getSpellOrLKIStack(zEvent.getTargetId());
            return spell != null && spell.getFromZone() == Zone.EXILED && spell.isCreature(game);
        }
        return false;
    }
}
