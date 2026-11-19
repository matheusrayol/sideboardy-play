package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInHandEffect;
import mage.abilities.effects.common.turn.AddExtraTurnControllerEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HMSBountyUnlikelyTimeship extends CardImpl {

    private static final FilterCard whaleCardFilter = new FilterCard("a Whale card");

    static {
        whaleCardFilter.add(SubType.WHALE.getPredicate());
    }

    public HMSBountyUnlikelyTimeship(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // When H.M.S. Bounty enters, you may search your library for a Whale card, reveal it, put it into your hand, then shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new SearchLibraryPutInHandEffect(new TargetCardInLibrary(whaleCardFilter), true), true
        ));

        // Station
        this.addAbility(new StationAbility());

        // STATION 8+
        // Flying
        // Whenever H.M.S. Bounty deals combat damage to a player, draw a card. If you control a Whale and haven't begun an extra turn this game, take an extra turn after this one.
        // 3/4
        this.addAbility(new StationLevelAbility(8)
                .withLevelAbility(FlyingAbility.getInstance())
                .withLevelAbility(new DealsCombatDamageToAPlayerTriggeredAbility(new HMSBountyEffect(), false))
                .withPT(3, 4), new HMSBountyExtraTurnWatcher());
    }

    private HMSBountyUnlikelyTimeship(final HMSBountyUnlikelyTimeship card) {
        super(card);
    }

    @Override
    public HMSBountyUnlikelyTimeship copy() {
        return new HMSBountyUnlikelyTimeship(this);
    }
}

class HMSBountyEffect extends OneShotEffect {

    private static final FilterControlledPermanent whaleFilter =
            new FilterControlledPermanent(SubType.WHALE, "Whale");

    HMSBountyEffect() {
        super(Outcome.DrawCard);
        this.staticText = "draw a card. If you control a Whale and haven't begun an extra turn this game, take an extra turn after this one";
    }

    private HMSBountyEffect(final HMSBountyEffect effect) {
        super(effect);
    }

    @Override
    public HMSBountyEffect copy() {
        return new HMSBountyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        HMSBountyExtraTurnWatcher watcher = game.getState().getWatcher(HMSBountyExtraTurnWatcher.class);
        if (game.getBattlefield().contains(whaleFilter, source, game, 1)
                && watcher != null
                && !watcher.hasBegunExtraTurn(controller.getId())) {
            new AddExtraTurnControllerEffect().apply(game, source);
        }
        return true;
    }
}

class HMSBountyExtraTurnWatcher extends Watcher {

    private final Set<UUID> playersWithExtraTurn = new HashSet<>();

    HMSBountyExtraTurnWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.BEGIN_TURN && game.getState().isExtraTurn()) {
            playersWithExtraTurn.add(event.getPlayerId());
        }
    }

    boolean hasBegunExtraTurn(UUID playerId) {
        return playersWithExtraTurn.contains(playerId);
    }
}
