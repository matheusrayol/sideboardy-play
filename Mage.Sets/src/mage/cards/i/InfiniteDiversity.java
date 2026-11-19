package mage.cards.i;

import mage.abilities.Ability;
import mage.abilities.dynamicvalue.common.FederationCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileSpellEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class InfiniteDiversity extends CardImpl {

    public InfiniteDiversity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{7}{R}{R}");

        // Federation — Exile target instant or sorcery card from your graveyard. Copy that card X times, where X is the number of creature types among non-Borg creatures you control. You may cast the copies without paying their mana costs. Exile Infinite Diversity.
        this.getSpellAbility().addEffect(new InfiniteDiversityEffect());
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY_FROM_YOUR_GRAVEYARD));
        this.getSpellAbility().addEffect(new ExileSpellEffect());
        this.getSpellAbility().setAbilityWord(AbilityWord.FEDERATION);
        this.getSpellAbility().addHint(FederationCount.instance.getHint());
    }

    private InfiniteDiversity(final InfiniteDiversity card) {
        super(card);
    }

    @Override
    public InfiniteDiversity copy() {
        return new InfiniteDiversity(this);
    }
}

class InfiniteDiversityEffect extends OneShotEffect {

    InfiniteDiversityEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "Exile target instant or sorcery card from your graveyard. "
                + "Copy that card X times, where X is the number of creature types among non-Borg creatures you control. "
                + "You may cast the copies without paying their mana costs";
    }

    private InfiniteDiversityEffect(final InfiniteDiversityEffect effect) {
        super(effect);
    }

    @Override
    public InfiniteDiversityEffect copy() {
        return new InfiniteDiversityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        controller.moveCards(card, Zone.EXILED, source, game);
        int xValue = FederationCount.instance.calculate(game, source, this);
        if (xValue <= 0) {
            return true;
        }
        Cards copies = new CardsImpl();
        for (int i = 0; i < xValue; i++) {
            Card copiedCard = game.copyCard(card, source, controller.getId());
            copies.add(copiedCard);
        }
        CardUtil.castMultipleWithAttributeForFree(controller, source, game, copies, StaticFilters.FILTER_CARD);
        return true;
    }
}
