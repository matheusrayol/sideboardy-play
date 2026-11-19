package mage.cards.t;

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
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TasteOfParadise extends CardImpl {

    public TasteOfParadise(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{G}");

        // As an additional cost to cast this spell, you may pay {1}{G} any number of times.
        this.addAbility(new TasteOfParadiseAdditionalCostAbility());

        // You gain 3 life plus an additional 3 life for each additional {1}{G} you paid.
        this.getSpellAbility().addEffect(new TasteOfParadiseEffect());
    }

    private TasteOfParadise(final TasteOfParadise card) {
        super(card);
    }

    @Override
    public TasteOfParadise copy() {
        return new TasteOfParadise(this);
    }
}

class TasteOfParadiseAdditionalCostAbility extends StaticAbility implements OptionalAdditionalSourceCosts {

    static final String TAG = "tasteOfParadiseCount";

    private OptionalAdditionalCost additionalCost;

    TasteOfParadiseAdditionalCostAbility() {
        super(Zone.STACK, null);
        this.additionalCost = makeCost();
        this.setRuleAtTheTop(true);
    }

    private static OptionalAdditionalCost makeCost() {
        OptionalAdditionalCost cost = new OptionalAdditionalCostImpl(
                "Additional {1}{G}",
                "You may pay an additional {1}{G} any number of times as you cast this spell.",
                new ManaCostsImpl<>("{1}{G}")
        );
        cost.setRepeatable(true);
        cost.setCostType(VariableCostType.ADDITIONAL);
        return cost;
    }

    private TasteOfParadiseAdditionalCostAbility(final TasteOfParadiseAdditionalCostAbility ability) {
        super(ability);
        this.additionalCost = ability.additionalCost.copy();
    }

    @Override
    public TasteOfParadiseAdditionalCostAbility copy() {
        return new TasteOfParadiseAdditionalCostAbility(this);
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
        additionalCost.reset();
        while (player.canRespond() && additionalCost.canPay(ability, this, ability.getControllerId(), game)) {
            int currentCount = CardUtil.getSourceCostsTag(game, ability, TAG, 0);
            String times = (currentCount + 1) + (currentCount == 0 ? " time" : " times");
            if (!player.chooseUse(Outcome.GainLife, "Pay additional {1}{G} (" + times + ")?", ability, game)) {
                break;
            }
            additionalCost.activate();
            ability.setCostsTag(TAG, currentCount + 1);
            ability.addManaCostsToPay(new ManaCostsImpl<>("{1}{G}"));
        }
    }

    @Override
    public String getRule() {
        return "As an additional cost to cast this spell, you may pay {1}{G} any number of times.";
    }

    @Override
    public String getCastMessageSuffix() {
        return additionalCost.isActivated() ? additionalCost.getCastSuffixMessage(0) : "";
    }
}

class TasteOfParadiseEffect extends OneShotEffect {

    TasteOfParadiseEffect() {
        super(Outcome.GainLife);
        staticText = "You gain 3 life plus an additional 3 life for each additional {1}{G} you paid";
    }

    private TasteOfParadiseEffect(final TasteOfParadiseEffect effect) {
        super(effect);
    }

    @Override
    public TasteOfParadiseEffect copy() {
        return new TasteOfParadiseEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int extraPaid = CardUtil.getSourceCostsTag(game, source, TasteOfParadiseAdditionalCostAbility.TAG, 0);
        controller.gainLife(3 + 3 * extraPaid, game, source);
        return true;
    }
}
