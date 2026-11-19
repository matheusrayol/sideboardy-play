package mage.cards.s;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SSBotanyBay extends CardImpl {

    public SSBotanyBay(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // {T}: Each opponent exiles a card from their graveyard. You may exile a card from your graveyard.
        this.addAbility(new SimpleActivatedAbility(new SSBotanyBayExileEffect(), new TapSourceCost()));

        // Station
        this.addAbility(new StationAbility());

        // STATION 8+
        // Flying
        // Whenever S.S. Botany Bay deals combat damage to a player, return each creature card you own exiled with it to the battlefield.
        // 3/5
        this.addAbility(new StationLevelAbility(8)
                .withLevelAbility(FlyingAbility.getInstance())
                .withLevelAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
                        new SSBotanyBayReturnEffect(), false
                ))
                .withPT(3, 5));
    }

    private SSBotanyBay(final SSBotanyBay card) {
        super(card);
    }

    @Override
    public SSBotanyBay copy() {
        return new SSBotanyBay(this);
    }
}

class SSBotanyBayExileEffect extends OneShotEffect {

    SSBotanyBayExileEffect() {
        super(Outcome.Exile);
        this.staticText = "each opponent exiles a card from their graveyard. You may exile a card from your graveyard";
    }

    private SSBotanyBayExileEffect(final SSBotanyBayExileEffect effect) {
        super(effect);
    }

    @Override
    public SSBotanyBayExileEffect copy() {
        return new SSBotanyBayExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileZoneId = CardUtil.getExileZoneId(game, source);
        MageObject sourceObject = source.getSourceObject(game);
        String exileName = sourceObject == null ? "S.S. Botany Bay" : sourceObject.getIdName();
        for (UUID opponentId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null || opponent.getGraveyard().isEmpty()) {
                continue;
            }
            TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(
                    1, 1, StaticFilters.FILTER_CARD_FROM_YOUR_GRAVEYARD, true
            );
            if (opponent.choose(Outcome.Exile, target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    opponent.moveCardsToExile(card, source, game, true, exileZoneId, exileName);
                }
            }
        }
        if (!controller.getGraveyard().isEmpty()) {
            TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(
                    0, 1, StaticFilters.FILTER_CARD_FROM_YOUR_GRAVEYARD, true
            );
            if (controller.choose(Outcome.Exile, target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    controller.moveCardsToExile(card, source, game, true, exileZoneId, exileName);
                }
            }
        }
        return true;
    }
}

class SSBotanyBayReturnEffect extends OneShotEffect {

    SSBotanyBayReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "return each creature card you own exiled with it to the battlefield";
    }

    private SSBotanyBayReturnEffect(final SSBotanyBayReturnEffect effect) {
        super(effect);
    }

    @Override
    public SSBotanyBayReturnEffect copy() {
        return new SSBotanyBayReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (exileZone == null) {
            return true;
        }
        Cards toReturn = new CardsImpl();
        for (Card card : exileZone.getCards(StaticFilters.FILTER_CARD_CREATURE, game)) {
            if (card.isOwnedBy(source.getControllerId())) {
                toReturn.add(card);
            }
        }
        if (!toReturn.isEmpty()) {
            controller.moveCards(toReturn, Zone.BATTLEFIELD, source, game);
        }
        return true;
    }
}
