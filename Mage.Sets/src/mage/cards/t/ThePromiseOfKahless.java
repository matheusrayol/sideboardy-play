package mage.cards.t;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetSacrifice;

/**
 *
 * @author muz
 */
public final class ThePromiseOfKahless extends CardImpl {

    public ThePromiseOfKahless(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{B}");

        // You may sacrifice a Klingon or Warrior. If you do, return all other Klingon and Warrior creature cards from your graveyard to the battlefield.
        this.getSpellAbility().addEffect(new ThePromiseOfKahlessEffect());
    }

    private ThePromiseOfKahless(final ThePromiseOfKahless card) {
        super(card);
    }

    @Override
    public ThePromiseOfKahless copy() {
        return new ThePromiseOfKahless(this);
    }
}

class ThePromiseOfKahlessEffect extends OneShotEffect {

    private static final FilterControlledPermanent sacFilter = new FilterControlledPermanent("a Klingon or Warrior");

    static {
        sacFilter.add(Predicates.or(
                SubType.KLINGON.getPredicate(),
                SubType.WARRIOR.getPredicate()
        ));
    }

    ThePromiseOfKahlessEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "You may sacrifice a Klingon or Warrior. If you do, return all other Klingon and Warrior creature cards from your graveyard to the battlefield";
    }

    private ThePromiseOfKahlessEffect(final ThePromiseOfKahlessEffect effect) {
        super(effect);
    }

    @Override
    public ThePromiseOfKahlessEffect copy() {
        return new ThePromiseOfKahlessEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice target = new TargetSacrifice(0, 1, sacFilter);
        if (!target.canChoose(controller.getId(), source, game)) {
            return false;
        }
        if (!target.choose(Outcome.Sacrifice, controller.getId(), source.getSourceId(), source, game)
                || target.getTargets().isEmpty()) {
            return false;
        }
        Permanent sacrificed = game.getPermanent(target.getFirstTarget());
        if (sacrificed == null || !sacrificed.sacrifice(source, game)) {
            return false;
        }
        UUID sacrificedId = sacrificed.getId();
        UUID sacrificedCardId = sacrificed.getMainCard().getId();
        Cards toReturn = new CardsImpl();
        for (Card card : controller.getGraveyard().getCards(game)) {
            if (card.isCreature(game)
                    && (card.hasSubtype(SubType.KLINGON, game) || card.hasSubtype(SubType.WARRIOR, game))
                    && !card.getId().equals(sacrificedId)
                    && !card.getId().equals(sacrificedCardId)) {
                toReturn.add(card);
            }
        }
        if (!toReturn.isEmpty()) {
            controller.moveCards(toReturn, Zone.BATTLEFIELD, source, game);
        }
        return true;
    }
}
