package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlTargetEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class TheGuardianOfForever extends CardImpl {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent("another nontoken creature");

    static {
        filter.add(AnotherPredicate.instance);
        filter.add(TokenPredicate.FALSE);
    }

    public TheGuardianOfForever(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(6);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // {4}, Sacrifice another nontoken creature: Return that card to the battlefield under its owner's control at the beginning of the next end step.
        Ability ability = new SimpleActivatedAbility(new TheGuardianOfForeverEffect(), new GenericManaCost(4));
        ability.addCost(new SacrificeTargetCost(filter));
        this.addAbility(ability);
    }

    private TheGuardianOfForever(final TheGuardianOfForever card) {
        super(card);
    }

    @Override
    public TheGuardianOfForever copy() {
        return new TheGuardianOfForever(this);
    }
}

class TheGuardianOfForeverEffect extends OneShotEffect {

    TheGuardianOfForeverEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "return that card to the battlefield under its owner's control at the beginning of the next end step";
    }

    private TheGuardianOfForeverEffect(final TheGuardianOfForeverEffect effect) {
        super(effect);
    }

    @Override
    public TheGuardianOfForeverEffect copy() {
        return new TheGuardianOfForeverEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        boolean scheduled = false;
        for (Cost cost : source.getCosts()) {
            if (cost instanceof SacrificeTargetCost) {
                for (Permanent sacrificed : ((SacrificeTargetCost) cost).getPermanents()) {
                    Card card = game.getCard(sacrificed.getId());
                    if (card != null
                            && card.getZoneChangeCounter(game) == sacrificed.getZoneChangeCounter(game) + 1
                            && game.getState().getZone(card.getId()) != Zone.BATTLEFIELD) {
                        Effect returnEffect = new ReturnToBattlefieldUnderOwnerControlTargetEffect(false, false);
                        returnEffect.setTargetPointer(new FixedTarget(card, game));
                        game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(returnEffect), source);
                        scheduled = true;
                    }
                }
            }
        }
        return scheduled;
    }
}
