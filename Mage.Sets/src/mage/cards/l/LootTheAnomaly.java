package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.ThresholdCondition;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class LootTheAnomaly extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("another creature or planeswalker");

    static {
        filter.add(AnotherPredicate.instance);
        filter.add(Predicates.or(CardType.CREATURE.getPredicate(), CardType.PLANESWALKER.getPredicate()));
    }

    public LootTheAnomaly(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BEAST);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(-2);
        this.toughness = new MageInt(4);

        // If Loot's power is negative, he assigns combat damage as though his power were positive.
        this.addAbility(new SimpleStaticAbility(new LootTheAnomalyPowerEffect()));

        // Threshold — Sacrifice another creature or planeswalker: Loot gets -2/-0 until end of turn. Activate only if there are seven or more cards in your graveyard.
        ActivateIfConditionActivatedAbility ability = new ActivateIfConditionActivatedAbility(
                new BoostSourceEffect(-2, 0, Duration.EndOfTurn),
                new SacrificeTargetCost(filter),
                ThresholdCondition.instance
        );
        ability.setAbilityWord(AbilityWord.THRESHOLD);
        this.addAbility(ability);
    }

    private LootTheAnomaly(final LootTheAnomaly card) {
        super(card);
    }

    @Override
    public LootTheAnomaly copy() {
        return new LootTheAnomaly(this);
    }
}

class LootTheAnomalyPowerEffect extends ContinuousEffectImpl {

    LootTheAnomalyPowerEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.SwitchPT_e, Outcome.Benefit);
        this.staticText = "If {this}'s power is negative, he assigns combat damage as though his power were positive";
    }

    private LootTheAnomalyPowerEffect(final LootTheAnomalyPowerEffect effect) {
        super(effect);
    }

    @Override
    public LootTheAnomalyPowerEffect copy() {
        return new LootTheAnomalyPowerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (game.getTurnStepType() != PhaseStep.FIRST_COMBAT_DAMAGE
                && game.getTurnStepType() != PhaseStep.COMBAT_DAMAGE) {
            return false;
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null && permanent.getPower().getValue() < 0) {
            permanent.addPower(-2 * permanent.getPower().getValue());
            return true;
        }
        return false;
    }
}
