package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Shahrazad extends CardImpl {

    public Shahrazad(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{W}{W}");

        // Players play a Magic subgame, using their libraries as their decks. Each player who doesn't win the subgame loses half their life, rounded up.
        this.getSpellAbility().addEffect(new ShahrazadEffect());
    }

    private Shahrazad(final Shahrazad card) {
        super(card);
    }

    @Override
    public Shahrazad copy() {
        return new Shahrazad(this);
    }
}

class ShahrazadEffect extends OneShotEffect {

    ShahrazadEffect() {
        super(Outcome.Neutral);
        staticText = "Players play a Magic subgame, using their libraries as their decks. "
                + "Each player who doesn't win the subgame loses half their life, rounded up";
    }

    private ShahrazadEffect(final ShahrazadEffect effect) {
        super(effect);
    }

    @Override
    public ShahrazadEffect copy() {
        return new ShahrazadEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Map<UUID, Integer> rolls = new LinkedHashMap<>();
        int maxRoll = Integer.MIN_VALUE;
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            int roll = player.rollDice(Outcome.Neutral, source, game, 20);
            rolls.put(playerId, roll);
            if (roll > maxRoll) {
                maxRoll = roll;
            }
        }
        for (Map.Entry<UUID, Integer> entry : rolls.entrySet()) {
            if (entry.getValue() < maxRoll) {
                Player player = game.getPlayer(entry.getKey());
                if (player != null) {
                    int loss = (player.getLife() + 1) / 2;
                    if (loss > 0) {
                        player.loseLife(loss, game, source, false);
                    }
                }
            }
        }
        return true;
    }
}
