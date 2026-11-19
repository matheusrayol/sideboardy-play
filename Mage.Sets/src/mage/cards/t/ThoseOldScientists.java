package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawDiscardControllerEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class ThoseOldScientists extends CardImpl {

    private static final FilterCard filter = new FilterCard("artifact, instant, or sorcery card from your graveyard");

    static {
        filter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.INSTANT.getPredicate(),
                CardType.SORCERY.getPredicate()
        ));
    }

    public ThoseOldScientists(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_III);

        // I — Draw two cards, then discard two cards.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I,
                new DrawDiscardControllerEffect(2, 2)
        );

        // II — Return up to one target artifact, instant, or sorcery card from your graveyard to your hand.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II, SagaChapter.CHAPTER_II,
                new ReturnFromGraveyardToHandTargetEffect(),
                new TargetCardInYourGraveyard(0, 1, filter)
        );

        // III — Choose up to two target cards in graveyards. The owners of those cards shuffle them into their libraries.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III, SagaChapter.CHAPTER_III,
                new ThoseOldScientistsEffect(),
                new TargetCardInGraveyard(0, 2)
        );

        this.addAbility(sagaAbility);
    }

    private ThoseOldScientists(final ThoseOldScientists card) {
        super(card);
    }

    @Override
    public ThoseOldScientists copy() {
        return new ThoseOldScientists(this);
    }
}

class ThoseOldScientistsEffect extends OneShotEffect {

    ThoseOldScientistsEffect() {
        super(Outcome.Neutral);
        this.staticText = "Choose up to two target cards in graveyards. The owners of those cards shuffle them into their libraries";
    }

    private ThoseOldScientistsEffect(final ThoseOldScientistsEffect effect) {
        super(effect);
    }

    @Override
    public ThoseOldScientistsEffect copy() {
        return new ThoseOldScientistsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<UUID> ownersToShuffle = new LinkedHashSet<>();
        for (UUID cardId : getTargetPointer().getTargets(game, source)) {
            Card card = game.getCard(cardId);
            if (card != null && game.getState().getZone(card.getId()) == Zone.GRAVEYARD) {
                Player owner = game.getPlayer(card.getOwnerId());
                if (owner != null) {
                    owner.moveCards(card, Zone.LIBRARY, source, game);
                    ownersToShuffle.add(owner.getId());
                }
            }
        }
        for (UUID ownerId : ownersToShuffle) {
            Player owner = game.getPlayer(ownerId);
            if (owner != null) {
                owner.shuffleLibrary(source, game);
            }
        }
        return true;
    }
}
