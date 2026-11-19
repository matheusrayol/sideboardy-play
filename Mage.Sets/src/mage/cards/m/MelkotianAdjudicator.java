package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackObject;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class MelkotianAdjudicator extends CardImpl {

    public MelkotianAdjudicator(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.subtype.add(SubType.MELKOTIAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Whenever you face a dilemma, put a +1/+1 counter on this creature.
        this.addAbility(new MelkotianAdjudicatorTriggeredAbility());
    }

    private MelkotianAdjudicator(final MelkotianAdjudicator card) {
        super(card);
    }

    @Override
    public MelkotianAdjudicator copy() {
        return new MelkotianAdjudicator(this);
    }

    public static boolean isDilemmaEvent(GameEvent event, UUID controllerId, Game game) {
        if (controllerId == null || !controllerId.equals(event.getPlayerId())) {
            return false;
        }
        if (event.getType() != GameEvent.EventType.SPELL_CAST
                && event.getType() != GameEvent.EventType.ACTIVATED_ABILITY
                && event.getType() != GameEvent.EventType.TRIGGERED_ABILITY) {
            return false;
        }
        StackObject stackObject = game.getStack().getStackObject(event.getTargetId());
        if (stackObject == null) {
            return false;
        }
        Ability stackAbility = stackObject.getStackAbility();
        return stackAbility != null
                && stackAbility.getModes().size() > 1
                && !stackAbility.getModes().getSelectedModes().isEmpty();
    }
}

class MelkotianAdjudicatorTriggeredAbility extends TriggeredAbilityImpl {

    MelkotianAdjudicatorTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    private MelkotianAdjudicatorTriggeredAbility(final MelkotianAdjudicatorTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MelkotianAdjudicatorTriggeredAbility copy() {
        return new MelkotianAdjudicatorTriggeredAbility(this);
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
