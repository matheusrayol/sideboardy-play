package mage.cards.b;

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
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BounceChamber extends CardImpl {

    public BounceChamber(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Return a creature you don't control with the lowest toughness among creatures you don't control to its owner's hand.
        this.addAbility(new BounceChamberTriggeredAbility().withFlavorWord("Visit"));
    }

    private BounceChamber(final BounceChamber card) {
        super(card);
    }

    @Override
    public BounceChamber copy() {
        return new BounceChamber(this);
    }
}

class BounceChamberTriggeredAbility extends TriggeredAbilityImpl {

    BounceChamberTriggeredAbility() {
        super(Zone.BATTLEFIELD, new BounceChamberEffect(), false);
    }

    private BounceChamberTriggeredAbility(final BounceChamberTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public BounceChamberTriggeredAbility copy() {
        return new BounceChamberTriggeredAbility(this);
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
        return "Return a creature you don't control with the lowest toughness among creatures you don't control to its owner's hand.";
    }
}

class BounceChamberEffect extends OneShotEffect {

    BounceChamberEffect() {
        super(Outcome.ReturnToHand);
        staticText = "Return a creature you don't control with the lowest toughness among creatures you don't control to its owner's hand";
    }

    private BounceChamberEffect(final BounceChamberEffect effect) {
        super(effect);
    }

    @Override
    public BounceChamberEffect copy() {
        return new BounceChamberEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Set<UUID> opponents = game.getOpponents(source.getControllerId());
        Permanent lowest = null;
        for (Permanent perm : game.getBattlefield().getAllActivePermanents()) {
            if (perm.isCreature(game) && opponents.contains(perm.getControllerId())) {
                if (lowest == null || perm.getToughness().getValue() < lowest.getToughness().getValue()) {
                    lowest = perm;
                }
            }
        }
        if (lowest != null) {
            controller.moveCards(lowest, Zone.HAND, source, game);
        }
        return true;
    }
}
