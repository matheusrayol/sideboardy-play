package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheIgnobleSerpent extends CardImpl {

    private static final FilterCreatureCard seaMonsterFilter = new FilterCreatureCard("sea monster creature card from your graveyard");

    static {
        seaMonsterFilter.add(Predicates.or(
                SubType.KRAKEN.getPredicate(),
                SubType.LEVIATHAN.getPredicate(),
                SubType.OCTOPUS.getPredicate(),
                SubType.SERPENT.getPredicate(),
                SubType.WHALE.getPredicate()
        ));
    }

    public TheIgnobleSerpent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{B}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SERPENT);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(7);
        this.toughness = new MageInt(9);

        // At the beginning of your first upkeep, if "The Ignoble Serpent" is in your command zone, mill four cards.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new MillCardsControllerEffect(4), false
        ).withInterveningIf(TheIgnobleSerpentCondition.instance)
                .setTriggerPhrase("At the beginning of your first upkeep, "),
                new TheIgnobleSerpentWatcher());

        // At the beginning of your end step, return target sea monster creature card from your graveyard to the battlefield.
        Ability endAbility = new BeginningOfEndStepTriggeredAbility(new ReturnFromGraveyardToBattlefieldTargetEffect());
        endAbility.addTarget(new TargetCardInYourGraveyard(seaMonsterFilter));
        this.addAbility(endAbility);
    }

    private TheIgnobleSerpent(final TheIgnobleSerpent card) {
        super(card);
    }

    @Override
    public TheIgnobleSerpent copy() {
        return new TheIgnobleSerpent(this);
    }
}

enum TheIgnobleSerpentCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        if (game.getState().getZone(source.getSourceId()) != Zone.COMMAND) {
            return false;
        }
        TheIgnobleSerpentWatcher watcher = game.getState().getWatcher(TheIgnobleSerpentWatcher.class);
        return watcher != null && watcher.isFirstUpkeep(source.getControllerId());
    }

    @Override
    public String toString() {
        return "it's your first upkeep and {this} is in your command zone";
    }
}

class TheIgnobleSerpentWatcher extends Watcher {

    private final Map<UUID, Integer> upkeepCounts = new HashMap<>();

    TheIgnobleSerpentWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE && event.getPlayerId() != null) {
            upkeepCounts.merge(event.getPlayerId(), 1, Integer::sum);
        }
    }

    boolean isFirstUpkeep(UUID playerId) {
        return upkeepCounts.getOrDefault(playerId, 0) == 1;
    }
}
