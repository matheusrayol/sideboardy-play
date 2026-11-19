package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.discard.DiscardCardYouChooseTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DownForRepairs extends CardImpl {

    public DownForRepairs(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{B}");

        // Target opponent reveals their hand. You choose a nonland card from it. That player discards that card. Destroy up to one target Attraction that player controls.
        this.getSpellAbility().addEffect(new DiscardCardYouChooseTargetEffect(StaticFilters.FILTER_CARD_NON_LAND));
        this.getSpellAbility().addEffect(new DownForRepairsEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());
    }

    private DownForRepairs(final DownForRepairs card) {
        super(card);
    }

    @Override
    public DownForRepairs copy() {
        return new DownForRepairs(this);
    }
}

class DownForRepairsEffect extends OneShotEffect {

    DownForRepairsEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "Destroy up to one target Attraction that player controls";
    }

    private DownForRepairsEffect(final DownForRepairsEffect effect) {
        super(effect);
    }

    @Override
    public DownForRepairsEffect copy() {
        return new DownForRepairsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        FilterPermanent filter = new FilterPermanent(SubType.ATTRACTION, "Attraction " + opponent.getLogName() + " controls");
        filter.add(new ControllerIdPredicate(opponent.getId()));
        TargetPermanent target = new TargetPermanent(0, 1, filter, true);
        if (controller.choose(Outcome.DestroyPermanent, target, source, game)) {
            Permanent attraction = game.getPermanent(target.getFirstTarget());
            if (attraction != null) {
                attraction.destroy(source, game);
            }
        }
        return true;
    }
}
