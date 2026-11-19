package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.*;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeBatchEvent;
import mage.game.events.ZoneChangeEvent;
import mage.players.Player;
import mage.target.targetpointer.FixedTargets;
import mage.util.CardUtil;

import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheMadcapJester extends CardImpl {

    public TheMadcapJester(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CLOWN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Whenever The Madcap Jester attacks, each opponent exiles the top card of their library.
        this.addAbility(new AttacksTriggeredAbility(new TheMadcapJesterAttackEffect()));

        // Whenever a spell or ability you control causes one or more cards to be exiled from an opponent's library, you may play those cards until the end of your next turn. Mana of any type can be spent to cast spells this way.
        this.addAbility(new TheMadcapJesterExileTriggeredAbility());
    }

    private TheMadcapJester(final TheMadcapJester card) {
        super(card);
    }

    @Override
    public TheMadcapJester copy() {
        return new TheMadcapJester(this);
    }
}

class TheMadcapJesterAttackEffect extends OneShotEffect {

    TheMadcapJesterAttackEffect() {
        super(Outcome.Detriment);
        this.staticText = "each opponent exiles the top card of their library";
    }

    private TheMadcapJesterAttackEffect(final TheMadcapJesterAttackEffect effect) {
        super(effect);
    }

    @Override
    public TheMadcapJesterAttackEffect copy() {
        return new TheMadcapJesterAttackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID opponentId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent == null) {
                continue;
            }
            Card topCard = opponent.getLibrary().getFromTop(game);
            if (topCard != null) {
                opponent.moveCardsToExile(topCard, source, game, true, null, "");
            }
        }
        return true;
    }
}

class TheMadcapJesterExileTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<ZoneChangeEvent> {

    TheMadcapJesterExileTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TheMadcapJesterPlayEffect(), false);
        setTriggerPhrase("Whenever a spell or ability you control causes one or more cards to be exiled from an opponent's library, ");
    }

    private TheMadcapJesterExileTriggeredAbility(final TheMadcapJesterExileTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TheMadcapJesterExileTriggeredAbility copy() {
        return new TheMadcapJesterExileTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE_BATCH;
    }

    @Override
    public boolean checkEvent(ZoneChangeEvent event, Game game) {
        if (!Zone.LIBRARY.match(event.getFromZone()) || !Zone.EXILED.match(event.getToZone())) {
            return false;
        }
        if (event.getSourceId() == null || !isControlledBy(game.getControllerId(event.getSourceId()))) {
            return false;
        }
        Card card = game.getCard(event.getTargetId());
        return card != null && game.getOpponents(getControllerId()).contains(card.getOwnerId());
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        List<ZoneChangeEvent> events = getFilteredEvents((ZoneChangeBatchEvent) event, game);
        if (events.isEmpty()) {
            return false;
        }
        Cards cards = new CardsImpl();
        for (ZoneChangeEvent zce : events) {
            Card card = game.getCard(zce.getTargetId());
            if (card != null && game.getState().getZone(card.getId()) == Zone.EXILED) {
                cards.add(card);
            }
        }
        if (cards.isEmpty()) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTargets(cards, game));
        return true;
    }
}

class TheMadcapJesterPlayEffect extends OneShotEffect {

    TheMadcapJesterPlayEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "you may play those cards until the end of your next turn. Mana of any type can be spent to cast spells this way";
    }

    private TheMadcapJesterPlayEffect(final TheMadcapJesterPlayEffect effect) {
        super(effect);
    }

    @Override
    public TheMadcapJesterPlayEffect copy() {
        return new TheMadcapJesterPlayEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID cardId : getTargetPointer().getTargets(game, source)) {
            Card card = game.getCard(cardId);
            if (card != null && game.getState().getZone(card.getId()) == Zone.EXILED) {
                CardUtil.makeCardPlayable(
                        game, source, card, false, Duration.UntilEndOfYourNextTurn, true,
                        source.getControllerId(), null
                );
            }
        }
        return true;
    }
}
