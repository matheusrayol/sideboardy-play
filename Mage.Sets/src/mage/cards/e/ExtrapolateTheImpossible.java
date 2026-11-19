package mage.cards.e;

import java.util.UUID;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.hint.common.OpenSideboardHint;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

/**
 *
 * @author muz
 */
public final class ExtrapolateTheImpossible extends CardImpl {

    public ExtrapolateTheImpossible(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{B}");

        // You may reveal exactly two cards you own with different names from outside the game. An opponent chooses one of them. You put that card into your hand.
        this.getSpellAbility().addEffect(new ExtrapolateTheImpossibleEffect());
        this.getSpellAbility().addHint(OpenSideboardHint.instance);
    }

    private ExtrapolateTheImpossible(final ExtrapolateTheImpossible card) {
        super(card);
    }

    @Override
    public ExtrapolateTheImpossible copy() {
        return new ExtrapolateTheImpossible(this);
    }
}

class ExtrapolateTheImpossibleEffect extends OneShotEffect {

    ExtrapolateTheImpossibleEffect() {
        super(Outcome.DrawCard);
        staticText = "You may reveal exactly two cards you own with different names from outside the game. An opponent chooses one of them. You put that card into your hand";
    }

    private ExtrapolateTheImpossibleEffect(final ExtrapolateTheImpossibleEffect effect) {
        super(effect);
    }

    @Override
    public ExtrapolateTheImpossibleEffect copy() {
        return new ExtrapolateTheImpossibleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller == null || sourceObject == null) {
            return false;
        }
        long distinctNames = controller.getSideboard().getCards(game).stream()
                .map(MageObject::getName)
                .distinct()
                .count();
        if (distinctNames < 2) {
            game.informPlayer(controller, "You do not have two cards with different names outside the game.");
            return false;
        }
        if (!controller.chooseUse(Outcome.DrawCard, "Reveal two cards with different names from outside the game?", source, game)) {
            return false;
        }
        TargetCardWithDifferentNameInSideboard target = new TargetCardWithDifferentNameInSideboard();
        if (!controller.choose(Outcome.DrawCard, controller.getSideboard(), target, source, game)
                || target.getTargets().size() != 2) {
            return false;
        }
        Cards revealed = new CardsImpl(target.getTargets());
        controller.revealCards(sourceObject.getIdName(), revealed, game);

        TargetOpponent targetOpponent = new TargetOpponent(true);
        if (!controller.choose(Outcome.Detriment, targetOpponent, source, game)) {
            return false;
        }
        Player opponent = game.getPlayer(targetOpponent.getFirstTarget());
        if (opponent == null) {
            return false;
        }
        TargetCard oppTarget = new TargetCard(1, 1, Zone.OUTSIDE, new FilterCard("card to put into " + controller.getName() + "'s hand"));
        oppTarget.withNotTarget(true);
        if (!opponent.choose(Outcome.Detriment, revealed, oppTarget, source, game)) {
            return false;
        }
        Card chosen = revealed.get(oppTarget.getFirstTarget(), game);
        if (chosen == null) {
            return false;
        }
        return controller.moveCards(chosen, Zone.HAND, source, game);
    }
}

class TargetCardWithDifferentNameInSideboard extends TargetCard {

    TargetCardWithDifferentNameInSideboard() {
        super(2, 2, Zone.OUTSIDE, StaticFilters.FILTER_CARD);
        this.withNotTarget(true);
    }

    private TargetCardWithDifferentNameInSideboard(final TargetCardWithDifferentNameInSideboard target) {
        super(target);
    }

    @Override
    public TargetCardWithDifferentNameInSideboard copy() {
        return new TargetCardWithDifferentNameInSideboard(this);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Cards cards, Game game) {
        if (!super.canTarget(playerId, id, source, cards, game)) {
            return false;
        }
        Card card = cards.get(id, game);
        if (card == null) {
            return false;
        }
        for (UUID chosenId : this.getTargets()) {
            Card existing = cards.get(chosenId, game);
            if (existing != null && CardUtil.haveSameNames(card, existing)) {
                return false;
            }
        }
        return true;
    }
}
