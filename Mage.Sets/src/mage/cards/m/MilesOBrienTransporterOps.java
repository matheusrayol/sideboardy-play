package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.hint.ConditionHint;
import mage.abilities.hint.Hint;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.common.TargetControlledPermanent;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class MilesOBrienTransporterOps extends CardImpl {

    public MilesOBrienTransporterOps(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Miles O'Brien enters, return up to one other target creature or artifact you control to its owner's hand.
        Ability etb = new EntersBattlefieldTriggeredAbility(new ReturnToHandTargetEffect());
        etb.addTarget(new TargetControlledPermanent(0, 1, StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE_OR_ARTIFACT, false));
        this.addAbility(etb);

        // At the beginning of each end step, if a permanent was returned to your hand from the battlefield this turn, draw a card.
        this.addAbility(
                new BeginningOfEndStepTriggeredAbility(
                        TargetController.ANY,
                        new DrawCardSourceControllerEffect(1),
                        false,
                        MilesOBrienCondition.instance
                ).addHint(MilesOBrienCondition.getHint()),
                new MilesOBrienWatcher()
        );
    }

    private MilesOBrienTransporterOps(final MilesOBrienTransporterOps card) {
        super(card);
    }

    @Override
    public MilesOBrienTransporterOps copy() {
        return new MilesOBrienTransporterOps(this);
    }
}

enum MilesOBrienCondition implements Condition {
    instance;

    private static final Hint hint = new ConditionHint(
            instance, "A permanent was returned to your hand from the battlefield this turn"
    );

    @Override
    public boolean apply(Game game, Ability source) {
        MilesOBrienWatcher watcher = game.getState().getWatcher(MilesOBrienWatcher.class);
        return watcher != null && watcher.didReturnToHand(source.getControllerId());
    }

    @Override
    public String toString() {
        return "a permanent was returned to your hand from the battlefield this turn";
    }

    static Hint getHint() {
        return hint;
    }
}

class MilesOBrienWatcher extends Watcher {

    private final Set<UUID> playersReturnedPermanentToHand = new HashSet<>();

    MilesOBrienWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.ZONE_CHANGE) {
            ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
            if (zEvent.getFromZone() == Zone.BATTLEFIELD && zEvent.getToZone() == Zone.HAND) {
                Card card = game.getCard(zEvent.getTargetId());
                if (card != null) {
                    playersReturnedPermanentToHand.add(card.getOwnerId());
                } else if (zEvent.getTarget() != null) {
                    playersReturnedPermanentToHand.add(zEvent.getTarget().getOwnerId());
                }
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        playersReturnedPermanentToHand.clear();
    }

    boolean didReturnToHand(UUID playerId) {
        return playersReturnedPermanentToHand.contains(playerId);
    }
}
