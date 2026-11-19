package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.common.StrangeNewWorldsValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.hint.common.StrangeNewWorldsHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class BranchingTimeline extends CardImpl {

    public BranchingTimeline(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{6}{G}{G}");

        // Strange new worlds — Reveal the top X cards of your library, where X is the number of differently named lands you control. Put all permanent cards revealed this way onto the battlefield tapped and the rest on the bottom of your library in a random order.
        this.getSpellAbility().addEffect(new BranchingTimelineEffect());
        this.getSpellAbility().setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS);
        this.getSpellAbility().addHint(StrangeNewWorldsHint.instance);
    }

    private BranchingTimeline(final BranchingTimeline card) {
        super(card);
    }

    @Override
    public BranchingTimeline copy() {
        return new BranchingTimeline(this);
    }
}

class BranchingTimelineEffect extends OneShotEffect {

    BranchingTimelineEffect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "Reveal the top X cards of your library, where X is the number of differently named lands you control. "
                + "Put all permanent cards revealed this way onto the battlefield tapped and the rest on the bottom of your library in a random order";
    }

    private BranchingTimelineEffect(final BranchingTimelineEffect effect) {
        super(effect);
    }

    @Override
    public BranchingTimelineEffect copy() {
        return new BranchingTimelineEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int xValue = StrangeNewWorldsValue.instance.calculate(game, source, this);
        if (xValue <= 0) {
            return true;
        }
        Cards cards = new CardsImpl(controller.getLibrary().getTopCards(game, xValue));
        controller.revealCards(source, cards, game);
        Cards toBattlefield = new CardsImpl(cards.getCards(StaticFilters.FILTER_CARD_PERMANENT, game));
        cards.removeAll(toBattlefield);
        controller.moveCards(toBattlefield.getCards(game), Zone.BATTLEFIELD, source, game, true, false, false, null);
        controller.putCardsOnBottomOfLibrary(cards, game, source, false);
        return true;
    }
}
