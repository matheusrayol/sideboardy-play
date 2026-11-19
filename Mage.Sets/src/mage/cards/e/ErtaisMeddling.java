package mage.cards.e;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetSpell;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ErtaisMeddling extends CardImpl {

    public ErtaisMeddling(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{X}{U}");

        // X can't be 0.
        this.getSpellAbility().addEffect(new InfoEffect("X can't be 0"));
        this.getSpellAbility().setCostAdjuster(ErtaisMeddlingCostAdjuster.instance);

        // Target spell's controller exiles it with X delay counters on it.
        // At the beginning of each of that player's upkeeps, if that card is exiled, remove a delay counter from it. If the card has no delay counters on it, the player puts it onto the stack as a copy of the original spell.
        this.getSpellAbility().addEffect(new ErtaisMeddlingEffect());
        this.getSpellAbility().addTarget(new TargetSpell());
    }

    private ErtaisMeddling(final ErtaisMeddling card) {
        super(card);
    }

    @Override
    public ErtaisMeddling copy() {
        return new ErtaisMeddling(this);
    }
}

enum ErtaisMeddlingCostAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareX(Ability ability, Game game) {
        ability.setVariableCostsMinMax(1, Integer.MAX_VALUE);
    }
}

class ErtaisMeddlingEffect extends OneShotEffect {

    ErtaisMeddlingEffect() {
        super(Outcome.Detriment);
        this.staticText = "target spell's controller exiles it with X delay counters on it. "
                + "At the beginning of each of that player's upkeeps, if that card is exiled, remove a delay counter from it. "
                + "If the card has no delay counters on it, the player puts it onto the stack as a copy of the original spell";
    }

    private ErtaisMeddlingEffect(final ErtaisMeddlingEffect effect) {
        super(effect);
    }

    @Override
    public ErtaisMeddlingEffect copy() {
        return new ErtaisMeddlingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell targetSpell = game.getStack().getSpell(getTargetPointer().getFirst(game, source));
        if (targetSpell == null) {
            return false;
        }
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        if (xValue <= 0) {
            return false;
        }
        UUID spellControllerId = targetSpell.getControllerId();
        Player spellController = game.getPlayer(spellControllerId);
        if (spellController == null) {
            return false;
        }
        Spell savedSpellSnapshot = targetSpell.copy();
        Card card = targetSpell.getMainCard();
        UUID exileId = CardUtil.getExileZoneId(game, source);
        if (!spellController.moveCardsToExile(targetSpell, source, game, true, exileId, CardUtil.getSourceName(game, source))) {
            return false;
        }
        if (game.getState().getZone(card.getId()) == Zone.EXILED) {
            card.addCounters(new Counter("delay", xValue), source.getControllerId(), source, game);
            game.addDelayedTriggeredAbility(new ErtaisMeddlingUpkeepDelayedTriggeredAbility(
                    new MageObjectReference(card, game),
                    spellControllerId,
                    savedSpellSnapshot
            ), source);
        }
        return true;
    }
}

class ErtaisMeddlingUpkeepDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference exiledCardMor;
    private final UUID spellControllerId;
    private final Spell originalSpellSnapshot;
    private boolean finished = false;

    ErtaisMeddlingUpkeepDelayedTriggeredAbility(
            MageObjectReference exiledCardMor,
            UUID spellControllerId,
            Spell originalSpellSnapshot
    ) {
        super(new ErtaisMeddlingRemoveCounterEffect(exiledCardMor, spellControllerId, originalSpellSnapshot), Duration.EndOfGame, false, false);
        this.exiledCardMor = exiledCardMor;
        this.spellControllerId = spellControllerId;
        this.originalSpellSnapshot = originalSpellSnapshot;
    }

    private ErtaisMeddlingUpkeepDelayedTriggeredAbility(final ErtaisMeddlingUpkeepDelayedTriggeredAbility ability) {
        super(ability);
        this.exiledCardMor = ability.exiledCardMor;
        this.spellControllerId = ability.spellControllerId;
        this.originalSpellSnapshot = ability.originalSpellSnapshot;
        this.finished = ability.finished;
    }

    @Override
    public ErtaisMeddlingUpkeepDelayedTriggeredAbility copy() {
        return new ErtaisMeddlingUpkeepDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!spellControllerId.equals(event.getPlayerId())) {
            return false;
        }
        Card card = exiledCardMor.getCard(game);
        if (card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            finished = true;
            return false;
        }
        if (card.getCounters(game).getCount("delay") <= 1) {
            finished = true;
        }
        return true;
    }

    @Override
    public boolean isInactive(Game game) {
        return finished || super.isInactive(game);
    }

    @Override
    public String getRule() {
        return "At the beginning of that player's upkeep, remove a delay counter from the exiled card. "
                + "If it has no delay counters on it, they put it onto the stack as a copy of the original spell.";
    }
}

class ErtaisMeddlingRemoveCounterEffect extends OneShotEffect {

    private final MageObjectReference exiledCardMor;
    private final UUID spellControllerId;
    private final Spell originalSpellSnapshot;

    ErtaisMeddlingRemoveCounterEffect(
            MageObjectReference exiledCardMor,
            UUID spellControllerId,
            Spell originalSpellSnapshot
    ) {
        super(Outcome.Neutral);
        this.exiledCardMor = exiledCardMor;
        this.spellControllerId = spellControllerId;
        this.originalSpellSnapshot = originalSpellSnapshot;
        this.staticText = "remove a delay counter from the exiled card. If it has no delay counters on it, "
                + "that player puts it onto the stack as a copy of the original spell";
    }

    private ErtaisMeddlingRemoveCounterEffect(final ErtaisMeddlingRemoveCounterEffect effect) {
        super(effect);
        this.exiledCardMor = effect.exiledCardMor;
        this.spellControllerId = effect.spellControllerId;
        this.originalSpellSnapshot = effect.originalSpellSnapshot;
    }

    @Override
    public ErtaisMeddlingRemoveCounterEffect copy() {
        return new ErtaisMeddlingRemoveCounterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = exiledCardMor.getCard(game);
        if (card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            return false;
        }
        card.removeCounters("delay", 1, source, game);
        if (card.getCounters(game).getCount("delay") == 0 && originalSpellSnapshot != null) {
            originalSpellSnapshot.createCopyOnStack(game, source, spellControllerId, false);
        }
        return true;
    }
}
