package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.keyword.KickerAbility;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.common.TargetOpponentsCreaturePermanent;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class MirrorUniverseIntrusion extends CardImpl {

    public MirrorUniverseIntrusion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{U}{B}");

        // Kicker {4}{U}{B}
        this.addAbility(new KickerAbility("{4}{U}{B}"));

        // Create a token that's a copy of target creature an opponent controls, except it has menace. If this spell was kicked, instead for each creature target opponent controls, create a token that's a copy of that creature, except it has menace.
        this.getSpellAbility().addEffect(new MirrorUniverseIntrusionEffect());
        this.getSpellAbility().addTarget(new TargetOpponentsCreaturePermanent());
        this.getSpellAbility().setTargetAdjuster(MirrorUniverseIntrusionAdjuster.instance);
    }

    private MirrorUniverseIntrusion(final MirrorUniverseIntrusion card) {
        super(card);
    }

    @Override
    public MirrorUniverseIntrusion copy() {
        return new MirrorUniverseIntrusion(this);
    }
}

enum MirrorUniverseIntrusionAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        if (KickedCondition.ONCE.apply(game, ability)) {
            ability.addTarget(new TargetOpponent());
        } else {
            ability.addTarget(new TargetOpponentsCreaturePermanent());
        }
    }
}

class MirrorUniverseIntrusionEffect extends OneShotEffect {

    MirrorUniverseIntrusionEffect() {
        super(Outcome.Copy);
        this.staticText = "Create a token that's a copy of target creature an opponent controls, except it has menace. "
                + "If this spell was kicked, instead for each creature target opponent controls, "
                + "create a token that's a copy of that creature, except it has menace";
    }

    private MirrorUniverseIntrusionEffect(final MirrorUniverseIntrusionEffect effect) {
        super(effect);
    }

    @Override
    public MirrorUniverseIntrusionEffect copy() {
        return new MirrorUniverseIntrusionEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (KickedCondition.ONCE.apply(game, source)) {
            Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
            if (opponent == null) {
                return false;
            }
            for (Permanent permanent : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, opponent.getId(), game)) {
                CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect();
                effect.setSavedPermanent(permanent);
                effect.addAdditionalAbilities(new MenaceAbility(false));
                effect.apply(game, source);
            }
            return true;
        }
        Permanent permanent = game.getPermanentOrLKIBattlefield(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect();
        effect.setSavedPermanent(permanent);
        effect.addAdditionalAbilities(new MenaceAbility(false));
        return effect.apply(game, source);
    }
}
