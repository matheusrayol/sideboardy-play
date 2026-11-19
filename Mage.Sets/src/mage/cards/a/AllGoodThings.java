package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;
import mage.util.CardUtil;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class AllGoodThings extends CardImpl {

    public AllGoodThings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after IV.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_IV);

        // I, II, III — Draw two cards, then exile a card from your hand.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_III,
                new AllGoodThingsExileEffect()
        );

        // IV — If all cards exiled with this Saga share a card type, you gain 10 life.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_IV,
                new AllGoodThingsGainLifeEffect()
        );

        this.addAbility(sagaAbility);
    }

    private AllGoodThings(final AllGoodThings card) {
        super(card);
    }

    @Override
    public AllGoodThings copy() {
        return new AllGoodThings(this);
    }
}

class AllGoodThingsExileEffect extends OneShotEffect {

    AllGoodThingsExileEffect() {
        super(Outcome.DrawCard);
        this.staticText = "Draw two cards, then exile a card from your hand";
    }

    private AllGoodThingsExileEffect(final AllGoodThingsExileEffect effect) {
        super(effect);
    }

    @Override
    public AllGoodThingsExileEffect copy() {
        return new AllGoodThingsExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(2, source, game);
        if (!controller.getHand().isEmpty()) {
            TargetCardInHand target = new TargetCardInHand();
            controller.choose(Outcome.Exile, controller.getHand(), target, source, game);
            Card card = game.getCard(target.getFirstTarget());
            if (card != null) {
                controller.moveCardsToExile(
                        card, source, game, true,
                        CardUtil.getExileZoneId(game, source),
                        CardUtil.getSourceName(game, source)
                );
            }
        }
        return true;
    }
}

class AllGoodThingsGainLifeEffect extends OneShotEffect {

    AllGoodThingsGainLifeEffect() {
        super(Outcome.GainLife);
        this.staticText = "If all cards exiled with {this} share a card type, you gain 10 life";
    }

    private AllGoodThingsGainLifeEffect(final AllGoodThingsGainLifeEffect effect) {
        super(effect);
    }

    @Override
    public AllGoodThingsGainLifeEffect copy() {
        return new AllGoodThingsGainLifeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        ExileZone exile = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (exile != null && !exile.isEmpty()) {
            Set<Card> exiledCards = exile.getCards(game);
            boolean shareType = Arrays.stream(CardType.values())
                    .anyMatch(ct -> exiledCards.stream().allMatch(c -> c.getCardType(game).contains(ct)));
            if (shareType) {
                controller.gainLife(10, game, source);
                return true;
            }
        }
        return false;
    }
}
