package mage.cards.q;

import mage.ApprovingObject;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.common.MyTurnCondition;
import mage.abilities.costs.common.ExileFromHandCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class QStackingTheDeck extends CardImpl {

    public QStackingTheDeck(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.Q);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Q enters, shuffle your library, then exile cards from the top of your library until you exile a nonland card.
        // That nonland card gains "Exile a card from your hand: Create a copy of this card. You may cast the copy. Activate only during your turn and only if this card is exiled."
        this.addAbility(new EntersBattlefieldTriggeredAbility(new QStackingTheDeckEffect()));
    }

    private QStackingTheDeck(final QStackingTheDeck card) {
        super(card);
    }

    @Override
    public QStackingTheDeck copy() {
        return new QStackingTheDeck(this);
    }
}

class QStackingTheDeckEffect extends OneShotEffect {

    QStackingTheDeckEffect() {
        super(Outcome.Benefit);
        this.staticText = "shuffle your library, then exile cards from the top of your library until you exile a nonland card. "
                + "That nonland card gains \"Exile a card from your hand: Create a copy of this card. "
                + "You may cast the copy. Activate only during your turn and only if this card is exiled.\"";
    }

    private QStackingTheDeckEffect(final QStackingTheDeckEffect effect) {
        super(effect);
    }

    @Override
    public QStackingTheDeckEffect copy() {
        return new QStackingTheDeckEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.shuffleLibrary(source, game);
        while (controller.getLibrary().hasCards()) {
            Card card = controller.getLibrary().getFromTop(game);
            if (card == null) {
                break;
            }
            controller.moveCards(card, Zone.EXILED, source, game);
            if (!card.isLand(game)) {
                game.addEffect(new QStackingTheDeckGrantEffect(new MageObjectReference(card, game)), source);
                break;
            }
        }
        return true;
    }
}

class QStackingTheDeckGrantEffect extends ContinuousEffectImpl {

    private final MageObjectReference cardMor;

    QStackingTheDeckGrantEffect(MageObjectReference cardMor) {
        super(Duration.EndOfGame, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cardMor = cardMor;
    }

    private QStackingTheDeckGrantEffect(final QStackingTheDeckGrantEffect effect) {
        super(effect);
        this.cardMor = effect.cardMor;
    }

    @Override
    public QStackingTheDeckGrantEffect copy() {
        return new QStackingTheDeckGrantEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = cardMor.getCard(game);
        if (card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            discard();
            return false;
        }
        Ability ability = new QExiledCardAbility();
        ability.setSourceId(card.getId());
        ability.setControllerId(card.getOwnerId());
        game.getState().addOtherAbility(card, ability);
        return true;
    }
}

class QExiledCardAbility extends ActivateIfConditionActivatedAbility {

    QExiledCardAbility() {
        super(
                Zone.EXILED,
                new QCopyAndCastSelfEffect(),
                new ExileFromHandCost(new TargetCardInHand()),
                MyTurnCondition.instance
        );
    }

    private QExiledCardAbility(final QExiledCardAbility ability) {
        super(ability);
    }

    @Override
    public QExiledCardAbility copy() {
        return new QExiledCardAbility(this);
    }

    @Override
    public String getRule() {
        return "Exile a card from your hand: Create a copy of this card. You may cast the copy. Activate only during your turn and only if this card is exiled.";
    }
}

class QCopyAndCastSelfEffect extends OneShotEffect {

    QCopyAndCastSelfEffect() {
        super(Outcome.Benefit);
        this.staticText = "create a copy of this card. You may cast the copy";
    }

    private QCopyAndCastSelfEffect(final QCopyAndCastSelfEffect effect) {
        super(effect);
    }

    @Override
    public QCopyAndCastSelfEffect copy() {
        return new QCopyAndCastSelfEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(source.getSourceId());
        if (controller == null || card == null) {
            return false;
        }
        if (!controller.chooseUse(Outcome.Benefit, "Cast a copy of " + card.getName() + '?', source, game)) {
            return true;
        }
        Card copiedCard = game.copyCard(card, source, source.getControllerId());
        copiedCard.setZone(Zone.OUTSIDE, game);
        game.getState().setValue("PlayFromNotOwnHandZone" + copiedCard.getId(), Boolean.TRUE);
        controller.cast(
                controller.chooseAbilityForCast(copiedCard, game, false),
                game, false, new ApprovingObject(source, game)
        );
        game.getState().setValue("PlayFromNotOwnHandZone" + copiedCard.getId(), null);
        return true;
    }
}
