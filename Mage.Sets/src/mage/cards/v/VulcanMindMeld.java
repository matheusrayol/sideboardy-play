package mage.cards.v;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileFaceDownYouMayPlayAsLongAsExiledTargetEffect;
import mage.abilities.effects.common.asthought.MayLookAtTargetCardEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class VulcanMindMeld extends CardImpl {

    public VulcanMindMeld(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{U}");

        // Target opponent looks at the top card of your library, then exiles it face down.
        // You look at the top two cards of their library, then exile them face down.
        // For each card exiled this way, the player who exiled it may play it for as long as it remains exiled.
        // Mana of any type can be spent to cast spells this way.
        this.getSpellAbility().addEffect(new VulcanMindMeldEffect());
        this.getSpellAbility().addTarget(new TargetOpponent());
    }

    private VulcanMindMeld(final VulcanMindMeld card) {
        super(card);
    }

    @Override
    public VulcanMindMeld copy() {
        return new VulcanMindMeld(this);
    }
}

class VulcanMindMeldEffect extends OneShotEffect {

    VulcanMindMeldEffect() {
        super(Outcome.Benefit);
        this.staticText = "target opponent looks at the top card of your library, then exiles it face down. "
                + "You look at the top two cards of their library, then exile them face down. "
                + "For each card exiled this way, the player who exiled it may play it for as long as it remains exiled. "
                + "Mana of any type can be spent to cast spells this way";
    }

    private VulcanMindMeldEffect(final VulcanMindMeldEffect effect) {
        super(effect);
    }

    @Override
    public VulcanMindMeldEffect copy() {
        return new VulcanMindMeldEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        MageObject sourceObject = source.getSourceObject(game);
        String sourceName = sourceObject == null ? "Vulcan Mind-Meld" : sourceObject.getIdName();

        Card controllerTop = controller.getLibrary().getFromTop(game);
        if (controllerTop != null) {
            opponent.lookAtCards(sourceName, new CardsImpl(controllerTop), game);
            controllerTop.setFaceDown(true, game);
            UUID exileZoneId = CardUtil.getExileZoneId(game, source.getSourceId(), source.getStackMomentSourceZCC());
            if (opponent.moveCardsToExile(controllerTop, source, game, false, exileZoneId, sourceName)) {
                controllerTop.setFaceDown(true, game);
                CardUtil.makeCardPlayable(game, source, controllerTop, false, Duration.EndOfGame, true, opponent.getId(), null);
                ContinuousEffect lookEffect = new MayLookAtTargetCardEffect(opponent.getId());
                lookEffect.setTargetPointer(new FixedTarget(controllerTop.getId(), game));
                game.addEffect(lookEffect, source);
            }
        }

        Cards oppTopCards = new CardsImpl(opponent.getLibrary().getTopCards(game, 2));
        if (!oppTopCards.isEmpty()) {
            controller.lookAtCards(sourceName, oppTopCards, game);
            new ExileFaceDownYouMayPlayAsLongAsExiledTargetEffect(false, CastManaAdjustment.AS_THOUGH_ANY_MANA_TYPE)
                    .setTargetPointer(new FixedTargets(oppTopCards, game))
                    .apply(game, source);
        }
        return true;
    }
}
