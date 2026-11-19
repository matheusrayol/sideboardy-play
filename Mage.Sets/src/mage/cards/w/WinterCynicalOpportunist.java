package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.condition.common.DeliriumCondition;
import mage.abilities.dynamicvalue.common.CardTypesInGraveyardCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class WinterCynicalOpportunist extends CardImpl {

    public WinterCynicalOpportunist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARLOCK);
        this.power = new MageInt(2);
        this.toughness = new MageInt(5);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Whenever Winter attacks, mill three cards.
        this.addAbility(new AttacksTriggeredAbility(new MillCardsControllerEffect(3)));

        // Delirium — At the beginning of your end step, you may exile any number of cards from your graveyard with four or more card types among them. If you do, put a permanent card from among them onto the battlefield with a finality counter on it.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                new WinterCynicalOpportunistEffect(), true
        ).withInterveningIf(DeliriumCondition.instance)
                .setAbilityWord(AbilityWord.DELIRIUM)
                .addHint(CardTypesInGraveyardCount.YOU.getHint()));
    }

    private WinterCynicalOpportunist(final WinterCynicalOpportunist card) {
        super(card);
    }

    @Override
    public WinterCynicalOpportunist copy() {
        return new WinterCynicalOpportunist(this);
    }
}

class WinterCynicalOpportunistEffect extends OneShotEffect {

    WinterCynicalOpportunistEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "exile any number of cards from your graveyard with four or more card types among them. "
                + "If you do, put a permanent card from among them onto the battlefield with a finality counter on it";
    }

    private WinterCynicalOpportunistEffect(final WinterCynicalOpportunistEffect effect) {
        super(effect);
    }

    @Override
    public WinterCynicalOpportunistEffect copy() {
        return new WinterCynicalOpportunistEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInYourGraveyard target = new WinterGraveyardTarget();
        if (!controller.chooseTarget(Outcome.Exile, target, source, game)) {
            return false;
        }
        Cards chosenCards = new CardsImpl(target.getTargets());
        if (chosenCards.isEmpty() || countDistinctCardTypes(chosenCards, game) < 4) {
            return false;
        }
        controller.moveCards(chosenCards, Zone.EXILED, source, game);

        Cards exiledPermanentCards = new CardsImpl();
        for (Card card : chosenCards.getCards(game)) {
            if (game.getState().getZone(card.getId()) == Zone.EXILED && card.isPermanent(game)) {
                exiledPermanentCards.add(card);
            }
        }
        if (exiledPermanentCards.isEmpty()) {
            return true;
        }
        TargetCard choosePermanent = new TargetCard(1, Zone.EXILED, StaticFilters.FILTER_CARD_PERMANENT);
        choosePermanent.withChooseHint("to put onto the battlefield with a finality counter");
        if (controller.choose(Outcome.PutCardInPlay, exiledPermanentCards, choosePermanent, source, game)) {
            Card chosenPermCard = game.getCard(choosePermanent.getFirstTarget());
            if (chosenPermCard != null && controller.moveCards(chosenPermCard, Zone.BATTLEFIELD, source, game)) {
                Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(chosenPermCard, game);
                if (permanent != null) {
                    permanent.addCounters(CounterType.FINALITY.createInstance(), controller.getId(), source, game);
                }
            }
        }
        return true;
    }

    static int countDistinctCardTypes(Cards cards, Game game) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        for (Card card : cards.getCards(game)) {
            types.addAll(card.getCardType(game));
        }
        return types.size();
    }
}

class WinterGraveyardTarget extends TargetCardInYourGraveyard {

    WinterGraveyardTarget() {
        super(1, Integer.MAX_VALUE, StaticFilters.FILTER_CARD_CARDS, true);
        withChooseHint("cards with four or more card types among them");
    }

    private WinterGraveyardTarget(final WinterGraveyardTarget target) {
        super(target);
    }

    @Override
    public WinterGraveyardTarget copy() {
        return new WinterGraveyardTarget(this);
    }

    @Override
    public boolean isChosen(Game game) {
        if (!super.isChosen(game)) {
            return false;
        }
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        for (UUID cardId : this.targets.keySet()) {
            Card card = game.getCard(cardId);
            if (card != null) {
                types.addAll(card.getCardType(game));
            }
        }
        return types.size() >= 4;
    }
}
