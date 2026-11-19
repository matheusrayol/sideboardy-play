package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.ReturnToHandChosenControlledPermanentCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.BeamMeUpAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ScottyMiracleWorker extends CardImpl {

    public ScottyMiracleWorker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // During your main phase, each card in your graveyard has beam me up.
        // The beam me up cost is equal to that spell's mana cost.
        // Scotty can't be beamed up to cast a spell this way.
        this.addAbility(new SimpleStaticAbility(new ScottyMiracleWorkerEffect()));
    }

    private ScottyMiracleWorker(final ScottyMiracleWorker card) {
        super(card);
    }

    @Override
    public ScottyMiracleWorker copy() {
        return new ScottyMiracleWorker(this);
    }
}

class ScottyMiracleWorkerEffect extends ContinuousEffectImpl {

    ScottyMiracleWorkerEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "during your main phase, each card in your graveyard has beam me up. "
                + "The beam me up cost is equal to that spell's mana cost. "
                + "{this} can't be beamed up to cast a spell this way";
    }

    private ScottyMiracleWorkerEffect(final ScottyMiracleWorkerEffect effect) {
        super(effect);
    }

    @Override
    public ScottyMiracleWorkerEffect copy() {
        return new ScottyMiracleWorkerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!game.isActivePlayer(source.getControllerId()) || !game.isMainPhase()) {
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        FilterControlledCreaturePermanent filter =
                new FilterControlledCreaturePermanent("a creature you control (other than {this})");
        filter.add(Predicates.not(new PermanentIdPredicate(source.getSourceId())));
        filter.add(BeamMeUpAbility.CAN_BE_BEAMED_UP);
        for (Card card : controller.getGraveyard().getCards(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY, game)) {
            if (card.getManaCost().isEmpty()) {
                continue;
            }
            BeamMeUpAbility ability = new BeamMeUpAbility(card, card.getManaCost().getText());
            ability.getCosts().removeIf(ReturnToHandChosenControlledPermanentCost.class::isInstance);
            ability.addCost(new ReturnToHandChosenControlledPermanentCost(
                    new TargetControlledPermanent(1, 1, filter, true)
            ));
            ability.setSourceId(card.getId());
            ability.setControllerId(card.getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
        return true;
    }
}
