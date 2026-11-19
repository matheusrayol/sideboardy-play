package mage.cards.n;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.CastFromEverywhereSourceCondition;
import mage.abilities.condition.common.ThresholdCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetOpponent;
import mage.util.CardUtil;

/**
 *
 * @author muz
 */
public final class NullSummoner extends CardImpl {

    public NullSummoner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARLOCK);
        this.power = new MageInt(4);
        this.toughness = new MageInt(2);

        // When this creature enters, if you cast it, target opponent reveals their hand. You choose a nonland card from it. Exile that card.
        Ability ability = new EntersBattlefieldTriggeredAbility(new NullSummonerExileEffect())
                .withInterveningIf(CastFromEverywhereSourceCondition.instance);
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);

        // Threshold — As long as there are seven or more cards in your graveyard, you may cast the exiled card, and mana of any type can be spent to cast that spell.
        this.addAbility(new SimpleStaticAbility(
                new InfoEffect("as long as there are seven or more cards in your graveyard, you may cast the exiled card, and mana of any type can be spent to cast that spell")
        ).setAbilityWord(AbilityWord.THRESHOLD));
    }

    private NullSummoner(final NullSummoner card) {
        super(card);
    }

    @Override
    public NullSummoner copy() {
        return new NullSummoner(this);
    }
}

class NullSummonerExileEffect extends OneShotEffect {

    NullSummonerExileEffect() {
        super(Outcome.Exile);
        staticText = "target opponent reveals their hand. You choose a nonland card from it. Exile that card";
    }

    private NullSummonerExileEffect(final NullSummonerExileEffect effect) {
        super(effect);
    }

    @Override
    public NullSummonerExileEffect copy() {
        return new NullSummonerExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        opponent.revealCards(source, opponent.getHand(), game);
        TargetCard target = new TargetCard(1, 1, Zone.HAND, StaticFilters.FILTER_CARD_A_NON_LAND);
        target.withNotTarget(true);
        if (!controller.choose(Outcome.Exile, opponent.getHand(), target, source, game)) {
            return false;
        }
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return false;
        }
        controller.moveCardsToExile(
                card, source, game, true,
                CardUtil.getExileZoneId(game, source),
                CardUtil.getSourceName(game, source)
        );
        CardUtil.makeCardPlayable(
                game, source, card, true,
                Duration.WhileOnBattlefield, true,
                source.getControllerId(), ThresholdCondition.instance
        );
        return true;
    }
}
