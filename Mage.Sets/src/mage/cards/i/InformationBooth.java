package mage.cards.i;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class InformationBooth extends CardImpl {

    public InformationBooth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Draw a card.
        this.addAbility(new InformationBoothTriggeredAbility().withFlavorWord("Visit"));
    }

    private InformationBooth(final InformationBooth card) {
        super(card);
    }

    @Override
    public InformationBooth copy() {
        return new InformationBooth(this);
    }
}

class InformationBoothTriggeredAbility extends TriggeredAbilityImpl {

    InformationBoothTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawCardSourceControllerEffect(1), false);
    }

    private InformationBoothTriggeredAbility(final InformationBoothTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public InformationBoothTriggeredAbility copy() {
        return new InformationBoothTriggeredAbility(this);
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
        return "Draw a card.";
    }
}
