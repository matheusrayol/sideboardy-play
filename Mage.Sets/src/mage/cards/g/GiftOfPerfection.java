package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GiftOfPerfection extends CardImpl {

    public GiftOfPerfection(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{U}");

        // Each opponent reveals cards from the top of their library until they reveal a creature card.
        // Assimilate one of the creature cards revealed this way.
        // Then each opponent puts the other cards they revealed this way on the bottom of their library in a random order.
        this.getSpellAbility().addEffect(new GiftOfPerfectionEffect());
    }

    private GiftOfPerfection(final GiftOfPerfection card) {
        super(card);
    }

    @Override
    public GiftOfPerfection copy() {
        return new GiftOfPerfection(this);
    }
}

class GiftOfPerfectionEffect extends OneShotEffect {

    GiftOfPerfectionEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "each opponent reveals cards from the top of their library until they reveal a creature card. "
                + "Assimilate one of the creature cards revealed this way. "
                + "Then each opponent puts the other cards they revealed this way on the bottom of their library in a random order. "
                + "<i>(To assimilate a card, put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private GiftOfPerfectionEffect(final GiftOfPerfectionEffect effect) {
        super(effect);
    }

    @Override
    public GiftOfPerfectionEffect copy() {
        return new GiftOfPerfectionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Map<UUID, Cards> revealedByOpponent = new LinkedHashMap<>();
        Cards creatureCards = new CardsImpl();
        for (UUID opponentId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            Cards revealed = new CardsImpl();
            for (Card card : opponent.getLibrary().getCards(game)) {
                revealed.add(card);
                if (card.isCreature(game)) {
                    creatureCards.add(card);
                    break;
                }
            }
            if (!revealed.isEmpty()) {
                opponent.revealCards(source, revealed, game);
                revealedByOpponent.put(opponentId, revealed);
            }
        }
        if (!creatureCards.isEmpty()) {
            TargetCard target = new TargetCard(1, 1, Zone.LIBRARY, StaticFilters.FILTER_CARD_CREATURE);
            target.withNotTarget(true);
            controller.choose(Outcome.PutCreatureInPlay, creatureCards, target, source, game);
            Card chosen = game.getCard(target.getFirstTarget());
            if (chosen != null) {
                game.setEnterWithCounters(chosen.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
                controller.moveCards(chosen, Zone.BATTLEFIELD, source, game);
                Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(chosen, game);
                if (permanent != null) {
                    game.addEffect(new GiftOfPerfectionTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
                }
            }
        }
        for (Map.Entry<UUID, Cards> entry : revealedByOpponent.entrySet()) {
            Player opponent = game.getPlayer(entry.getKey());
            if (opponent == null) {
                continue;
            }
            Cards remaining = entry.getValue();
            remaining.retainZone(Zone.LIBRARY, game);
            if (!remaining.isEmpty()) {
                opponent.putCardsOnBottomOfLibrary(remaining, game, source, false);
            }
        }
        return true;
    }
}

class GiftOfPerfectionTypeEffect extends ContinuousEffectImpl {

    GiftOfPerfectionTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private GiftOfPerfectionTypeEffect(final GiftOfPerfectionTypeEffect effect) {
        super(effect);
    }

    @Override
    public GiftOfPerfectionTypeEffect copy() {
        return new GiftOfPerfectionTypeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            discard();
            return false;
        }
        permanent.addCardType(game, CardType.ARTIFACT);
        permanent.addCardType(game, CardType.CREATURE);
        permanent.removeAllCreatureTypes(game);
        permanent.addSubType(game, SubType.BORG);
        return true;
    }
}
