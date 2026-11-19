package mage.cards.o;

import mage.MageInt;
import mage.MageIdentifier;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.CastFromGraveyardOnceDuringEachOfYourTurnAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class OverflowSubmatrix extends CardImpl {

    private static final FilterCard filter = new FilterCard("an artifact creature spell");

    static {
        filter.add(CardType.ARTIFACT.getPredicate());
        filter.add(CardType.CREATURE.getPredicate());
    }

    public OverflowSubmatrix(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.BORG);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Once during each of your turns, you may cast an artifact creature spell from your graveyard.
        // If you cast a spell this way, that artifact creature enters with a finality counter on it.
        Ability ability = new CastFromGraveyardOnceDuringEachOfYourTurnAbility(filter);
        this.addAbility(ability, new OverflowSubmatrixWatcher(ability.getOriginalId()));
    }

    private OverflowSubmatrix(final OverflowSubmatrix card) {
        super(card);
    }

    @Override
    public OverflowSubmatrix copy() {
        return new OverflowSubmatrix(this);
    }
}

class OverflowSubmatrixWatcher extends Watcher {

    private final UUID abilityOriginalId;

    OverflowSubmatrixWatcher(UUID abilityOriginalId) {
        super(WatcherScope.GAME);
        this.abilityOriginalId = abilityOriginalId;
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SPELL_CAST
                || !event.hasApprovingIdentifier(MageIdentifier.OnceOnYourTurnCastFromGraveyard)
                || event.getApprovingObject() == null) {
            return;
        }
        Ability approvingAbility = event.getApprovingObject().getApprovingAbility();
        if (approvingAbility == null || !abilityOriginalId.equals(approvingAbility.getOriginalId())) {
            return;
        }
        Spell spell = game.getSpell(event.getTargetId());
        if (spell != null) {
            game.getState().addEffect(
                    new OverflowSubmatrixFinalityEffect(new MageObjectReference(spell, game)),
                    approvingAbility
            );
        }
    }
}

class OverflowSubmatrixFinalityEffect extends ReplacementEffectImpl {

    private final MageObjectReference mor;

    OverflowSubmatrixFinalityEffect(MageObjectReference mor) {
        super(Duration.OneUse, Outcome.Detriment, false);
        this.mor = mor;
        this.staticText = "that artifact creature enters with a finality counter on it";
    }

    private OverflowSubmatrixFinalityEffect(final OverflowSubmatrixFinalityEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public OverflowSubmatrixFinalityEffect copy() {
        return new OverflowSubmatrixFinalityEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Spell spell = game.getSpell(event.getSourceId());
        Spell morSpell = mor.getSpell(game);
        if (morSpell == null) {
            discard();
            return false;
        }
        return spell != null
                && morSpell.getSourceId().equals(spell.getSourceId())
                && morSpell.getZoneChangeCounter(game) == spell.getZoneChangeCounter(game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        if (permanent != null) {
            permanent.addCounters(CounterType.FINALITY.createInstance(), source.getControllerId(), source, game);
        }
        return false;
    }
}
