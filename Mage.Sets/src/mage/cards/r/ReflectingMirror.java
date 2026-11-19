package mage.cards.r;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.FilterSpell;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.other.NumberOfTargetsPredicate;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.TargetSpell;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ReflectingMirror extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("spell with a single target");

    static {
        filter.add(new NumberOfTargetsPredicate(1));
    }

    public ReflectingMirror(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // {X}, {T}: Change the target of target spell with a single target if that target is you. The new target must be a player. X is twice the mana value of that spell.
        Ability ability = new SimpleActivatedAbility(new ReflectingMirrorEffect(), new ManaCostsImpl<>("{X}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetSpell(filter));
        ability.setCostAdjuster(ReflectingMirrorCostAdjuster.instance);
        this.addAbility(ability);
    }

    private ReflectingMirror(final ReflectingMirror card) {
        super(card);
    }

    @Override
    public ReflectingMirror copy() {
        return new ReflectingMirror(this);
    }
}

enum ReflectingMirrorCostAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareX(Ability ability, Game game) {
        if (game.inCheckPlayableState()) {
            int minX = Integer.MAX_VALUE;
            int maxX = 0;
            for (StackObject stackObject : game.getStack()) {
                if (stackObject instanceof Spell && stackObject.getStackAbility().getTargets().size() == 1) {
                    int doubledMv = stackObject.getManaValue() * 2;
                    minX = Math.min(minX, doubledMv);
                    maxX = Math.max(maxX, doubledMv);
                }
            }
            if (minX == Integer.MAX_VALUE) {
                minX = 0;
            }
            ability.setVariableCostsMinMax(minX, Math.max(minX, maxX));
        } else {
            // Defer final {X} amount until after targets are chosen in 601.2f
            ability.setVariableCostsValue(0);
        }
    }

    @Override
    public void increaseCost(Ability ability, Game game) {
        UUID targetId = ability.getTargets().getFirstTarget();
        if (targetId == null) {
            return;
        }
        Spell spell = game.getStack().getSpell(targetId);
        if (spell != null) {
            int xValue = spell.getManaValue() * 2;
            CardUtil.increaseCost(ability, xValue);
            ability.setCostsTag("X", xValue);
        }
    }
}

class ReflectingMirrorEffect extends OneShotEffect {

    ReflectingMirrorEffect() {
        super(Outcome.Neutral);
        staticText = "Change the target of target spell with a single target if that target is you. "
                + "The new target must be a player. X is twice the mana value of that spell.";
    }

    private ReflectingMirrorEffect(final ReflectingMirrorEffect effect) {
        super(effect);
    }

    @Override
    public ReflectingMirrorEffect copy() {
        return new ReflectingMirrorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        StackObject stackObject = game.getStack().getStackObject(getTargetPointer().getFirst(game, source));
        if (stackObject == null) {
            return false;
        }
        Set<UUID> selected = CardUtil.getAllSelectedTargets(stackObject.getStackAbility(), game);
        if (selected.size() == 1 && selected.contains(source.getControllerId())) {
            stackObject.chooseNewTargets(game, source.getControllerId(), true, true, (Predicate<MageItem>) (item, g) -> item instanceof Player);
            return true;
        }
        return false;
    }
}
