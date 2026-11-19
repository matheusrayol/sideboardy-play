package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TrashBin extends CardImpl {

    public TrashBin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Mill two cards, then return a card at random from your graveyard to your hand.
        this.addAbility(new TrashBinTriggeredAbility().withFlavorWord("Visit"));
    }

    private TrashBin(final TrashBin card) {
        super(card);
    }

    @Override
    public TrashBin copy() {
        return new TrashBin(this);
    }
}

class TrashBinTriggeredAbility extends TriggeredAbilityImpl {

    TrashBinTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TrashBinEffect(), false);
    }

    private TrashBinTriggeredAbility(final TrashBinTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TrashBinTriggeredAbility copy() {
        return new TrashBinTriggeredAbility(this);
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
        return result == 2 || result == 6;
    }

    @Override
    public String getRule() {
        return "Mill two cards, then return a card at random from your graveyard to your hand.";
    }
}

class TrashBinEffect extends OneShotEffect {

    TrashBinEffect() {
        super(Outcome.ReturnToHand);
        staticText = "Mill two cards, then return a card at random from your graveyard to your hand";
    }

    private TrashBinEffect(final TrashBinEffect effect) {
        super(effect);
    }

    @Override
    public TrashBinEffect copy() {
        return new TrashBinEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.millCards(2, source, game);
        if (!controller.getGraveyard().isEmpty()) {
            Card randomCard = controller.getGraveyard().getRandom(game);
            if (randomCard != null) {
                controller.moveCards(randomCard, Zone.HAND, source, game);
            }
        }
        return true;
    }
}
