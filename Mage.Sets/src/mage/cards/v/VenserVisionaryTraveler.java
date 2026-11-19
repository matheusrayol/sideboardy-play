package mage.cards.v;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.CanBeYourCommanderAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.ExileReturnBattlefieldNextEndStepTargetEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterNonlandPermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentToken;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetadjustment.TargetAdjuster;
import mage.target.targetpointer.EachTargetPointer;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class VenserVisionaryTraveler extends CardImpl {

    public VenserVisionaryTraveler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{3}{W}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VENSER);
        this.setStartingLoyalty(4);

        // Each nontoken creature you control that wasn't cast from your hand enters with two additional +1/+1 counters on it.
        Ability staticAbility = new SimpleStaticAbility(new VenserVisionaryTravelerReplacementEffect());
        staticAbility.addWatcher(new VenserVisionaryTravelerWatcher());
        this.addAbility(staticAbility);

        // +1: Exile up to one other target permanent you control. At the beginning of the next end step, return that card to the battlefield under its owner's control.
        LoyaltyAbility ability1 = new LoyaltyAbility(
                new ExileReturnBattlefieldNextEndStepTargetEffect().withTextThatCard(true),
                1
        );
        ability1.addTarget(new TargetPermanent(0, 1, StaticFilters.FILTER_CONTROLLED_ANOTHER_TARGET_PERMANENT));
        this.addAbility(ability1);

        // −2: For each opponent, return up to one target nonland permanent that player controls to its owner's hand.
        LoyaltyAbility ability2 = new LoyaltyAbility(
                new ReturnToHandTargetEffect()
                        .setTargetPointer(new EachTargetPointer())
                        .setText("for each opponent, return up to one target nonland permanent that player controls to its owner's hand"),
                -2
        );
        ability2.setTargetAdjuster(VenserVisionaryTravelerAdjuster.instance);
        this.addAbility(ability2);

        // Venser, Visionary Traveler can be your commander.
        this.addAbility(CanBeYourCommanderAbility.getInstance());
    }

    private VenserVisionaryTraveler(final VenserVisionaryTraveler card) {
        super(card);
    }

    @Override
    public VenserVisionaryTraveler copy() {
        return new VenserVisionaryTraveler(this);
    }
}

class VenserVisionaryTravelerReplacementEffect extends ReplacementEffectImpl {

    VenserVisionaryTravelerReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        this.staticText = "each nontoken creature you control that wasn't cast from your hand enters with two additional +1/+1 counters on it";
    }

    private VenserVisionaryTravelerReplacementEffect(final VenserVisionaryTravelerReplacementEffect effect) {
        super(effect);
    }

    @Override
    public VenserVisionaryTravelerReplacementEffect copy() {
        return new VenserVisionaryTravelerReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        if (permanent == null
                || permanent instanceof PermanentToken
                || !permanent.isCreature(game)
                || !permanent.isControlledBy(source.getControllerId())) {
            return false;
        }
        VenserVisionaryTravelerWatcher watcher = game.getState().getWatcher(VenserVisionaryTravelerWatcher.class);
        return watcher == null || !watcher.wasCastFromControllerHand(permanent, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent creature = ((EntersTheBattlefieldEvent) event).getTarget();
        if (creature != null) {
            creature.addCounters(CounterType.P1P1.createInstance(2), source.getControllerId(), source, game, event.getAppliedEffects());
        }
        return false;
    }
}

class VenserVisionaryTravelerWatcher extends Watcher {

    private final Set<MageObjectReference> castFromHandByOwner = new HashSet<>();

    VenserVisionaryTravelerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SPELL_CAST && event.getZone() == Zone.HAND) {
            Spell spell = game.getSpellOrLKIStack(event.getTargetId());
            if (spell != null && spell.isControlledBy(event.getPlayerId())) {
                castFromHandByOwner.add(new MageObjectReference(spell.getMainCard(), game));
            }
        }
    }

    boolean wasCastFromControllerHand(Permanent permanent, Game game) {
        // When a creature spell resolves from stack to battlefield, permanent ZCC is +1 compared to spell ZCC (or same card with ZCC-1)
        return castFromHandByOwner.contains(new MageObjectReference(permanent.getMainCard(), game))
                || castFromHandByOwner.contains(new MageObjectReference(permanent.getMainCard().getId(), permanent.getZoneChangeCounter(game) - 1, game));
    }

    @Override
    public void reset() {
        super.reset();
        castFromHandByOwner.clear();
    }
}

enum VenserVisionaryTravelerAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        for (UUID opponentId : game.getOpponents(ability.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            FilterNonlandPermanent filter = new FilterNonlandPermanent("nonland permanent " + opponent.getName() + " controls");
            filter.add(new ControllerIdPredicate(opponentId));
            ability.addTarget(new TargetPermanent(0, 1, filter, false));
        }
    }
}
