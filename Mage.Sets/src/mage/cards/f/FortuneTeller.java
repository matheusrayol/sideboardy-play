package mage.cards.f;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.keyword.ScryEffect;
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
public final class FortuneTeller extends CardImpl {

    public FortuneTeller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Scry 1.
        this.addAbility(new FortuneTellerTriggeredAbility().withFlavorWord("Visit"));
    }

    private FortuneTeller(final FortuneTeller card) {
        super(card);
    }

    @Override
    public FortuneTeller copy() {
        return new FortuneTeller(this);
    }
}

class FortuneTellerTriggeredAbility extends TriggeredAbilityImpl {

    FortuneTellerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ScryEffect(1, false), false);
    }

    private FortuneTellerTriggeredAbility(final FortuneTellerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public FortuneTellerTriggeredAbility copy() {
        return new FortuneTellerTriggeredAbility(this);
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
        return "Scry 1.";
    }
}
