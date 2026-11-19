package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ReedRichardsSmartestMan extends CardImpl {

    public ReedRichardsSmartestMan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // You have no maximum hand size.
        this.addAbility(new SimpleStaticAbility(new MaximumHandSizeControllerEffect(
                Integer.MAX_VALUE, Duration.WhileOnBattlefield,
                MaximumHandSizeControllerEffect.HandSizeModification.SET
        )));

        // The first time you would draw a card each turn except the first card you draw during each of your draw steps, you draw four cards instead.
        this.addAbility(new SimpleStaticAbility(new ReedRichardsReplacementEffect()), new ReedRichardsWatcher());
    }

    private ReedRichardsSmartestMan(final ReedRichardsSmartestMan card) {
        super(card);
    }

    @Override
    public ReedRichardsSmartestMan copy() {
        return new ReedRichardsSmartestMan(this);
    }
}

class ReedRichardsReplacementEffect extends ReplacementEffectImpl {

    ReedRichardsReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.DrawCard);
        staticText = "The first time you would draw a card each turn except the first card you draw during each of your draw steps, you draw four cards instead";
    }

    private ReedRichardsReplacementEffect(final ReedRichardsReplacementEffect effect) {
        super(effect);
    }

    @Override
    public ReedRichardsReplacementEffect copy() {
        return new ReedRichardsReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        UUID controllerId = source.getControllerId();
        ReedRichardsWatcher watcher = game.getState().getWatcher(ReedRichardsWatcher.class);
        if (watcher != null && controllerId != null) {
            watcher.markQualifiedDraw(controllerId);
        }
        Player controller = game.getPlayer(controllerId);
        if (controller != null) {
            controller.drawCards(4, source, game, event);
        }
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        UUID playerId = event.getPlayerId();
        if (playerId == null || !playerId.equals(source.getControllerId())) {
            return false;
        }
        ReedRichardsWatcher watcher = game.getState().getWatcher(ReedRichardsWatcher.class);
        if (watcher == null || watcher.hasQualifiedDrawOccurred(playerId)) {
            return false;
        }
        if (game.isActivePlayer(playerId)
                && game.getPhase() != null
                && game.getPhase().getStep().getType() == PhaseStep.DRAW
                && !watcher.hasDrawnFirstInOwnDrawStep(playerId)) {
            return false;
        }
        return true;
    }
}

class ReedRichardsWatcher extends Watcher {

    private final Set<UUID> drewFirstInOwnDrawStep = new HashSet<>();
    private final Set<UUID> qualifiedDrawOccurred = new HashSet<>();

    ReedRichardsWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DREW_CARD) {
            UUID playerId = event.getPlayerId();
            if (playerId == null) {
                return;
            }
            if (game.isActivePlayer(playerId)
                    && game.getPhase() != null
                    && game.getPhase().getStep().getType() == PhaseStep.DRAW
                    && drewFirstInOwnDrawStep.add(playerId)) {
                return;
            }
            qualifiedDrawOccurred.add(playerId);
        }
    }

    boolean hasDrawnFirstInOwnDrawStep(UUID playerId) {
        return drewFirstInOwnDrawStep.contains(playerId);
    }

    boolean hasQualifiedDrawOccurred(UUID playerId) {
        return qualifiedDrawOccurred.contains(playerId);
    }

    void markQualifiedDraw(UUID playerId) {
        qualifiedDrawOccurred.add(playerId);
    }

    @Override
    public void reset() {
        super.reset();
        drewFirstInOwnDrawStep.clear();
        qualifiedDrawOccurred.clear();
    }
}
