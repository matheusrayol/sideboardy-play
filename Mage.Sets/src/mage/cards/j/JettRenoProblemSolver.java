package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.BecomesTappedSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MelkotianAdjudicator;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class JettRenoProblemSolver extends CardImpl {

    public JettRenoProblemSolver(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Whenever Jett Reno becomes tapped, surveil 1. If her power is 4 or greater, instead draw a card.
        this.addAbility(new BecomesTappedSourceTriggeredAbility(new JettRenoTapEffect()));

        // Whenever you face a dilemma, put a +1/+1 counter on Jett Reno.
        this.addAbility(new JettRenoDilemmaTriggeredAbility());
    }

    private JettRenoProblemSolver(final JettRenoProblemSolver card) {
        super(card);
    }

    @Override
    public JettRenoProblemSolver copy() {
        return new JettRenoProblemSolver(this);
    }
}

class JettRenoTapEffect extends OneShotEffect {

    JettRenoTapEffect() {
        super(Outcome.DrawCard);
        this.staticText = "surveil 1. If her power is 4 or greater, instead draw a card";
    }

    private JettRenoTapEffect(final JettRenoTapEffect effect) {
        super(effect);
    }

    @Override
    public JettRenoTapEffect copy() {
        return new JettRenoTapEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent sourcePermanent = source.getSourcePermanentOrLKI(game);
        if (sourcePermanent != null && sourcePermanent.getPower().getValue() >= 4) {
            controller.drawCards(1, source, game);
        } else {
            controller.surveil(1, source, game);
        }
        return true;
    }
}

class JettRenoDilemmaTriggeredAbility extends TriggeredAbilityImpl {

    JettRenoDilemmaTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private JettRenoDilemmaTriggeredAbility(final JettRenoDilemmaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public JettRenoDilemmaTriggeredAbility copy() {
        return new JettRenoDilemmaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ACTIVATED_ABILITY
                || event.getType() == GameEvent.EventType.TRIGGERED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return MelkotianAdjudicator.isDilemmaEvent(event, getControllerId(), game);
    }
}
