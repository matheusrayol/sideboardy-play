package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.dynamicvalue.common.OpponentsCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.effects.common.SacrificeOpponentsEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.token.KlingonWarriorToken;
import mage.players.Player;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class SinsOfTheFather extends CardImpl {

    public SinsOfTheFather(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}{R}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_III);

        // I — Each opponent sacrifices a nontoken creature of their choice.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I,
                new SacrificeOpponentsEffect(StaticFilters.FILTER_CREATURE_NON_TOKEN)
        );

        // II — For each opponent, you create a 2/1 red Klingon Warrior creature token with haste.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II,
                new CreateTokenEffect(new KlingonWarriorToken(), OpponentsCount.instance)
                        .setText("for each opponent, you create a 2/1 red Klingon Warrior creature token with haste")
        );

        // III — Exile target player's graveyard. Each opponent loses life equal to the number of creature cards exiled this way.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III, SagaChapter.CHAPTER_III,
                new SinsOfTheFatherEffect(),
                new TargetPlayer()
        );

        this.addAbility(sagaAbility);
    }

    private SinsOfTheFather(final SinsOfTheFather card) {
        super(card);
    }

    @Override
    public SinsOfTheFather copy() {
        return new SinsOfTheFather(this);
    }
}

class SinsOfTheFatherEffect extends OneShotEffect {

    SinsOfTheFatherEffect() {
        super(Outcome.Exile);
        this.staticText = "exile target player's graveyard. Each opponent loses life equal to the number of creature cards exiled this way";
    }

    private SinsOfTheFatherEffect(final SinsOfTheFatherEffect effect) {
        super(effect);
    }

    @Override
    public SinsOfTheFatherEffect copy() {
        return new SinsOfTheFatherEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player targetPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || targetPlayer == null) {
            return false;
        }
        Cards cardsToExile = new CardsImpl(targetPlayer.getGraveyard());
        int creatureCount = cardsToExile.count(StaticFilters.FILTER_CARD_CREATURE, game);
        controller.moveCards(cardsToExile, Zone.EXILED, source, game);
        if (creatureCount > 0) {
            new LoseLifeOpponentsEffect(creatureCount).apply(game, source);
        }
        return true;
    }
}
