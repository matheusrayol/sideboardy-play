package mage.cards.e;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DestroyAllEffect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AttachmentType;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.target.TargetSpell;
import mage.target.common.TargetLandPermanent;

import java.util.Locale;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Equinox extends CardImpl {

    public Equinox(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{W}");

        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Benefit));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted land has "{T}: Counter target spell if it would destroy a land you control."
        Ability gainedAbility = new SimpleActivatedAbility(new EquinoxCounterEffect(), new TapSourceCost());
        gainedAbility.addTarget(new TargetSpell());
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(
                gainedAbility,
                AttachmentType.AURA
        )));
    }

    private Equinox(final Equinox card) {
        super(card);
    }

    @Override
    public Equinox copy() {
        return new Equinox(this);
    }
}

class EquinoxCounterEffect extends OneShotEffect {

    EquinoxCounterEffect() {
        super(Outcome.Detriment);
        this.staticText = "counter target spell if it would destroy a land you control";
    }

    private EquinoxCounterEffect(final EquinoxCounterEffect effect) {
        super(effect);
    }

    @Override
    public EquinoxCounterEffect copy() {
        return new EquinoxCounterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell targetSpell = game.getStack().getSpell(getTargetPointer().getFirst(game, source));
        if (targetSpell == null) {
            return false;
        }
        if (wouldDestroyControlledLand(targetSpell, source.getControllerId(), game)) {
            return game.getStack().counter(targetSpell.getId(), source, game);
        }
        return true;
    }

    private static boolean wouldDestroyControlledLand(Spell spell, UUID controllerId, Game game) {
        Ability ability = spell.getSpellAbility();
        if (ability == null) {
            return false;
        }
        String rule = ability.getRule(true);
        String lower = rule != null ? rule.toLowerCase(Locale.ENGLISH) : "";

        boolean hasDestroyEffect = lower.contains("destroy");
        for (UUID modeId : ability.getModes().getSelectedModes()) {
            Mode mode = ability.getModes().get(modeId);
            if (mode == null) {
                continue;
            }
            for (Effect effect : mode.getEffects()) {
                if (effect instanceof DestroyTargetEffect || effect instanceof DestroyAllEffect) {
                    hasDestroyEffect = true;
                }
            }
            if (hasDestroyEffect) {
                for (Target target : mode.getTargets()) {
                    for (UUID targetId : target.getTargets()) {
                        Permanent perm = game.getPermanent(targetId);
                        if (perm != null && perm.isLand(game) && perm.isControlledBy(controllerId)) {
                            return true;
                        }
                    }
                }
            }
        }

        if (hasDestroyEffect && game.getBattlefield().count(StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND, controllerId, ability, game) > 0) {
            if (lower.contains("destroy all lands")
                    || lower.contains("destroy each land")
                    || lower.contains("destroy all permanents")
                    || lower.contains("destroy all noncreature permanents")) {
                return true;
            }
        }
        return false;
    }
}
