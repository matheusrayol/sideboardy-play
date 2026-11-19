package mage.cards.n;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInOpponentsGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class NegativeZonePortal extends CardImpl {

    public NegativeZonePortal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // {2}, {T}: Exile target card from an opponent's graveyard. If it's a creature card, draw a card.
        Ability ability = new SimpleActivatedAbility(new NegativeZonePortalExileEffect(), new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetCardInOpponentsGraveyard(StaticFilters.FILTER_CARD));
        this.addAbility(ability);

        // At the beginning of your upkeep, if there are four or more creature cards exiled with this artifact, flip a coin.
        // If you lose the flip, sacrifice this artifact and return a card exiled with it at random to its owner's hand.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new NegativeZonePortalUpkeepEffect())
                .withInterveningIf(NegativeZonePortalCondition.instance));
    }

    private NegativeZonePortal(final NegativeZonePortal card) {
        super(card);
    }

    @Override
    public NegativeZonePortal copy() {
        return new NegativeZonePortal(this);
    }
}

enum NegativeZonePortalCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        return exileZone != null && exileZone.count(StaticFilters.FILTER_CARD_CREATURE, game) >= 4;
    }

    @Override
    public String toString() {
        return "there are four or more creature cards exiled with {this}";
    }
}

class NegativeZonePortalExileEffect extends OneShotEffect {

    NegativeZonePortalExileEffect() {
        super(Outcome.Exile);
        this.staticText = "exile target card from an opponent's graveyard. If it's a creature card, draw a card";
    }

    private NegativeZonePortalExileEffect(final NegativeZonePortalExileEffect effect) {
        super(effect);
    }

    @Override
    public NegativeZonePortalExileEffect copy() {
        return new NegativeZonePortalExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        MageObject sourceObject = source.getSourceObject(game);
        String exileName = sourceObject == null ? "Negative Zone Portal" : sourceObject.getIdName();
        boolean isCreature = card.isCreature(game);
        if (controller.moveCardsToExile(card, source, game, true, CardUtil.getExileZoneId(game, source), exileName)
                && isCreature) {
            controller.drawCards(1, source, game);
        }
        return true;
    }
}

class NegativeZonePortalUpkeepEffect extends OneShotEffect {

    NegativeZonePortalUpkeepEffect() {
        super(Outcome.Sacrifice);
        this.staticText = "flip a coin. If you lose the flip, sacrifice {this} and return a card exiled with it at random to its owner's hand";
    }

    private NegativeZonePortalUpkeepEffect(final NegativeZonePortalUpkeepEffect effect) {
        super(effect);
    }

    @Override
    public NegativeZonePortalUpkeepEffect copy() {
        return new NegativeZonePortalUpkeepEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (controller.flipCoin(source, game, true)) {
            return true;
        }
        Permanent portal = source.getSourcePermanentIfItStillExists(game);
        if (portal != null) {
            portal.sacrifice(source, game);
        }
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (exileZone != null && !exileZone.isEmpty()) {
            Card randomCard = exileZone.getRandom(game);
            if (randomCard != null) {
                controller.moveCards(randomCard, Zone.HAND, source, game);
            }
        }
        return true;
    }
}
