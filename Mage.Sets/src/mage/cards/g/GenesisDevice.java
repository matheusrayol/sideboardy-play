package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInPlayEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class GenesisDevice extends CardImpl {

    private static final FilterCard filter = new FilterCard("a basic land card or Planet card");

    static {
        filter.add(Predicates.or(
                Predicates.and(CardType.LAND.getPredicate(), SuperType.BASIC.getPredicate()),
                SubType.PLANET.getPredicate()
        ));
    }

    public GenesisDevice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{G}");

        this.supertype.add(SuperType.LEGENDARY);

        // When Genesis Device enters, search your library for a basic land card or Planet card, put it onto the battlefield tapped, then shuffle.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new SearchLibraryPutInPlayEffect(new TargetCardInLibrary(filter), true)
        ));

        // {3}{G}{G}, {T}, Sacrifice Genesis Device: Sacrifice any number of lands you control, then search your library for up to that many land cards, put them onto the battlefield tapped, then shuffle. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new GenesisDeviceEffect(), new ManaCostsImpl<>("{3}{G}{G}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private GenesisDevice(final GenesisDevice card) {
        super(card);
    }

    @Override
    public GenesisDevice copy() {
        return new GenesisDevice(this);
    }
}

class GenesisDeviceEffect extends OneShotEffect {

    GenesisDeviceEffect() {
        super(Outcome.PutLandInPlay);
        this.staticText = "Sacrifice any number of lands you control, then search your library for up to that many land cards, put them onto the battlefield tapped, then shuffle";
    }

    private GenesisDeviceEffect(final GenesisDeviceEffect effect) {
        super(effect);
    }

    @Override
    public GenesisDeviceEffect copy() {
        return new GenesisDeviceEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice targetSac = new TargetSacrifice(0, Integer.MAX_VALUE, StaticFilters.FILTER_LAND);
        controller.choose(Outcome.Sacrifice, targetSac, source, game);
        int sacrificedCount = 0;
        for (UUID landId : targetSac.getTargets()) {
            Permanent land = game.getPermanent(landId);
            if (land != null && land.sacrifice(source, game)) {
                sacrificedCount++;
            }
        }
        TargetCardInLibrary targetSearch = new TargetCardInLibrary(0, sacrificedCount, StaticFilters.FILTER_CARD_LANDS);
        if (controller.searchLibrary(targetSearch, source, game)) {
            Cards found = new CardsImpl();
            for (UUID cardId : targetSearch.getTargets()) {
                Card card = controller.getLibrary().getCard(cardId, game);
                if (card != null) {
                    found.add(card);
                }
            }
            controller.moveCards(found.getCards(game), Zone.BATTLEFIELD, source, game, true, false, false, null);
        }
        controller.shuffleLibrary(source, game);
        return true;
    }
}
