package mage.cards.c;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.FoodToken;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ConcessionStand extends CardImpl {

    public ConcessionStand(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Create a Food token.
        this.addAbility(new ConcessionStandTriggeredAbility().withFlavorWord("Visit"));
    }

    private ConcessionStand(final ConcessionStand card) {
        super(card);
    }

    @Override
    public ConcessionStand copy() {
        return new ConcessionStand(this);
    }
}

class ConcessionStandTriggeredAbility extends TriggeredAbilityImpl {

    ConcessionStandTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new FoodToken()), false);
    }

    private ConcessionStandTriggeredAbility(final ConcessionStandTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ConcessionStandTriggeredAbility copy() {
        return new ConcessionStandTriggeredAbility(this);
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
        return "Create a Food token.";
    }
}
