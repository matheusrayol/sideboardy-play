package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.MillThenPutInHandEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.WatcherScope;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class FirstContact extends CardImpl {

    public FirstContact(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{G}");

        // Mill four cards. You may put a permanent card from among them into your hand. If this spell is the first spell you've cast this game, you gain 2 life.
        this.getSpellAbility().addEffect(new MillThenPutInHandEffect(4, StaticFilters.FILTER_CARD_PERMANENT));
        this.getSpellAbility().addEffect(new FirstContactEffect());
        this.getSpellAbility().addWatcher(new FirstContactWatcher());
    }

    private FirstContact(final FirstContact card) {
        super(card);
    }

    @Override
    public FirstContact copy() {
        return new FirstContact(this);
    }
}

class FirstContactWatcher extends Watcher {

    private final Map<UUID, Integer> spellsCastThisGame = new HashMap<>();

    FirstContactWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            spellsCastThisGame.merge(event.getPlayerId(), 1, Integer::sum);
        }
    }

    int getSpellsCastCount(UUID playerId) {
        return spellsCastThisGame.getOrDefault(playerId, 0);
    }
}

class FirstContactEffect extends OneShotEffect {

    FirstContactEffect() {
        super(Outcome.GainLife);
        this.staticText = "If this spell is the first spell you've cast this game, you gain 2 life";
    }

    private FirstContactEffect(final FirstContactEffect effect) {
        super(effect);
    }

    @Override
    public FirstContactEffect copy() {
        return new FirstContactEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        FirstContactWatcher watcher = game.getState().getWatcher(FirstContactWatcher.class);
        if (controller != null && watcher != null && watcher.getSpellsCastCount(controller.getId()) == 1) {
            controller.gainLife(2, game, source);
            return true;
        }
        return false;
    }
}
