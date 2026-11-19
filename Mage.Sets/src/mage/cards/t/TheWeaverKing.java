package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.ShadowAbility;
import mage.cards.*;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.watchers.Watcher;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class TheWeaverKing extends CardImpl {

    public TheWeaverKing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HORROR);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(6);

        // Shadow
        this.addAbility(ShadowAbility.getInstance());

        // Whenever The Weaver King deals combat damage to a player, they mill that many cards. Then for each opponent, put a creature card from that player's graveyard that was put there from their library this turn onto the battlefield under your control.
        Ability ability = new DealsCombatDamageToAPlayerTriggeredAbility(
                new TheWeaverKingEffect(), false, true
        );
        ability.addWatcher(new TheWeaverKingWatcher());
        this.addAbility(ability);
    }

    private TheWeaverKing(final TheWeaverKing card) {
        super(card);
    }

    @Override
    public TheWeaverKing copy() {
        return new TheWeaverKing(this);
    }
}

class TheWeaverKingEffect extends OneShotEffect {

    TheWeaverKingEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "they mill that many cards. Then for each opponent, put a creature card from that player's graveyard "
                + "that was put there from their library this turn onto the battlefield under your control";
    }

    private TheWeaverKingEffect(final TheWeaverKingEffect effect) {
        super(effect);
    }

    @Override
    public TheWeaverKingEffect copy() {
        return new TheWeaverKingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        int damage = (Integer) getValue("damage");
        if (controller == null) {
            return false;
        }
        if (damagedPlayer != null && damage > 0) {
            damagedPlayer.millCards(damage, source, game);
        }
        TheWeaverKingWatcher watcher = game.getState().getWatcher(TheWeaverKingWatcher.class);
        if (watcher == null) {
            return true;
        }
        Cards toReturn = new CardsImpl();
        for (UUID opponentId : game.getOpponents(controller.getId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            Cards eligible = new CardsImpl();
            for (Card card : opponent.getGraveyard().getCards(game)) {
                if (card.isCreature(game) && watcher.wasPutFromLibraryThisTurn(card, game)) {
                    eligible.add(card);
                }
            }
            if (eligible.isEmpty()) {
                continue;
            }
            if (eligible.size() == 1) {
                toReturn.addAll(eligible);
            } else {
                FilterCard filter = new FilterCreatureCard("creature card from " + opponent.getName() + "'s graveyard put there from their library this turn");
                filter.add((Predicate<Card>) (input, g) -> eligible.contains(input.getId()));
                TargetCard target = new TargetCard(1, 1, Zone.GRAVEYARD, filter);
                target.withNotTarget(true);
                if (controller.choose(Outcome.PutCreatureInPlay, eligible, target, source, game)) {
                    toReturn.add(target.getFirstTarget());
                }
            }
        }
        if (!toReturn.isEmpty()) {
            controller.moveCards(toReturn, Zone.BATTLEFIELD, source, game);
        }
        return true;
    }
}

class TheWeaverKingWatcher extends Watcher {

    private final Set<MageObjectReference> milledToGraveyard = new HashSet<>();

    TheWeaverKingWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.ZONE_CHANGE) {
            return;
        }
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getFromZone() == Zone.LIBRARY && zEvent.getToZone() == Zone.GRAVEYARD) {
            Card card = game.getCard(zEvent.getTargetId());
            if (card != null) {
                milledToGraveyard.add(new MageObjectReference(card.getMainCard(), game));
            }
        }
    }

    boolean wasPutFromLibraryThisTurn(Card card, Game game) {
        return card != null && milledToGraveyard.contains(new MageObjectReference(card.getMainCard(), game));
    }

    @Override
    public void reset() {
        super.reset();
        milledToGraveyard.clear();
    }
}
