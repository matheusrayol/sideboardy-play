package mage.cards.u;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.StaticAbility;
import mage.abilities.costs.OptionalAdditionalCost;
import mage.abilities.costs.OptionalAdditionalCostImpl;
import mage.abilities.costs.OptionalAdditionalSourceCosts;
import mage.abilities.costs.VariableCostType;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.PreventAllDamageByAllPermanentsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Undergrowth extends CardImpl {

    public Undergrowth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{G}");

        // As an additional cost to cast this spell, you may pay {2}{R}.
        this.addAbility(new UndergrowthAdditionalCostAbility());

        // Prevent all combat damage that would be dealt this turn. If this spell's additional cost was paid, this effect doesn't affect combat damage that would be dealt by red creatures.
        this.getSpellAbility().addEffect(new UndergrowthEffect());
    }

    private Undergrowth(final Undergrowth card) {
        super(card);
    }

    @Override
    public Undergrowth copy() {
        return new Undergrowth(this);
    }
}

class UndergrowthAdditionalCostAbility extends StaticAbility implements OptionalAdditionalSourceCosts {

    static final String TAG = "undergrowthPaid";

    private OptionalAdditionalCost additionalCost;

    UndergrowthAdditionalCostAbility() {
        super(Zone.STACK, null);
        this.additionalCost = makeCost();
        this.setRuleAtTheTop(true);
    }

    private static OptionalAdditionalCost makeCost() {
        OptionalAdditionalCost cost = new OptionalAdditionalCostImpl(
                "Additional {2}{R}",
                "You may pay an additional {2}{R} as you cast this spell.",
                new ManaCostsImpl<>("{2}{R}")
        );
        cost.setRepeatable(false);
        cost.setCostType(VariableCostType.ADDITIONAL);
        return cost;
    }

    private UndergrowthAdditionalCostAbility(final UndergrowthAdditionalCostAbility ability) {
        super(ability);
        this.additionalCost = ability.additionalCost.copy();
    }

    @Override
    public UndergrowthAdditionalCostAbility copy() {
        return new UndergrowthAdditionalCostAbility(this);
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
        if (additionalCost.canPay(ability, this, ability.getControllerId(), game)
                && player.chooseUse(Outcome.AIDontUseIt, "Pay additional {2}{R} (to not prevent combat damage dealt by red creatures)?", ability, game)) {
            additionalCost.activate();
            ability.setCostsTag(TAG, 1);
            ability.addManaCostsToPay(new ManaCostsImpl<>("{2}{R}"));
        }
    }

    @Override
    public String getRule() {
        return "As an additional cost to cast this spell, you may pay {2}{R}.";
    }

    @Override
    public String getCastMessageSuffix() {
        return additionalCost.isActivated() ? additionalCost.getCastSuffixMessage(0) : "";
    }
}

class UndergrowthEffect extends OneShotEffect {

    private static final FilterCreaturePermanent filterNonRed = new FilterCreaturePermanent("nonred creatures");

    static {
        filterNonRed.add(Predicates.not(new ColorPredicate(ObjectColor.RED)));
    }

    UndergrowthEffect() {
        super(Outcome.PreventDamage);
        staticText = "Prevent all combat damage that would be dealt this turn. If this spell's additional cost was paid, "
                + "this effect doesn't affect combat damage that would be dealt by red creatures";
    }

    private UndergrowthEffect(final UndergrowthEffect effect) {
        super(effect);
    }

    @Override
    public UndergrowthEffect copy() {
        return new UndergrowthEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        boolean paid = CardUtil.getSourceCostsTag(game, source, UndergrowthAdditionalCostAbility.TAG, 0) > 0;
        if (paid) {
            game.addEffect(new PreventAllDamageByAllPermanentsEffect(filterNonRed, Duration.EndOfTurn, true), source);
        } else {
            game.addEffect(new PreventAllDamageByAllPermanentsEffect(Duration.EndOfTurn, true), source);
        }
        return true;
    }
}
