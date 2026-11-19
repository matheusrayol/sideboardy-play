package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.game.permanent.token.TreasureToken;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PickABeeble extends CardImpl {

    public PickABeeble(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Roll a six-sided die. Put a number of luck counters on Pick-a-Beeble equal to the result and create a Treasure token. Then if there are six or more luck counters on Pick-a-Beeble, claim the prize!
        // Prize — Create two Treasure tokens, then sacrifice Pick-a-Beeble and open an Attraction.
        this.addAbility(new PickABeebleTriggeredAbility().withFlavorWord("Visit"));
    }

    private PickABeeble(final PickABeeble card) {
        super(card);
    }

    @Override
    public PickABeeble copy() {
        return new PickABeeble(this);
    }
}

class PickABeebleTriggeredAbility extends TriggeredAbilityImpl {

    PickABeebleTriggeredAbility() {
        super(Zone.BATTLEFIELD, new PickABeebleEffect(), false);
    }

    private PickABeebleTriggeredAbility(final PickABeebleTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public PickABeebleTriggeredAbility copy() {
        return new PickABeebleTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        if (drEvent.getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        int result = drEvent.getResult();
        return result == 2 || result == 3 || result == 6;
    }

    @Override
    public String getRule() {
        return "Roll a six-sided die. Put a number of luck counters on {this} equal to the result "
                + "and create a Treasure token. Then if there are six or more luck counters on {this}, claim the prize! "
                + "Prize — Create two Treasure tokens, then sacrifice {this} and open an Attraction.";
    }
}

class PickABeebleEffect extends OneShotEffect {

    PickABeebleEffect() {
        super(Outcome.Benefit);
        staticText = "Roll a six-sided die. Put a number of luck counters on {this} equal to the result "
                + "and create a Treasure token. Then if there are six or more luck counters on {this}, claim the prize!";
    }

    private PickABeebleEffect(final PickABeebleEffect effect) {
        super(effect);
    }

    @Override
    public PickABeebleEffect copy() {
        return new PickABeebleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int roll = controller.rollDice(Outcome.Benefit, source, game, 6);
        Permanent perm = source.getSourcePermanentIfItStillExists(game);
        if (perm != null && roll > 0) {
            perm.addCounters(CounterType.LUCK.createInstance(roll), source.getControllerId(), source, game);
        }
        new TreasureToken().putOntoBattlefield(1, game, source);
        if (perm != null && perm.getCounters(game).getCount(CounterType.LUCK) >= 6) {
            new TreasureToken().putOntoBattlefield(2, game, source);
            perm.sacrifice(source, game);
            new PickABeebleAttractionToken().putOntoBattlefield(1, game, source);
        }
        return true;
    }
}

class PickABeebleAttractionToken extends TokenImpl {

    PickABeebleAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private PickABeebleAttractionToken(final PickABeebleAttractionToken token) {
        super(token);
    }

    @Override
    public PickABeebleAttractionToken copy() {
        return new PickABeebleAttractionToken(this);
    }
}
