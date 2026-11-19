package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.LandwalkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceLandType;
import mage.constants.*;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class BarbarianGuides extends CardImpl {

    public BarbarianGuides(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.BARBARIAN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // {2}{R}, {T}: Choose a land type. Target creature you control gains snow landwalk of the chosen type until end of turn. Return that creature to its owner's hand at the beginning of the next end step.
        Ability ability = new SimpleActivatedAbility(new BarbarianGuidesEffect(), new ManaCostsImpl<>("{2}{R}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(ability);
    }

    private BarbarianGuides(final BarbarianGuides card) {
        super(card);
    }

    @Override
    public BarbarianGuides copy() {
        return new BarbarianGuides(this);
    }
}

class BarbarianGuidesEffect extends OneShotEffect {

    BarbarianGuidesEffect() {
        super(Outcome.AddAbility);
        this.staticText = "choose a land type. Target creature you control gains snow landwalk of the chosen type until end of turn. "
                + "Return that creature to its owner's hand at the beginning of the next end step";
    }

    private BarbarianGuidesEffect(final BarbarianGuidesEffect effect) {
        super(effect);
    }

    @Override
    public BarbarianGuidesEffect copy() {
        return new BarbarianGuidesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent targetCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller == null || targetCreature == null) {
            return false;
        }
        Choice landChoice = new ChoiceLandType();
        if (!controller.choose(Outcome.AddAbility, landChoice, game)) {
            return false;
        }
        SubType landSubType = SubType.byDescription(landChoice.getChoice());
        if (landSubType == null) {
            return false;
        }
        if (!game.isSimulation()) {
            game.informPlayers(controller.getLogName() + " chose " + landSubType.getDescription());
        }
        FilterControlledLandPermanent filter = new FilterControlledLandPermanent("snow " + landSubType.getDescription() + "s");
        filter.add(SuperType.SNOW.getPredicate());
        filter.add(landSubType.getPredicate());
        game.addEffect(
                new GainAbilityTargetEffect(new LandwalkAbility(filter, false), Duration.EndOfTurn)
                        .setTargetPointer(new FixedTarget(targetCreature, game)),
                source
        );
        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(
                new ReturnToHandTargetEffect().setTargetPointer(new FixedTarget(targetCreature, game))
        ), source);
        return true;
    }
}
