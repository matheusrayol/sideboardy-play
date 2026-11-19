package mage.cards.i;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.*;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterPlayer;
import mage.filter.StaticFilters;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class ItazuraLingeringWick extends CardImpl {

    public ItazuraLingeringWick(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPIRIT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(5);

        // At the beginning of your upkeep, exile the top three cards of your library. Each opponent secretly chooses a number 0 or greater. Then those numbers are revealed. Choose an opponent with the highest number. Itazura deals that much damage to them, then they may cast a spell from among those cards without paying its mana cost. You put a card from among them that wasn't cast this way into your hand.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new ItazuraLingeringWickEffect()));
    }

    private ItazuraLingeringWick(final ItazuraLingeringWick card) {
        super(card);
    }

    @Override
    public ItazuraLingeringWick copy() {
        return new ItazuraLingeringWick(this);
    }
}

class ItazuraLingeringWickEffect extends OneShotEffect {

    ItazuraLingeringWickEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "exile the top three cards of your library. Each opponent secretly chooses a number 0 or greater. "
                + "Then those numbers are revealed. Choose an opponent with the highest number. "
                + "{this} deals that much damage to them, then they may cast a spell from among those cards without paying its mana cost. "
                + "You put a card from among them that wasn't cast this way into your hand";
    }

    private ItazuraLingeringWickEffect(final ItazuraLingeringWickEffect effect) {
        super(effect);
    }

    @Override
    public ItazuraLingeringWickEffect copy() {
        return new ItazuraLingeringWickEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards exiled = new CardsImpl(controller.getLibrary().getTopCards(game, 3));
        controller.moveCardsToExile(exiled.getCards(game), source, game, true, CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source));

        Map<UUID, Integer> choices = new LinkedHashMap<>();
        for (UUID opponentId : game.getOpponents(controller.getId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null || !opponent.canRespond()) {
                continue;
            }
            int chosen = opponent.getAmount(0, 1000, "Secretly choose a number 0 or greater", source, game);
            choices.put(opponentId, chosen);
        }

        if (choices.isEmpty()) {
            return true;
        }

        for (Map.Entry<UUID, Integer> entry : choices.entrySet()) {
            Player opponent = game.getPlayer(entry.getKey());
            if (opponent != null) {
                game.informPlayers(opponent.getLogName() + " chose " + entry.getValue());
            }
        }

        int maxAmount = choices.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        Set<UUID> tiedOpponents = new LinkedHashSet<>();
        for (Map.Entry<UUID, Integer> entry : choices.entrySet()) {
            if (entry.getValue() == maxAmount) {
                tiedOpponents.add(entry.getKey());
            }
        }

        UUID chosenOpponentId;
        if (tiedOpponents.size() == 1) {
            chosenOpponentId = tiedOpponents.iterator().next();
        } else {
            FilterPlayer filter = new FilterPlayer("opponent who chose " + maxAmount);
            filter.add(new ItazuraHighestOpponentPredicate(tiedOpponents));
            TargetPlayer target = new TargetPlayer(1, 1, true, filter);
            controller.choose(Outcome.Damage, target, source, game);
            chosenOpponentId = target.getFirstTarget();
            if (chosenOpponentId == null) {
                chosenOpponentId = tiedOpponents.iterator().next();
            }
        }

        Player chosenOpponent = game.getPlayer(chosenOpponentId);
        if (chosenOpponent != null) {
            if (maxAmount > 0) {
                chosenOpponent.damage(maxAmount, source.getSourceId(), source, game);
            }
            exiled.retainZone(Zone.EXILED, game);
            if (!exiled.isEmpty()) {
                CardUtil.castSpellWithAttributesForFree(chosenOpponent, source, game, exiled, StaticFilters.FILTER_CARD);
            }
        }

        exiled.retainZone(Zone.EXILED, game);
        if (!exiled.isEmpty()) {
            Card toHand = null;
            if (exiled.size() == 1) {
                toHand = exiled.getRandom(game);
            } else {
                TargetCard target = new TargetCard(1, 1, Zone.EXILED, StaticFilters.FILTER_CARD);
                target.withChooseHint("to put into your hand");
                if (controller.choose(Outcome.DrawCard, exiled, target, source, game)) {
                    toHand = game.getCard(target.getFirstTarget());
                }
            }
            if (toHand != null) {
                controller.moveCards(toHand, Zone.HAND, source, game);
            }
        }
        return true;
    }
}

class ItazuraHighestOpponentPredicate implements ObjectSourcePlayerPredicate<Player> {

    private final Set<UUID> validIds;

    ItazuraHighestOpponentPredicate(Set<UUID> validIds) {
        this.validIds = validIds;
    }

    @Override
    public boolean apply(ObjectSourcePlayer<Player> input, Game game) {
        return input.getObject() != null && validIds.contains(input.getObject().getId());
    }

    @Override
    public String toString() {
        return "opponent with the highest chosen number";
    }
}
