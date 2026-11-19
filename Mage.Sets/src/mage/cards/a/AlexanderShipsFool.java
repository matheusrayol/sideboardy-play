package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;
import mage.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AlexanderShipsFool extends CardImpl {

    public AlexanderShipsFool(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.HUMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // {T}: Each player reveals the top card of their library, then draws a card.
        // Alexander deals X damage to a random opponent, where X is the greatest mana value among cards revealed this way.
        this.addAbility(new SimpleActivatedAbility(new AlexanderShipsFoolEffect(), new TapSourceCost()));
    }

    private AlexanderShipsFool(final AlexanderShipsFool card) {
        super(card);
    }

    @Override
    public AlexanderShipsFool copy() {
        return new AlexanderShipsFool(this);
    }
}

class AlexanderShipsFoolEffect extends OneShotEffect {

    AlexanderShipsFoolEffect() {
        super(Outcome.Damage);
        this.staticText = "each player reveals the top card of their library, then draws a card. "
                + "{this} deals X damage to a random opponent, where X is the greatest mana value among cards revealed this way";
    }

    private AlexanderShipsFoolEffect(final AlexanderShipsFoolEffect effect) {
        super(effect);
    }

    @Override
    public AlexanderShipsFoolEffect copy() {
        return new AlexanderShipsFoolEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int maxMv = 0;
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            Card topCard = player.getLibrary().getFromTop(game);
            if (topCard != null) {
                player.revealCards(source, new CardsImpl(topCard), game);
                maxMv = Math.max(maxMv, topCard.getManaValue());
            }
        }
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                player.drawCards(1, source, game);
            }
        }
        List<UUID> opponents = new ArrayList<>();
        for (UUID oppId : game.getOpponents(source.getControllerId())) {
            Player opp = game.getPlayer(oppId);
            if (opp != null && game.getState().getPlayersInRange(source.getControllerId(), game).contains(oppId)) {
                opponents.add(oppId);
            }
        }
        if (!opponents.isEmpty() && maxMv > 0) {
            UUID randomOppId = opponents.get(RandomUtil.nextInt(opponents.size()));
            Player opp = game.getPlayer(randomOppId);
            if (opp != null) {
                opp.damage(maxMv, source.getSourceId(), source, game);
            }
        }
        return true;
    }
}
