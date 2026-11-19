package mage.cards.g;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DiscardedCardsEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.BloodToken;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GoramouTheCoralTaken extends CardImpl {

    public GoramouTheCoralTaken(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SHARK);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // When Goramou enters, create a Blood token.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CreateTokenEffect(new BloodToken())));

        // Whenever you discard one or more land cards, you may exile one of them from your graveyard. If you do, put that card onto the battlefield tapped at the beginning of the next end step.
        this.addAbility(new GoramouTriggeredAbility());
    }

    private GoramouTheCoralTaken(final GoramouTheCoralTaken card) {
        super(card);
    }

    @Override
    public GoramouTheCoralTaken copy() {
        return new GoramouTheCoralTaken(this);
    }
}

class GoramouTriggeredAbility extends TriggeredAbilityImpl {

    GoramouTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GoramouExileEffect(), true);
    }

    private GoramouTriggeredAbility(final GoramouTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GoramouTriggeredAbility copy() {
        return new GoramouTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DISCARDED_CARDS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())) {
            return false;
        }
        DiscardedCardsEvent dEvent = (DiscardedCardsEvent) event;
        Cards landCards = new CardsImpl();
        for (UUID cardId : dEvent.getDiscardedCards()) {
            Card card = game.getCard(cardId);
            if (card != null && game.getState().getZone(card.getId()) == Zone.GRAVEYARD && card.isLand(game)) {
                landCards.add(card);
            }
        }
        if (landCards.isEmpty()) {
            return false;
        }
        this.getEffects().setTargetPointer(new FixedTargets(landCards, game));
        return true;
    }

    @Override
    public String getRule() {
        return "Whenever you discard one or more land cards, you may exile one of them from your graveyard. "
                + "If you do, put that card onto the battlefield tapped at the beginning of the next end step.";
    }
}

class GoramouExileEffect extends OneShotEffect {

    GoramouExileEffect() {
        super(Outcome.PutLandInPlay);
        this.staticText = "you may exile one of them from your graveyard. "
                + "If you do, put that card onto the battlefield tapped at the beginning of the next end step";
    }

    private GoramouExileEffect(final GoramouExileEffect effect) {
        super(effect);
    }

    @Override
    public GoramouExileEffect copy() {
        return new GoramouExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards graveyardLands = new CardsImpl();
        for (UUID id : getTargetPointer().getTargets(game, source)) {
            Card card = game.getCard(id);
            if (card != null && game.getState().getZone(card.getId()) == Zone.GRAVEYARD && card.isLand(game)) {
                graveyardLands.add(card);
            }
        }
        if (graveyardLands.isEmpty()) {
            return false;
        }
        Card chosen = null;
        if (graveyardLands.size() == 1) {
            chosen = graveyardLands.getRandom(game);
        } else {
            TargetCard target = new TargetCard(1, Zone.GRAVEYARD, StaticFilters.FILTER_CARD_LAND);
            target.withChooseHint("to exile from your graveyard");
            if (controller.choose(Outcome.Exile, graveyardLands, target, source, game)) {
                chosen = game.getCard(target.getFirstTarget());
            }
        }
        if (chosen == null) {
            return false;
        }
        if (controller.moveCardsToExile(
                chosen, source, game, true,
                CardUtil.getExileZoneId(game, source),
                CardUtil.getSourceName(game, source)
        )) {
            MageObjectReference mor = new MageObjectReference(chosen, game);
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                    new GoramouReturnTappedEffect(mor)
            ), source);
            return true;
        }
        return false;
    }
}

class GoramouReturnTappedEffect extends OneShotEffect {

    private final MageObjectReference cardRef;

    GoramouReturnTappedEffect(MageObjectReference cardRef) {
        super(Outcome.PutLandInPlay);
        this.cardRef = cardRef;
        this.staticText = "put that card onto the battlefield tapped";
    }

    private GoramouReturnTappedEffect(final GoramouReturnTappedEffect effect) {
        super(effect);
        this.cardRef = effect.cardRef;
    }

    @Override
    public GoramouReturnTappedEffect copy() {
        return new GoramouReturnTappedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = cardRef.getCard(game);
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            return false;
        }
        return controller.moveCards(card, Zone.BATTLEFIELD, source, game, true, false, false, null);
    }
}
