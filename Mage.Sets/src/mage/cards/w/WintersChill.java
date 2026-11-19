package mage.cards.w;

import mage.abilities.Ability;
import mage.abilities.common.CastOnlyDuringPhaseStepSourceAbility;
import mage.abilities.common.delayed.AtTheEndOfCombatDelayedTriggeredAbility;
import mage.abilities.condition.common.BeforeBlockersAreDeclaredCondition;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.PreventionEffectImpl;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.*;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAttackingCreature;
import mage.target.targetadjustment.TargetAdjuster;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class WintersChill extends CardImpl {

    static final FilterControlledLandPermanent filterSnowLands = new FilterControlledLandPermanent("snow lands you control");

    static {
        filterSnowLands.add(SuperType.SNOW.getPredicate());
    }

    public WintersChill(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{X}{U}");

        // Cast this spell only during combat before blockers are declared.
        this.addAbility(new CastOnlyDuringPhaseStepSourceAbility(
                TurnPhase.COMBAT,
                BeforeBlockersAreDeclaredCondition.instance
        ));

        // X can't be greater than the number of snow lands you control.
        this.getSpellAbility().addEffect(new InfoEffect("X can't be greater than the number of snow lands you control"));
        this.getSpellAbility().setCostAdjuster(WintersChillCostAdjuster.instance);

        // Choose X target attacking creatures. For each of those creatures, its controller may pay {1} or {2}. If that player doesn't, destroy that creature at end of combat. If that player pays only {1}, prevent all combat damage that would be dealt to and dealt by that creature this combat.
        this.getSpellAbility().addEffect(new WintersChillEffect());
        this.getSpellAbility().setTargetAdjuster(WintersChillAdjuster.instance);
    }

    private WintersChill(final WintersChill card) {
        super(card);
    }

    @Override
    public WintersChill copy() {
        return new WintersChill(this);
    }
}

enum WintersChillCostAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareX(Ability ability, Game game) {
        int maxSnowLands = game.getBattlefield().count(WintersChill.filterSnowLands, ability.getControllerId(), ability, game);
        ability.setVariableCostsMinMax(0, maxSnowLands);
    }
}

enum WintersChillAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        int xValue = CardUtil.getSourceCostsTag(game, ability, "X", 0);
        if (xValue > 0) {
            ability.addTarget(new TargetAttackingCreature(xValue));
        }
    }
}

class WintersChillEffect extends OneShotEffect {

    WintersChillEffect() {
        super(Outcome.Detriment);
        this.staticText = "choose X target attacking creatures. For each of those creatures, its controller may pay {1} or {2}. "
                + "If that player doesn't, destroy that creature at end of combat. If that player pays only {1}, "
                + "prevent all combat damage that would be dealt to and dealt by that creature this combat";
    }

    private WintersChillEffect(final WintersChillEffect effect) {
        super(effect);
    }

    @Override
    public WintersChillEffect copy() {
        return new WintersChillEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent creature = game.getPermanent(targetId);
            if (creature == null) {
                continue;
            }
            Player controller = game.getPlayer(creature.getControllerId());
            if (controller == null) {
                continue;
            }
            GenericManaCost cost1 = new GenericManaCost(1);
            GenericManaCost cost2 = new GenericManaCost(2);
            boolean canPay1 = cost1.canPay(source, source, controller.getId(), game);
            boolean canPay2 = cost2.canPay(source, source, controller.getId(), game);

            int paid = 0;
            if (canPay1 || canPay2) {
                Choice choice = new ChoiceImpl(true);
                choice.setMessage("Pay for " + creature.getLogName() + "?");
                Set<String> options = new LinkedHashSet<>();
                String optNone = "Pay nothing (destroy at end of combat)";
                String opt1 = "Pay {1} (prevent combat damage to/by it)";
                String opt2 = "Pay {2} (no effect)";
                options.add(optNone);
                if (canPay1) {
                    options.add(opt1);
                }
                if (canPay2) {
                    options.add(opt2);
                }
                choice.setChoices(options);
                controller.choose(Outcome.Benefit, choice, game);
                if (opt2.equals(choice.getChoice()) && cost2.pay(source, game, source, controller.getId(), false, null)) {
                    paid = 2;
                } else if (opt1.equals(choice.getChoice()) && cost1.pay(source, game, source, controller.getId(), false, null)) {
                    paid = 1;
                }
            }

            if (paid == 0) {
                game.addDelayedTriggeredAbility(new AtTheEndOfCombatDelayedTriggeredAbility(
                        new DestroyTargetEffect().setTargetPointer(new FixedTarget(creature, game))
                ), source);
            } else if (paid == 1) {
                game.addEffect(new WintersChillPreventCombatDamageEffect(creature.getId()), source);
            }
        }
        return true;
    }
}

class WintersChillPreventCombatDamageEffect extends PreventionEffectImpl {

    private final UUID creatureId;

    WintersChillPreventCombatDamageEffect(UUID creatureId) {
        super(Duration.EndOfCombat, Integer.MAX_VALUE, true);
        this.creatureId = creatureId;
        this.staticText = "prevent all combat damage that would be dealt to and dealt by that creature this combat";
    }

    private WintersChillPreventCombatDamageEffect(final WintersChillPreventCombatDamageEffect effect) {
        super(effect);
        this.creatureId = effect.creatureId;
    }

    @Override
    public WintersChillPreventCombatDamageEffect copy() {
        return new WintersChillPreventCombatDamageEffect(this);
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return super.applies(event, source, game)
                && (creatureId.equals(event.getTargetId()) || creatureId.equals(event.getSourceId()));
    }
}
