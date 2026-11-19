package mage.cards.p;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PavelChekovFirstPosting extends CardImpl {

    public PavelChekovFirstPosting(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // If you would draw a card, you may instead choose land or nonland and reveal cards from the top of your library until you reveal a card of the chosen kind.
        // Put that card into your hand and put all other cards revealed this way on the bottom of your library in a random order.
        this.addAbility(new SimpleStaticAbility(new PavelChekovReplacementEffect()));
    }

    private PavelChekovFirstPosting(final PavelChekovFirstPosting card) {
        super(card);
    }

    @Override
    public PavelChekovFirstPosting copy() {
        return new PavelChekovFirstPosting(this);
    }
}

class PavelChekovReplacementEffect extends ReplacementEffectImpl {

    PavelChekovReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        this.staticText = "If you would draw a card, you may instead choose land or nonland and reveal cards "
                + "from the top of your library until you reveal a card of the chosen kind. "
                + "Put that card into your hand and put all other cards revealed this way on the bottom of your library in a random order";
    }

    private PavelChekovReplacementEffect(final PavelChekovReplacementEffect effect) {
        super(effect);
    }

    @Override
    public PavelChekovReplacementEffect copy() {
        return new PavelChekovReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!event.getPlayerId().equals(source.getControllerId())) {
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller == null || sourceObject == null) {
            return false;
        }
        return controller.chooseUse(
                Outcome.Benefit,
                "Choose:",
                sourceObject.getLogName(),
                "land or nonland and reveal cards from the top",
                "normal card draw",
                source,
                game
        );
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(event.getPlayerId());
        MageObject sourceObject = game.getObject(source);
        if (controller == null || sourceObject == null) {
            return true;
        }
        FilterCard filter = new FilterCard();
        if (controller.chooseUse(
                Outcome.Benefit,
                "Choose card type:",
                sourceObject.getLogName(),
                "land",
                "nonland",
                source,
                game
        )) {
            game.informPlayers(controller.getLogName() + " chooses land.");
            filter.add(CardType.LAND.getPredicate());
        } else {
            game.informPlayers(controller.getLogName() + " chooses nonland.");
            filter.add(Predicates.not(CardType.LAND.getPredicate()));
        }
        Cards toReveal = new CardsImpl();
        Card selectedCard = null;
        for (Card card : controller.getLibrary().getCards(game)) {
            toReveal.add(card);
            if (filter.match(card, source.getControllerId(), source, game)) {
                selectedCard = card;
                break;
            }
        }
        if (!toReveal.isEmpty()) {
            controller.revealCards(sourceObject.getIdName(), toReveal, game);
        }
        if (selectedCard != null) {
            controller.moveCards(selectedCard, Zone.HAND, source, game);
            toReveal.remove(selectedCard);
        }
        toReveal.retainZone(Zone.LIBRARY, game);
        if (!toReveal.isEmpty()) {
            controller.putCardsOnBottomOfLibrary(toReveal, game, source, false);
        }
        return true;
    }
}
