package mage.cards.o;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.LoseLifeSourceControllerEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.target.targetadjustment.TargetAdjuster;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class OlindaTheOblivious extends CardImpl {

    public OlindaTheOblivious(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SKUNK);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever Olinda enters or attacks, for each opponent, put an odor counter on up to one target creature that player controls without an odor counter on it. Each of those creatures has "At the beginning of your upkeep, you lose 2 life" for as long as it has an odor counter on it.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new OlindaTheObliviousEffect());
        ability.setTargetAdjuster(OlindaTheObliviousAdjuster.instance);
        this.addAbility(ability);
    }

    private OlindaTheOblivious(final OlindaTheOblivious card) {
        super(card);
    }

    @Override
    public OlindaTheOblivious copy() {
        return new OlindaTheOblivious(this);
    }
}

enum OlindaTheObliviousAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        for (UUID opponentId : game.getOpponents(ability.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            FilterCreaturePermanent filter = new FilterCreaturePermanent("creature " + opponent.getName() + " controls without an odor counter on it");
            filter.add(new ControllerIdPredicate(opponentId));
            filter.add((Predicate<Permanent>) (input, g) -> input.getCounters(g).getCount("odor") == 0);
            TargetPermanent target = new TargetPermanent(0, 1, filter, false);
            ability.addTarget(target);
        }
    }
}

class OlindaTheObliviousEffect extends OneShotEffect {

    OlindaTheObliviousEffect() {
        super(Outcome.Detriment);
        this.staticText = "for each opponent, put an odor counter on up to one target creature that player controls without an odor counter on it. "
                + "Each of those creatures has \"At the beginning of your upkeep, you lose 2 life\" for as long as it has an odor counter on it";
    }

    private OlindaTheObliviousEffect(final OlindaTheObliviousEffect effect) {
        super(effect);
    }

    @Override
    public OlindaTheObliviousEffect copy() {
        return new OlindaTheObliviousEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Target target : source.getTargets()) {
            for (UUID targetId : target.getTargets()) {
                Permanent permanent = game.getPermanent(targetId);
                if (permanent == null) {
                    continue;
                }
                if (permanent.addCounters(new Counter("odor"), source.getControllerId(), source, game)) {
                    game.addEffect(
                            new OlindaOdorGainAbilityEffect()
                                    .setTargetPointer(new FixedTarget(permanent, game)),
                            source
                    );
                }
            }
        }
        return true;
    }
}

class OlindaOdorGainAbilityEffect extends ContinuousEffectImpl {

    private final Ability grantedAbility = new BeginningOfUpkeepTriggeredAbility(new LoseLifeSourceControllerEffect(2));

    OlindaOdorGainAbilityEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.Detriment);
        this.staticText = "it has \"At the beginning of your upkeep, you lose 2 life\" for as long as it has an odor counter on it";
    }

    private OlindaOdorGainAbilityEffect(final OlindaOdorGainAbilityEffect effect) {
        super(effect);
    }

    @Override
    public OlindaOdorGainAbilityEffect copy() {
        return new OlindaOdorGainAbilityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            discard();
            return false;
        }
        if (permanent.getCounters(game).getCount("odor") > 0) {
            permanent.addAbility(grantedAbility, source.getSourceId(), game);
            return true;
        }
        return false;
    }
}
