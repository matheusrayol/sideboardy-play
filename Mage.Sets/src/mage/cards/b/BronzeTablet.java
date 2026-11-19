package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BronzeTablet extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("nontoken permanent an opponent owns");

    static {
        filter.add(TokenPredicate.FALSE);
        filter.add(TargetController.OPPONENT.getOwnerPredicate());
    }

    public BronzeTablet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        // Remove Bronze Tablet from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // Bronze Tablet enters tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {4}, {T}: Exile Bronze Tablet and target nontoken permanent an opponent owns. That player may pay 10 life. If they do, put Bronze Tablet into its owner's graveyard. Otherwise, that player owns Bronze Tablet and you own the other exiled card.
        Ability ability = new SimpleActivatedAbility(new BronzeTabletEffect(), new ManaCostsImpl<>("{4}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private BronzeTablet(final BronzeTablet card) {
        super(card);
    }

    @Override
    public BronzeTablet copy() {
        return new BronzeTablet(this);
    }
}

class BronzeTabletEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    BronzeTabletEffect() {
        super(Outcome.Detriment);
        staticText = "Exile {this} and target nontoken permanent an opponent owns. "
                + "That player may pay 10 life. If they do, put {this} into its owner's graveyard. "
                + "Otherwise, that player owns {this} and you own the other exiled card";
    }

    private BronzeTabletEffect(final BronzeTabletEffect effect) {
        super(effect);
    }

    @Override
    public BronzeTabletEffect copy() {
        return new BronzeTabletEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent sourcePerm = source.getSourcePermanentIfItStillExists(game);
        Permanent targetPerm = game.getPermanent(getTargetPointer().getFirst(game, source));
        Cards toExile = new CardsImpl();
        if (sourcePerm != null) {
            toExile.add(sourcePerm);
        }
        if (targetPerm != null) {
            toExile.add(targetPerm);
        }
        if (!toExile.isEmpty()) {
            controller.moveCards(toExile, Zone.EXILED, source, game);
        }
        if (targetPerm == null) {
            return true;
        }
        Player opponent = game.getPlayer(targetPerm.getOwnerId());
        if (opponent != null
                && opponent.canPayLifeCost(source)
                && opponent.getLife() >= 10
                && opponent.chooseUse(Outcome.Detriment, "Pay 10 life to prevent Bronze Tablet exchange?", source, game)) {
            opponent.loseLife(10, game, source, false);
            Card sourceCard = game.getCard(source.getSourceId());
            if (sourceCard != null) {
                controller.moveCards(sourceCard, Zone.GRAVEYARD, source, game);
            }
        } else {
            Card sourceCard = game.getCard(source.getSourceId());
            if (sourceCard != null) {
                controller.moveCardsToExile(sourceCard, source, game, true, ANTE_EXILE_ID, "Ante");
            }
            Card targetCard = game.getCard(targetPerm.getId());
            if (targetCard != null) {
                controller.moveCardsToExile(targetCard, source, game, true, ANTE_EXILE_ID, "Ante");
            }
        }
        return true;
    }
}
