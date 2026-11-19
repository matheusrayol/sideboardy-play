package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.CostImpl;
import mage.abilities.costs.EarlyTargetCost;
import mage.abilities.costs.mana.VariableManaCost;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.counter.DistributeCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanentAmount;
import mage.target.common.TargetOpponent;
import mage.target.targetadjustment.TargetAdjuster;
import mage.util.CardUtil;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SpoilsOfWar extends CardImpl {

    static final FilterCard filterArtifactOrCreature = new FilterCard("artifact and/or creature cards");

    static {
        filterArtifactOrCreature.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.CREATURE.getPredicate()
        ));
    }

    public SpoilsOfWar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{X}{B}");

        // X is the number of artifact and/or creature cards in an opponent's graveyard as you cast this spell.
        this.getSpellAbility().addCost(new SpoilsOfWarChooseOpponentCost());
        this.getSpellAbility().addEffect(new InfoEffect("X is the number of artifact and/or creature cards in an opponent's graveyard as you cast this spell"));
        this.getSpellAbility().setCostAdjuster(SpoilsOfWarCostAdjuster.instance);

        // Distribute X +1/+1 counters among any number of target creatures.
        this.getSpellAbility().addEffect(new DistributeCountersEffect()
                .setText("Distribute X +1/+1 counters among any number of target creatures"));
        this.getSpellAbility().setTargetAdjuster(SpoilsOfWarTargetAdjuster.instance);
    }

    private SpoilsOfWar(final SpoilsOfWar card) {
        super(card);
    }

    @Override
    public SpoilsOfWar copy() {
        return new SpoilsOfWar(this);
    }
}

class SpoilsOfWarChooseOpponentCost extends CostImpl implements EarlyTargetCost {

    SpoilsOfWarChooseOpponentCost() {
        super();
        this.text = "choose an opponent";
    }

    private SpoilsOfWarChooseOpponentCost(final SpoilsOfWarChooseOpponentCost cost) {
        super(cost);
    }

    @Override
    public SpoilsOfWarChooseOpponentCost copy() {
        return new SpoilsOfWarChooseOpponentCost(this);
    }

    @Override
    public void chooseTarget(Game game, Ability source, Player controller) {
        TargetOpponent target = new TargetOpponent(true);
        target.withChooseHint("to count artifact and/or creature cards in graveyard");
        controller.choose(Outcome.Detriment, target, source, game);
        addTarget(target);
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        return game.getOpponents(controllerId).stream()
                .map(game::getPlayer)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        this.paid = getTargets().getFirstTarget() != null;
        return this.paid;
    }
}

enum SpoilsOfWarCostAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareX(Ability ability, Game game) {
        VariableManaCost costX = ability.getManaCostsToPay().stream()
                .filter(VariableManaCost.class::isInstance)
                .map(VariableManaCost.class::cast)
                .findFirst()
                .orElse(null);
        SpoilsOfWarChooseOpponentCost chooseCost = ability.getCosts().stream()
                .filter(SpoilsOfWarChooseOpponentCost.class::isInstance)
                .map(SpoilsOfWarChooseOpponentCost.class::cast)
                .findFirst()
                .orElse(null);
        if (costX == null || chooseCost == null) {
            return;
        }
        if (game.inCheckPlayableState()) {
            int minVal = Integer.MAX_VALUE;
            int maxVal = 0;
            for (UUID oppId : game.getOpponents(ability.getControllerId(), true)) {
                Player opp = game.getPlayer(oppId);
                if (opp != null) {
                    int cnt = opp.getGraveyard().count(SpoilsOfWar.filterArtifactOrCreature, game);
                    minVal = Math.min(minVal, cnt);
                    maxVal = Math.max(maxVal, cnt);
                }
            }
            ability.setVariableCostsMinMax(minVal == Integer.MAX_VALUE ? 0 : minVal, maxVal);
        } else {
            Player opp = game.getPlayer(chooseCost.getTargets().getFirstTarget());
            int cnt = opp != null ? opp.getGraveyard().count(SpoilsOfWar.filterArtifactOrCreature, game) : 0;
            ability.setVariableCostsValue(cnt);
        }
    }
}

enum SpoilsOfWarTargetAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        int xValue = CardUtil.getSourceCostsTag(game, ability, "X", 0);
        if (xValue > 0) {
            ability.addTarget(new TargetCreaturePermanentAmount(xValue));
        }
    }
}
