package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.NamePredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetCardInLibrary;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class AetherSearcher extends CardImpl {

    public AetherSearcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{7}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(6);
        this.toughness = new MageInt(4);

        // Reveal Aether Searcher as you draft it. Reveal the next card you draft and note its name.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it. Reveal the next card you draft and note its name - not implemented.")
        ));

        // When Aether Searcher enters, you may search your hand and/or library for a card with a name noted as you drafted cards named Aether Searcher. You may cast it without paying its mana cost. If you search your library this way, shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new AetherSearcherEffect(), true));
    }

    private AetherSearcher(final AetherSearcher card) {
        super(card);
    }

    @Override
    public AetherSearcher copy() {
        return new AetherSearcher(this);
    }
}

class AetherSearcherEffect extends OneShotEffect {

    AetherSearcherEffect() {
        super(Outcome.PlayForFree);
        staticText = "search your hand and/or library for a card with a name noted as you drafted cards named {this}. "
                + "You may cast it without paying its mana cost. If you search your library this way, shuffle";
    }

    private AetherSearcherEffect(final AetherSearcherEffect effect) {
        super(effect);
    }

    @Override
    public AetherSearcherEffect copy() {
        return new AetherSearcherEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Object raw = game.getState().getValue(controller.getId() + "_aether_searcher_names");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return true;
        }
        List<NamePredicate> namePredicates = new ArrayList<>();
        for (Object item : (Collection<?>) raw) {
            if (item instanceof String && !((String) item).isEmpty()) {
                namePredicates.add(new NamePredicate((String) item));
            }
        }
        if (namePredicates.isEmpty()) {
            return true;
        }
        FilterCard filter = new FilterCard("card with a noted name");
        filter.add(Predicates.or(namePredicates));

        Card found = null;
        if (controller.chooseUse(Outcome.PlayForFree, "Search your hand instead of your library?", source, game)) {
            TargetCardInHand handTarget = new TargetCardInHand(0, 1, filter);
            if (controller.choose(Outcome.PlayForFree, handTarget, source, game)) {
                found = controller.getHand().get(handTarget.getFirstTarget(), game);
            }
        } else {
            TargetCardInLibrary libTarget = new TargetCardInLibrary(filter);
            if (controller.searchLibrary(libTarget, source, game)) {
                found = controller.getLibrary().getCard(libTarget.getFirstTarget(), game);
            }
            controller.shuffleLibrary(source, game);
        }
        if (found != null) {
            CardUtil.castSpellWithAttributesForFree(controller, source, game, found);
        }
        return true;
    }
}
