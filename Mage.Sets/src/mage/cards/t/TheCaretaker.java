package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.keyword.WardAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class TheCaretaker extends CardImpl {

    public TheCaretaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELEMENTAL);
        this.power = new MageInt(5);
        this.toughness = new MageInt(6);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Ward {2}
        this.addAbility(new WardAbility(new ManaCostsImpl<>("{2}")));

        // At the beginning of each end step, if you faced a dilemma this turn, target opponent exiles cards from the top of their library until they exile a nonland card. You may cast that card without paying its mana cost.
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                TargetController.ANY,
                new TheCaretakerEffect(),
                false,
                TheCaretakerCondition.instance
        );
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability, new TheCaretakerWatcher());
    }

    private TheCaretaker(final TheCaretaker card) {
        super(card);
    }

    @Override
    public TheCaretaker copy() {
        return new TheCaretaker(this);
    }
}

enum TheCaretakerCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        TheCaretakerWatcher watcher = game.getState().getWatcher(TheCaretakerWatcher.class);
        return watcher != null && watcher.didFaceDilemma(source.getControllerId());
    }

    @Override
    public String toString() {
        return "you faced a dilemma this turn";
    }
}

class TheCaretakerWatcher extends Watcher {

    private final Set<UUID> facedDilemmaPlayers = new HashSet<>();

    TheCaretakerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getPlayerId() != null && MelkotianAdjudicator.isDilemmaEvent(event, event.getPlayerId(), game)) {
            facedDilemmaPlayers.add(event.getPlayerId());
        }
    }

    @Override
    public void reset() {
        super.reset();
        facedDilemmaPlayers.clear();
    }

    boolean didFaceDilemma(UUID playerId) {
        return facedDilemmaPlayers.contains(playerId);
    }
}

class TheCaretakerEffect extends OneShotEffect {

    TheCaretakerEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "target opponent exiles cards from the top of their library until they exile a nonland card. "
                + "You may cast that card without paying its mana cost";
    }

    private TheCaretakerEffect(final TheCaretakerEffect effect) {
        super(effect);
    }

    @Override
    public TheCaretakerEffect copy() {
        return new TheCaretakerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        Card nonLandCard = null;
        for (Card card : opponent.getLibrary().getCards(game)) {
            opponent.moveCards(card, Zone.EXILED, source, game);
            if (!card.isLand(game)) {
                nonLandCard = card;
                break;
            }
        }
        if (nonLandCard != null) {
            CardUtil.castSpellWithAttributesForFree(controller, source, game, nonLandCard);
        }
        return true;
    }
}
