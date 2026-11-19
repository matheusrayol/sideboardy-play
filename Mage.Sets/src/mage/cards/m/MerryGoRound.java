package mage.cards.m;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.HorsemanshipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.PowerPredicate;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class MerryGoRound extends CardImpl {

    public MerryGoRound(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Creatures you control with power 2 or less gain horsemanship until end of turn.
        this.addAbility(new MerryGoRoundTriggeredAbility().withFlavorWord("Visit"));
    }

    private MerryGoRound(final MerryGoRound card) {
        super(card);
    }

    @Override
    public MerryGoRound copy() {
        return new MerryGoRound(this);
    }
}

class MerryGoRoundTriggeredAbility extends TriggeredAbilityImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent(
            "Creatures you control with power 2 or less"
    );

    static {
        filter.add(new PowerPredicate(ComparisonType.FEWER_THAN, 3));
    }

    MerryGoRoundTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GainAbilityControlledEffect(
                HorsemanshipAbility.getInstance(), Duration.EndOfTurn, filter
        ), false);
    }

    private MerryGoRoundTriggeredAbility(final MerryGoRoundTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MerryGoRoundTriggeredAbility copy() {
        return new MerryGoRoundTriggeredAbility(this);
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
        return "Creatures you control with power 2 or less gain horsemanship until end of turn.";
    }
}
