package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DropTower extends CardImpl {

    public DropTower(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Target creature gains flying until end of turn, or until any player rolls a 1, whichever comes first.
        Ability ability = new DropTowerTriggeredAbility();
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private DropTower(final DropTower card) {
        super(card);
    }

    @Override
    public DropTower copy() {
        return new DropTower(this);
    }
}

class DropTowerTriggeredAbility extends TriggeredAbilityImpl {

    DropTowerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GainAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn)
                .setText("Target creature gains flying until end of turn, or until any player rolls a 1, whichever comes first"), false);
    }

    private DropTowerTriggeredAbility(final DropTowerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DropTowerTriggeredAbility copy() {
        return new DropTowerTriggeredAbility(this);
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
        return "Target creature gains flying until end of turn, or until any player rolls a 1, whichever comes first.";
    }
}
