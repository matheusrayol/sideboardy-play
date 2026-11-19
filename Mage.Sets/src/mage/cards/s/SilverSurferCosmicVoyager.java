package mage.cards.s;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class SilverSurferCosmicVoyager extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("other target permanents you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public SilverSurferCosmicVoyager(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Silver Surfer enters, exile any number of other target permanents you control. Return those cards to the battlefield under their owner's control at the beginning of the next end step. If a land enters this way, it enters tapped.
        Ability ability = new EntersBattlefieldTriggeredAbility(new SilverSurferCosmicVoyagerEffect());
        ability.addTarget(new TargetControlledPermanent(0, Integer.MAX_VALUE, filter, false));
        this.addAbility(ability);
    }

    private SilverSurferCosmicVoyager(final SilverSurferCosmicVoyager card) {
        super(card);
    }

    @Override
    public SilverSurferCosmicVoyager copy() {
        return new SilverSurferCosmicVoyager(this);
    }
}

class SilverSurferCosmicVoyagerEffect extends OneShotEffect {

    SilverSurferCosmicVoyagerEffect() {
        super(Outcome.Neutral);
        this.staticText = "exile any number of other target permanents you control. "
                + "Return those cards to the battlefield under their owner's control at the beginning of the next end step. "
                + "If a land enters this way, it enters tapped";
    }

    private SilverSurferCosmicVoyagerEffect(final SilverSurferCosmicVoyagerEffect effect) {
        super(effect);
    }

    @Override
    public SilverSurferCosmicVoyagerEffect copy() {
        return new SilverSurferCosmicVoyagerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Set<Card> toExile = new LinkedHashSet<>();
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent permanent = game.getPermanent(targetId);
            if (permanent != null) {
                toExile.add(permanent);
            }
        }
        if (toExile.isEmpty()) {
            return true;
        }
        UUID exileId = CardUtil.getExileZoneId(game, source);
        controller.moveCardsToExile(toExile, source, game, true, exileId, CardUtil.getSourceName(game, source));
        Cards exiledCards = new CardsImpl();
        ExileZone exileZone = game.getExile().getExileZone(exileId);
        if (exileZone != null) {
            exiledCards.addAll(exileZone);
        }
        game.addDelayedTriggeredAbility(
                new AtTheBeginOfNextEndStepDelayedTriggeredAbility(new SilverSurferReturnEffect(exiledCards, game)),
                source
        );
        return true;
    }
}

class SilverSurferReturnEffect extends OneShotEffect {

    private final Set<MageObjectReference> morSet = new LinkedHashSet<>();

    SilverSurferReturnEffect(Cards exiledCards, Game game) {
        super(Outcome.PutCardInPlay);
        for (Card card : exiledCards.getCards(game)) {
            this.morSet.add(new MageObjectReference(card, game));
        }
        this.staticText = "return those cards to the battlefield under their owner's control. If a land enters this way, it enters tapped";
    }

    private SilverSurferReturnEffect(final SilverSurferReturnEffect effect) {
        super(effect);
        this.morSet.addAll(effect.morSet);
    }

    @Override
    public SilverSurferReturnEffect copy() {
        return new SilverSurferReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Set<Card> lands = new LinkedHashSet<>();
        Set<Card> nonLands = new LinkedHashSet<>();
        for (MageObjectReference mor : morSet) {
            Card card = mor.getCard(game);
            if (card != null) {
                if (card.isLand(game)) {
                    lands.add(card);
                } else {
                    nonLands.add(card);
                }
            }
        }
        if (!nonLands.isEmpty()) {
            controller.moveCards(nonLands, Zone.BATTLEFIELD, source, game, false, false, true, null);
        }
        if (!lands.isEmpty()) {
            controller.moveCards(lands, Zone.BATTLEFIELD, source, game, true, false, true, null);
        }
        return true;
    }
}
