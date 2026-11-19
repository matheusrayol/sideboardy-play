package mage.abilities.keyword;

import mage.abilities.common.CantBeBeamedUpAbility;
import mage.abilities.costs.common.ReturnToHandChosenControlledPermanentCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.cards.Card;
import mage.constants.SpellAbilityCastMode;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledPermanent;

/**
 * Cast from the graveyard for the listed mana cost and return a creature you
 * control to its owner's hand. Exile the spell when it leaves the stack.
 *
 * @author muz
 */
public class BeamMeUpAbility extends CastFromGraveyardAbility {

    public static final Predicate<Permanent> CAN_BE_BEAMED_UP = CantBeBeamedUpPredicate.instance;
    public static final FilterControlledCreaturePermanent FILTER_CREATURE =
            new FilterControlledCreaturePermanent("a creature you control");

    static {
        FILTER_CREATURE.add(CAN_BE_BEAMED_UP);
    }

    public BeamMeUpAbility(Card card, String manaString) {
        super(card, new ManaCostsImpl<>(manaString), SpellAbilityCastMode.BEAM_ME_UP);
        this.addCost(new ReturnToHandChosenControlledPermanentCost(
                new TargetControlledPermanent(1, 1, FILTER_CREATURE, true)
        ));
    }

    private BeamMeUpAbility(final BeamMeUpAbility ability) {
        super(ability);
    }

    @Override
    public BeamMeUpAbility copy() {
        return new BeamMeUpAbility(this);
    }

    @Override
    public String getRule() {
        return name + " <i>(You may cast this card from your graveyard for " + getManaCosts().getText()
            + " if you also return a creature you control to its owner's hand. Then exile this spell.)</i>";
    }
}

enum CantBeBeamedUpPredicate implements Predicate<Permanent> {
    instance;

    @Override
    public boolean apply(Permanent input, Game game) {
        return !input.getAbilities(game).containsClass(CantBeBeamedUpAbility.class);
    }

    @Override
    public String toString() {
        return "can be beamed up";
    }
}
