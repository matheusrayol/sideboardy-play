package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.predicate.mageobject.CommanderPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class JaniceRandEssentialAide extends CardImpl {

    public JaniceRandEssentialAide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever a commander you control enters or attacks, put a +1/+1 counter on it,
        // then reveal cards from the top of your library until you reveal a land card.
        // Put that card onto the battlefield tapped and the rest on the bottom of your library in a random order.
        this.addAbility(new JaniceRandTriggeredAbility());
    }

    private JaniceRandEssentialAide(final JaniceRandEssentialAide card) {
        super(card);
    }

    @Override
    public JaniceRandEssentialAide copy() {
        return new JaniceRandEssentialAide(this);
    }
}

class JaniceRandTriggeredAbility extends TriggeredAbilityImpl {

    JaniceRandTriggeredAbility() {
        super(Zone.BATTLEFIELD, new JaniceRandEffect(), false);
        setTriggerPhrase("Whenever a commander you control enters or attacks, ");
    }

    private JaniceRandTriggeredAbility(final JaniceRandTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public JaniceRandTriggeredAbility copy() {
        return new JaniceRandTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
                || event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        UUID permanentId = event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD
                ? event.getTargetId()
                : event.getSourceId();
        Permanent permanent = game.getPermanent(permanentId);
        if (permanent != null
                && permanent.isControlledBy(this.getControllerId())
                && CommanderPredicate.instance.apply(permanent, game)) {
            this.getEffects().setTargetPointer(new FixedTarget(permanent, game));
            return true;
        }
        return false;
    }
}

class JaniceRandEffect extends OneShotEffect {

    JaniceRandEffect() {
        super(Outcome.Benefit);
        this.staticText = "put a +1/+1 counter on it, then reveal cards from the top of your library until you reveal a land card. "
                + "Put that card onto the battlefield tapped and the rest on the bottom of your library in a random order";
    }

    private JaniceRandEffect(final JaniceRandEffect effect) {
        super(effect);
    }

    @Override
    public JaniceRandEffect copy() {
        return new JaniceRandEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent commander = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (commander != null) {
            commander.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
        }
        Cards revealed = new CardsImpl();
        Card landCard = null;
        for (Card card : controller.getLibrary().getCards(game)) {
            revealed.add(card);
            if (card.isLand(game)) {
                landCard = card;
                break;
            }
        }
        if (!revealed.isEmpty()) {
            controller.revealCards(source, revealed, game);
        }
        if (landCard != null) {
            controller.moveCards(landCard, Zone.BATTLEFIELD, source, game, true, false, false, null);
        }
        revealed.retainZone(Zone.LIBRARY, game);
        if (!revealed.isEmpty()) {
            controller.putCardsOnBottomOfLibrary(revealed, game, source, false);
        }
        return true;
    }
}
