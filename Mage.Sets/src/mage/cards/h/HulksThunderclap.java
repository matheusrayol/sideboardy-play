package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.BeholdAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.other.AnotherTargetPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.targetadjustment.TargetAdjuster;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HulksThunderclap extends CardImpl {

    private static final FilterCreaturePermanent filter2 =
            new FilterCreaturePermanent("another target creature");

    static {
        filter2.add(new AnotherTargetPredicate(2));
    }

    public HulksThunderclap(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{G}");

        // As an additional cost to cast this spell, you may behold a Gamma creature.
        this.addAbility(new BeholdAbility(SubType.GAMMA));

        // Target creature you control deals damage equal to its power to another target creature.
        // If this spell's additional cost was paid, destroy target noncreature artifact or noncreature enchantment.
        this.getSpellAbility().addEffect(new HulksThunderclapEffect());
        this.getSpellAbility().addTarget(new TargetControlledCreaturePermanent().setTargetTag(1));
        this.getSpellAbility().addTarget(new TargetPermanent(filter2).setTargetTag(2));
        this.getSpellAbility().setTargetAdjuster(HulksThunderclapAdjuster.instance);
    }

    private HulksThunderclap(final HulksThunderclap card) {
        super(card);
    }

    @Override
    public HulksThunderclap copy() {
        return new HulksThunderclap(this);
    }
}

enum HulksThunderclapAdjuster implements TargetAdjuster {
    instance;

    private static final FilterPermanent noncreatureFilter =
            new FilterPermanent("noncreature artifact or noncreature enchantment");

    static {
        noncreatureFilter.add(Predicates.not(CardType.CREATURE.getPredicate()));
        noncreatureFilter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.ENCHANTMENT.getPredicate()
        ));
    }

    @Override
    public void adjustTargets(Ability ability, Game game) {
        if (CardUtil.checkSourceCostsTagExists(game, ability, BeholdAbility.BEHOLD_ACTIVATION_VALUE_KEY)) {
            ability.addTarget(new TargetPermanent(noncreatureFilter).setTargetTag(3));
        }
    }
}

class HulksThunderclapEffect extends OneShotEffect {

    HulksThunderclapEffect() {
        super(Outcome.Damage);
        this.staticText = "target creature you control deals damage equal to its power to another target creature. "
                + "If this spell's additional cost was paid, destroy target noncreature artifact or noncreature enchantment";
    }

    private HulksThunderclapEffect(final HulksThunderclapEffect effect) {
        super(effect);
    }

    @Override
    public HulksThunderclapEffect copy() {
        return new HulksThunderclapEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature1 = game.getPermanent(source.getTargets().get(0).getFirstTarget());
        Permanent creature2 = game.getPermanent(source.getTargets().get(1).getFirstTarget());
        if (creature1 != null && creature2 != null) {
            creature2.damage(creature1.getPower().getValue(), creature1.getId(), source, game);
        }
        if (source.getTargets().size() > 2) {
            Permanent target3 = game.getPermanent(source.getTargets().get(2).getFirstTarget());
            if (target3 != null) {
                target3.destroy(source, game, false);
            }
        }
        return true;
    }
}
