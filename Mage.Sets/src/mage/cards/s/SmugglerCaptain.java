package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.NamePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SmugglerCaptain extends CardImpl {

    public SmugglerCaptain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Draft Smuggler Captain face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Draft {this} face up - not implemented.")
        ));

        // As you draft a card, you may reveal it, note its name, then turn this card face down.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("As you draft a card, you may reveal it, note its name, then turn {this} face down - not implemented.")
        ));

        // When Smuggler Captain enters, you may search your library for a card with a name you noted for cards named Smuggler Captain, reveal it, put it into your hand, then shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SmugglerCaptainEffect(), true));
    }

    private SmugglerCaptain(final SmugglerCaptain card) {
        super(card);
    }

    @Override
    public SmugglerCaptain copy() {
        return new SmugglerCaptain(this);
    }
}

class SmugglerCaptainEffect extends OneShotEffect {

    SmugglerCaptainEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "search your library for a card with a name you noted for cards named {this}, reveal it, put it into your hand, then shuffle";
    }

    private SmugglerCaptainEffect(final SmugglerCaptainEffect effect) {
        super(effect);
    }

    @Override
    public SmugglerCaptainEffect copy() {
        return new SmugglerCaptainEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Object raw = game.getState().getValue(controller.getId() + "_smuggler_captain_names");
        List<NamePredicate> predicates = new ArrayList<>();
        if (raw instanceof Collection<?>) {
            for (Object item : (Collection<?>) raw) {
                if (item instanceof String && !((String) item).isEmpty()) {
                    predicates.add(new NamePredicate((String) item));
                }
            }
        }
        if (predicates.isEmpty()) {
            controller.shuffleLibrary(source, game);
            return true;
        }
        FilterCard filter = new FilterCard("card with a noted name");
        filter.add(Predicates.or(predicates));
        TargetCardInLibrary target = new TargetCardInLibrary(filter);
        if (controller.searchLibrary(target, source, game)) {
            Card card = controller.getLibrary().getCard(target.getFirstTarget(), game);
            if (card != null) {
                controller.revealCards(source, new CardsImpl(card), game);
                controller.moveCards(card, Zone.HAND, source, game);
            }
        }
        controller.shuffleLibrary(source, game);
        return true;
    }
}
