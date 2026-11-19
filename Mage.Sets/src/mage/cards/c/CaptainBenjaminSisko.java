package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterNonlandCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.token.OfficerToken;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class CaptainBenjaminSisko extends CardImpl {

    public CaptainBenjaminSisko(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // At the beginning of your end step, create a 1/1 red Officer creature token for each dilemma you've faced this turn.
        this.addAbility(
                new BeginningOfEndStepTriggeredAbility(
                        new CreateTokenEffect(new OfficerToken(), CaptainBenjaminSiskoValue.instance)
                                .setText("create a 1/1 red Officer creature token for each dilemma you've faced this turn")
                ),
                new CaptainBenjaminSiskoWatcher()
        );

        // {2}{W}{U}{B}{R}{G}, {T}: Exile the top five cards of your library. You may cast any number of spells with mana value less than or equal to the number of Officers you control from among those cards without paying their mana costs.
        Ability ability = new SimpleActivatedAbility(
                new CaptainBenjaminSiskoEffect(),
                new ManaCostsImpl<>("{2}{W}{U}{B}{R}{G}")
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private CaptainBenjaminSisko(final CaptainBenjaminSisko card) {
        super(card);
    }

    @Override
    public CaptainBenjaminSisko copy() {
        return new CaptainBenjaminSisko(this);
    }
}

enum CaptainBenjaminSiskoValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        CaptainBenjaminSiskoWatcher watcher = game.getState().getWatcher(CaptainBenjaminSiskoWatcher.class);
        return watcher != null ? watcher.getDilemmaCount(sourceAbility.getControllerId()) : 0;
    }

    @Override
    public CaptainBenjaminSiskoValue copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "dilemma you've faced this turn";
    }

    @Override
    public String toString() {
        return "1";
    }
}

class CaptainBenjaminSiskoWatcher extends Watcher {

    private final Map<UUID, Integer> dilemmasThisTurn = new HashMap<>();

    CaptainBenjaminSiskoWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getPlayerId() != null && MelkotianAdjudicator.isDilemmaEvent(event, event.getPlayerId(), game)) {
            dilemmasThisTurn.merge(event.getPlayerId(), 1, Integer::sum);
        }
    }

    @Override
    public void reset() {
        super.reset();
        dilemmasThisTurn.clear();
    }

    int getDilemmaCount(UUID playerId) {
        return dilemmasThisTurn.getOrDefault(playerId, 0);
    }
}

class CaptainBenjaminSiskoEffect extends OneShotEffect {

    CaptainBenjaminSiskoEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "Exile the top five cards of your library. You may cast any number of spells with mana value "
                + "less than or equal to the number of Officers you control from among those cards without paying their mana costs";
    }

    private CaptainBenjaminSiskoEffect(final CaptainBenjaminSiskoEffect effect) {
        super(effect);
    }

    @Override
    public CaptainBenjaminSiskoEffect copy() {
        return new CaptainBenjaminSiskoEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards cards = new CardsImpl(controller.getLibrary().getTopCards(game, 5));
        if (cards.isEmpty()) {
            return true;
        }
        controller.moveCards(cards, Zone.EXILED, source, game);
        cards.retainZone(Zone.EXILED, game);
        int officers = game.getBattlefield().count(
                new FilterControlledPermanent(SubType.OFFICER), source.getControllerId(), source, game
        );
        FilterCard filter = new FilterNonlandCard("spell with mana value " + officers + " or less");
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, officers + 1));
        CardUtil.castMultipleWithAttributeForFree(controller, source, game, cards, filter);
        return true;
    }
}
