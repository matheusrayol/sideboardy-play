package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.UntapAllControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TurnPhase;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.combat.Combat;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MercifulMandate extends CardImpl {

    public MercifulMandate(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // Untap all creatures you control. If it's a combat phase, you may end the combat phase.
        this.getSpellAbility().addEffect(new UntapAllControllerEffect(
                StaticFilters.FILTER_PERMANENT_CREATURE,
                "untap all creatures you control"
        ));
        this.getSpellAbility().addEffect(new MercifulMandateEffect());
    }

    private MercifulMandate(final MercifulMandate card) {
        super(card);
    }

    @Override
    public MercifulMandate copy() {
        return new MercifulMandate(this);
    }
}

class MercifulMandateEffect extends OneShotEffect {

    MercifulMandateEffect() {
        super(Outcome.Benefit);
        this.staticText = "If it's a combat phase, you may end the combat phase. "
                + "<i>(Remove all attackers and blockers from combat. Exile all spells and abilities from the stack, including this spell.)</i>";
    }

    private MercifulMandateEffect(final MercifulMandateEffect effect) {
        super(effect);
    }

    @Override
    public MercifulMandateEffect copy() {
        return new MercifulMandateEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || game.getTurnPhaseType() != TurnPhase.COMBAT) {
            return true;
        }
        if (!controller.chooseUse(Outcome.Benefit, "End the combat phase?", source, game)) {
            return true;
        }
        Combat combat = game.getCombat();
        List<UUID> combatIds = new ArrayList<>();
        combatIds.addAll(combat.getBlockers());
        combatIds.addAll(combat.getAttackers());
        for (UUID id : combatIds) {
            Permanent permanent = game.getPermanent(id);
            if (permanent != null) {
                permanent.removeFromCombat(game);
            }
        }
        combat.endCombat(game);
        if (!game.getStack().isEmpty()) {
            for (StackObject stackObject : new ArrayList<>(game.getStack())) {
                if (stackObject instanceof Spell) {
                    ((Spell) stackObject).moveToExile(null, "", null, game);
                } else if (stackObject instanceof Ability) {
                    game.getStack().remove(stackObject, game);
                }
            }
        }
        return true;
    }
}
