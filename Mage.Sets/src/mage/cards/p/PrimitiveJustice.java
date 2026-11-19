package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.StaticAbility;
import mage.abilities.costs.OptionalAdditionalCost;
import mage.abilities.costs.OptionalAdditionalCostImpl;
import mage.abilities.costs.OptionalAdditionalSourceCosts;
import mage.abilities.costs.VariableCostType;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetArtifactPermanent;
import mage.target.targetadjustment.TargetAdjuster;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PrimitiveJustice extends CardImpl {

    public PrimitiveJustice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{R}");

        // As an additional cost to cast this spell, you may pay {1}{R} and/or {1}{G} any number of times.
        this.addAbility(new PrimitiveJusticeAdditionalCostAbility());

        // Destroy target artifact. For each additional {1}{R} you paid, destroy another target artifact. For each additional {1}{G} you paid, destroy another target artifact, and you gain 1 life.
        this.getSpellAbility().addEffect(new PrimitiveJusticeEffect());
        this.getSpellAbility().addTarget(new TargetArtifactPermanent());
        this.getSpellAbility().setTargetAdjuster(PrimitiveJusticeTargetAdjuster.instance);
    }

    private PrimitiveJustice(final PrimitiveJustice card) {
        super(card);
    }

    @Override
    public PrimitiveJustice copy() {
        return new PrimitiveJustice(this);
    }
}

class PrimitiveJusticeAdditionalCostAbility extends StaticAbility implements OptionalAdditionalSourceCosts {

    static final String RED_TAG = "primitiveJusticeRed";
    static final String GREEN_TAG = "primitiveJusticeGreen";

    private OptionalAdditionalCost redCost;
    private OptionalAdditionalCost greenCost;

    PrimitiveJusticeAdditionalCostAbility() {
        super(Zone.STACK, null);
        this.redCost = createCost("{1}{R}");
        this.greenCost = createCost("{1}{G}");
        this.setRuleAtTheTop(true);
    }

    private static OptionalAdditionalCost createCost(String mana) {
        OptionalAdditionalCost cost = new OptionalAdditionalCostImpl(
                "Additional " + mana,
                "You may pay an additional " + mana + " any number of times as you cast this spell.",
                new ManaCostsImpl<>(mana)
        );
        cost.setRepeatable(true);
        cost.setCostType(VariableCostType.ADDITIONAL);
        return cost;
    }

    private PrimitiveJusticeAdditionalCostAbility(final PrimitiveJusticeAdditionalCostAbility ability) {
        super(ability);
        this.redCost = ability.redCost.copy();
        this.greenCost = ability.greenCost.copy();
    }

    @Override
    public PrimitiveJusticeAdditionalCostAbility copy() {
        return new PrimitiveJusticeAdditionalCostAbility(this);
    }

    @Override
    public void addOptionalAdditionalCosts(Ability ability, Game game) {
        if (!(ability instanceof SpellAbility)) {
            return;
        }
        Player player = game.getPlayer(ability.getControllerId());
        if (player == null) {
            return;
        }
        redCost.reset();
        greenCost.reset();
        promptRepeatable(redCost, "{1}{R}", RED_TAG, ability, player, game);
        promptRepeatable(greenCost, "{1}{G}", GREEN_TAG, ability, player, game);
    }

    private void promptRepeatable(OptionalAdditionalCost cost, String mana, String tag, Ability ability, Player player, Game game) {
        while (player.canRespond() && cost.canPay(ability, this, ability.getControllerId(), game)) {
            int currentCount = CardUtil.getSourceCostsTag(game, ability, tag, 0);
            String times = (currentCount + 1) + (currentCount == 0 ? " time" : " times");
            if (!player.chooseUse(Outcome.AIDontUseIt, "Pay additional " + mana + " (" + times + ")?", ability, game)) {
                break;
            }
            cost.activate();
            ability.setCostsTag(tag, currentCount + 1);
            ability.addManaCostsToPay(new ManaCostsImpl<>(mana));
        }
    }

    @Override
    public String getRule() {
        return "As an additional cost to cast this spell, you may pay {1}{R} and/or {1}{G} any number of times.";
    }

    @Override
    public String getCastMessageSuffix() {
        StringBuilder sb = new StringBuilder();
        if (redCost.isActivated()) {
            sb.append(redCost.getCastSuffixMessage(0));
        }
        if (greenCost.isActivated()) {
            sb.append(greenCost.getCastSuffixMessage(redCost.isActivated() ? 1 : 0));
        }
        return sb.toString();
    }
}

enum PrimitiveJusticeTargetAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        int redCount = CardUtil.getSourceCostsTag(game, ability, PrimitiveJusticeAdditionalCostAbility.RED_TAG, 0);
        int greenCount = CardUtil.getSourceCostsTag(game, ability, PrimitiveJusticeAdditionalCostAbility.GREEN_TAG, 0);
        int totalTargets = 1 + redCount + greenCount;
        ability.getTargets().clear();
        ability.addTarget(new TargetArtifactPermanent(totalTargets));
    }
}

class PrimitiveJusticeEffect extends OneShotEffect {

    PrimitiveJusticeEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "Destroy target artifact. For each additional {1}{R} you paid, destroy another target artifact. "
                + "For each additional {1}{G} you paid, destroy another target artifact, and you gain 1 life.";
    }

    private PrimitiveJusticeEffect(final PrimitiveJusticeEffect effect) {
        super(effect);
    }

    @Override
    public PrimitiveJusticeEffect copy() {
        return new PrimitiveJusticeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent artifact = game.getPermanent(targetId);
            if (artifact != null) {
                artifact.destroy(source, game);
            }
        }
        Player controller = game.getPlayer(source.getControllerId());
        int greenCount = CardUtil.getSourceCostsTag(game, source, PrimitiveJusticeAdditionalCostAbility.GREEN_TAG, 0);
        if (controller != null && greenCount > 0) {
            controller.gainLife(greenCount, game, source);
        }
        return true;
    }
}
