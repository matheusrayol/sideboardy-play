package mage.cards.e;

import java.util.UUID;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveCounterCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;

/**
 *
 * @author muz
 */
public final class EnsignHarryKimOperations extends CardImpl {

    public EnsignHarryKimOperations(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // Whenever another creature you control enters, if it has greater power than Ensign Kim, put a +1/+1 counter on Ensign Kim.
        this.addAbility(new EnsignHarryKimTriggeredAbility());

        // {2}, Remove two +1/+1 counters from among creatures you control: Draw a card.
        Ability ability = new SimpleActivatedAbility(new DrawCardSourceControllerEffect(1), new GenericManaCost(2));
        ability.addCost(new RemoveCounterCost(
                new TargetControlledCreaturePermanent(1, 2), CounterType.P1P1, 2
        ).setText("remove two +1/+1 counters from among creatures you control"));
        this.addAbility(ability);
    }

    private EnsignHarryKimOperations(final EnsignHarryKimOperations card) {
        super(card);
    }

    @Override
    public EnsignHarryKimOperations copy() {
        return new EnsignHarryKimOperations(this);
    }
}

class EnsignHarryKimTriggeredAbility extends TriggeredAbilityImpl {

    EnsignHarryKimTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.P1P1.createInstance()), false);
        setTriggerPhrase("Whenever another creature you control enters, ");
    }

    private EnsignHarryKimTriggeredAbility(final EnsignHarryKimTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public EnsignHarryKimTriggeredAbility copy() {
        return new EnsignHarryKimTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getTargetId().equals(getSourceId())) {
            return false;
        }
        Permanent entering = game.getPermanent(event.getTargetId());
        Permanent sourcePermanent = getSourcePermanentOrLKI(game);
        if (entering == null
                || !entering.isControlledBy(getControllerId())
                || !entering.isCreature(game)
                || sourcePermanent == null) {
            return false;
        }
        if (entering.getPower().getValue() <= sourcePermanent.getPower().getValue()) {
            return false;
        }
        getEffects().setValue("enteringMor", new MageObjectReference(entering, game));
        return true;
    }

    @Override
    public boolean checkInterveningIfClause(Game game) {
        Object value = getEffects().get(0).getValue("enteringMor");
        if (!(value instanceof MageObjectReference)) {
            return false;
        }
        MageObjectReference mor = (MageObjectReference) value;
        Permanent entering = mor.getPermanentOrLKIBattlefield(game);
        Permanent sourcePermanent = getSourcePermanentOrLKI(game);
        return entering != null
                && sourcePermanent != null
                && entering.getPower().getValue() > sourcePermanent.getPower().getValue();
    }

    @Override
    public String getRule() {
        return "Whenever another creature you control enters, if it has greater power than {this}, put a +1/+1 counter on {this}.";
    }
}
