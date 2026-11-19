package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.hint.ValueHint;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.TargetCard;
import mage.watchers.Watcher;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sideboardy
 */
public final class TheCelestialToymaker extends CardImpl {

    public TheCelestialToymaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ROGUE);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever The Celestial Toymaker attacks, look at the top three cards of your library. Exile any number of them in a face-down pile and the rest in a face-up pile. Defending player chooses one of those piles. Put that pile into your hand.
        this.addAbility(new AttacksTriggeredAbility(
                new TheCelestialToymakerAttackEffect(), false, null, SetTargetPointer.PLAYER
        ));

        // At the beginning of each end step, each opponent loses 2 life for each spell or ability that caused one or more players to guess or to group cards or permanents into a pile this turn.
        Ability endAbility = new BeginningOfEndStepTriggeredAbility(
                TargetController.ANY,
                new LoseLifeOpponentsEffect(TheCelestialToymakerValue.instance)
                        .setText("each opponent loses 2 life for each spell or ability that caused one or more players to guess or to group cards or permanents into a pile this turn"),
                false
        );
        endAbility.addHint(new ValueHint("Pile/guess spells or abilities this turn", TheCelestialToymakerCount.instance));
        endAbility.addWatcher(new TheCelestialToymakerWatcher());
        this.addAbility(endAbility);
    }

    private TheCelestialToymaker(final TheCelestialToymaker card) {
        super(card);
    }

    @Override
    public TheCelestialToymaker copy() {
        return new TheCelestialToymaker(this);
    }
}

class TheCelestialToymakerAttackEffect extends OneShotEffect {

    TheCelestialToymakerAttackEffect() {
        super(Outcome.DrawCard);
        this.staticText = "look at the top three cards of your library. Exile any number of them in a face-down pile "
                + "and the rest in a face-up pile. Defending player chooses one of those piles. Put that pile into your hand";
    }

    private TheCelestialToymakerAttackEffect(final TheCelestialToymakerAttackEffect effect) {
        super(effect);
    }

    @Override
    public TheCelestialToymakerAttackEffect copy() {
        return new TheCelestialToymakerAttackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards topCards = new CardsImpl(controller.getLibrary().getTopCards(game, 3));
        controller.lookAtCards(source, null, topCards, game);

        Cards faceDownPile = new CardsImpl();
        if (!topCards.isEmpty()) {
            TargetCard target = new TargetCard(0, topCards.size(), Zone.LIBRARY, StaticFilters.FILTER_CARD);
            target.withChooseHint("to put in the face-down pile");
            if (controller.choose(Outcome.Neutral, topCards, target, source, game)) {
                for (UUID id : target.getTargets()) {
                    Card c = game.getCard(id);
                    if (c != null) {
                        faceDownPile.add(c);
                    }
                }
            }
        }
        Cards faceUpPile = new CardsImpl();
        for (Card c : topCards.getCards(game)) {
            if (!faceDownPile.contains(c.getId())) {
                faceUpPile.add(c);
            }
        }

        UUID faceDownExileId = UUID.randomUUID();
        UUID faceUpExileId = UUID.randomUUID();
        if (!faceDownPile.isEmpty()) {
            controller.moveCardsToExile(faceDownPile.getCards(game), source, game, false, faceDownExileId, "Face-down pile");
            for (Card c : faceDownPile.getCards(game)) {
                c.setFaceDown(true, game);
            }
        }
        if (!faceUpPile.isEmpty()) {
            controller.moveCardsToExile(faceUpPile.getCards(game), source, game, true, faceUpExileId, "Face-up pile");
        }

        UUID defenderId = game.getCombat().getDefendingPlayerId(source.getSourceId(), game);
        if (defenderId == null) {
            defenderId = getTargetPointer().getFirst(game, source);
        }
        Player defender = game.getPlayer(defenderId);
        if (defender == null) {
            defender = controller;
        }

        String faceUpNames = faceUpPile.isEmpty()
                ? "empty"
                : faceUpPile.getCards(game).stream().map(Card::getName).collect(Collectors.joining(", "));
        String fdLabel = "Face-down pile (" + faceDownPile.size() + " card(s))";
        String fuLabel = "Face-up pile (" + faceUpPile.size() + " card(s): " + faceUpNames + ")";

        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose a pile for " + controller.getName() + " to put into their hand");
        Set<String> options = new LinkedHashSet<>();
        options.add(fdLabel);
        options.add(fuLabel);
        choice.setChoices(options);

        defender.choose(Outcome.Detriment, choice, game);
        boolean chooseFaceDown = fdLabel.equals(choice.getChoice());
        Cards chosenPile = chooseFaceDown ? faceDownPile : faceUpPile;
        game.informPlayers(defender.getLogName() + " chose the " + (chooseFaceDown ? "face-down" : "face-up") + " pile");

        if (!chosenPile.isEmpty()) {
            for (Card c : chosenPile.getCards(game)) {
                c.setFaceDown(false, game);
            }
            controller.moveCards(chosenPile, Zone.HAND, source, game);
        }
        return true;
    }
}

enum TheCelestialToymakerCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        TheCelestialToymakerWatcher watcher = game.getState().getWatcher(TheCelestialToymakerWatcher.class);
        return watcher != null ? watcher.getCount() : 0;
    }

    @Override
    public TheCelestialToymakerCount copy() {
        return this;
    }

    @Override
    public String toString() {
        return "X";
    }

    @Override
    public String getMessage() {
        return "each spell or ability that caused one or more players to guess or to group cards or permanents into a pile this turn";
    }
}

enum TheCelestialToymakerValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return 2 * TheCelestialToymakerCount.instance.calculate(game, sourceAbility, effect);
    }

    @Override
    public TheCelestialToymakerValue copy() {
        return this;
    }

    @Override
    public String toString() {
        return "twice X";
    }

    @Override
    public String getMessage() {
        return "2 life for each spell or ability that caused one or more players to guess or to group cards or permanents into a pile this turn";
    }
}

class TheCelestialToymakerWatcher extends Watcher {

    private int resolvedCount = 0;

    TheCelestialToymakerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.RESOLVING_ABILITY) {
            Optional<Ability> abilityOpt = game.getAbility(event.getTargetId(), event.getSourceId());
            if (abilityOpt.isPresent()) {
                String rule = abilityOpt.get().getRule(true);
                if (rule != null && (rule.toLowerCase(Locale.ENGLISH).contains("pile")
                        || rule.toLowerCase(Locale.ENGLISH).contains("guess"))) {
                    resolvedCount++;
                }
            }
        }
    }

    int getCount() {
        return resolvedCount;
    }

    @Override
    public void reset() {
        super.reset();
        resolvedCount = 0;
    }
}
