package mage.cards.r;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.common.delayed.WhenYouAttackDelayedTriggeredAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInExile;
import mage.target.common.TargetCardInLibrary;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class RoadsGoEverEverOn extends CardImpl {

    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(
            new FilterControlledPermanent(SubType.PLAINS, "Plains you control")
    );

    public RoadsGoEverEverOn(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{W}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after IV.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_IV);

        // I — Search your library for up to two basic Plains cards, exile them, then shuffle. You gain 2 life.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I,
                new RoadsGoEverEverOnSearchEffect()
        );

        // II, III — Put a card exiled with this Saga into its owner's hand.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II, SagaChapter.CHAPTER_III,
                new RoadsGoEverEverOnReturnEffect()
        );

        // IV — Whenever you attack this turn, target creature you control gets +1/+1 until end of turn for each Plains you control.
        WhenYouAttackDelayedTriggeredAbility delayed = new WhenYouAttackDelayedTriggeredAbility(
                new BoostTargetEffect(xValue, xValue, Duration.EndOfTurn)
                        .setText("target creature you control gets +1/+1 until end of turn for each Plains you control")
        );
        delayed.addTarget(new TargetControlledCreaturePermanent());
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_IV,
                new CreateDelayedTriggeredAbilityEffect(delayed)
        );

        this.addAbility(sagaAbility);
    }

    private RoadsGoEverEverOn(final RoadsGoEverEverOn card) {
        super(card);
    }

    @Override
    public RoadsGoEverEverOn copy() {
        return new RoadsGoEverEverOn(this);
    }
}

class RoadsGoEverEverOnSearchEffect extends OneShotEffect {

    RoadsGoEverEverOnSearchEffect() {
        super(Outcome.Benefit);
        this.staticText = "Search your library for up to two basic Plains cards, exile them, then shuffle. You gain 2 life";
    }

    private RoadsGoEverEverOnSearchEffect(final RoadsGoEverEverOnSearchEffect effect) {
        super(effect);
    }

    @Override
    public RoadsGoEverEverOnSearchEffect copy() {
        return new RoadsGoEverEverOnSearchEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInLibrary target = new TargetCardInLibrary(0, 2, StaticFilters.FILTER_CARD_BASIC_PLAINS);
        if (controller.searchLibrary(target, source, game)) {
            Cards cards = new CardsImpl();
            for (UUID cardId : target.getTargets()) {
                Card card = controller.getLibrary().getCard(cardId, game);
                if (card != null) {
                    cards.add(card);
                }
            }
            if (!cards.isEmpty()) {
                controller.moveCardsToExile(
                        cards.getCards(game), source, game, true,
                        CardUtil.getExileZoneId(game, source),
                        CardUtil.getSourceName(game, source)
                );
            }
        }
        controller.shuffleLibrary(source, game);
        controller.gainLife(2, game, source);
        return true;
    }
}

class RoadsGoEverEverOnReturnEffect extends OneShotEffect {

    RoadsGoEverEverOnReturnEffect() {
        super(Outcome.ReturnToHand);
        this.staticText = "Put a card exiled with {this} into its owner's hand";
    }

    private RoadsGoEverEverOnReturnEffect(final RoadsGoEverEverOnReturnEffect effect) {
        super(effect);
    }

    @Override
    public RoadsGoEverEverOnReturnEffect copy() {
        return new RoadsGoEverEverOnReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileId = CardUtil.getExileZoneId(game, source);
        ExileZone exile = game.getExile().getExileZone(exileId);
        if (exile == null || exile.isEmpty()) {
            return false;
        }
        TargetCard target = new TargetCardInExile(StaticFilters.FILTER_CARD, exileId);
        target.withNotTarget(true);
        if (controller.choose(outcome, target, source, game)) {
            Card card = game.getCard(target.getFirstTarget());
            if (card != null) {
                return controller.moveCards(card, Zone.HAND, source, game);
            }
        }
        return false;
    }
}
