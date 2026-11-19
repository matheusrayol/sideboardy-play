package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.replacement.DiesReplacementEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeBatchEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAnyTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheWarDoctor extends CardImpl {

    public TheWarDoctor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Whenever one or more other permanents phase out and whenever one or more other cards are put into exile from anywhere, put a time counter on The War Doctor.
        this.addAbility(new TheWarDoctorTriggeredAbility());

        // Whenever The War Doctor attacks, it deals damage equal to the number of time counters on it to any target. If a creature dealt damage this way would die this turn, exile it instead.
        Ability ability = new AttacksTriggeredAbility(new TheWarDoctorDamageEffect());
        ability.addTarget(new TargetAnyTarget());
        this.addAbility(ability);
    }

    private TheWarDoctor(final TheWarDoctor card) {
        super(card);
    }

    @Override
    public TheWarDoctor copy() {
        return new TheWarDoctor(this);
    }
}

class TheWarDoctorTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<ZoneChangeEvent> {

    TheWarDoctorTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.TIME.createInstance()), false);
    }

    private TheWarDoctorTriggeredAbility(final TheWarDoctorTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TheWarDoctorTriggeredAbility copy() {
        return new TheWarDoctorTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PHASED_OUT
                || event.getType() == GameEvent.EventType.ZONE_CHANGE_BATCH;
    }

    @Override
    public boolean checkEvent(ZoneChangeEvent event, Game game) {
        return event.getToZone() == Zone.EXILED
                && !event.getTargetId().equals(getSourceId())
                && game.getCard(event.getTargetId()) != null;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PHASED_OUT) {
            return !event.getTargetId().equals(getSourceId());
        }
        return !getFilteredEvents((ZoneChangeBatchEvent) event, game).isEmpty();
    }

    @Override
    public String getRule() {
        return "Whenever one or more other permanents phase out and whenever one or more other cards are put into exile from anywhere, put a time counter on {this}.";
    }
}

class TheWarDoctorDamageEffect extends OneShotEffect {

    TheWarDoctorDamageEffect() {
        super(Outcome.Damage);
        this.staticText = "it deals damage equal to the number of time counters on it to any target. If a creature dealt damage this way would die this turn, exile it instead";
    }

    private TheWarDoctorDamageEffect(final TheWarDoctorDamageEffect effect) {
        super(effect);
    }

    @Override
    public TheWarDoctorDamageEffect copy() {
        return new TheWarDoctorDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentOrLKI(game);
        int amount = sourcePermanent != null ? sourcePermanent.getCounters(game).getCount(CounterType.TIME) : 0;
        UUID targetId = getTargetPointer().getFirst(game, source);
        Permanent targetPerm = game.getPermanent(targetId);
        if (targetPerm != null) {
            if (amount > 0) {
                int dealt = targetPerm.damage(amount, source.getSourceId(), source, game, false, true);
                if (dealt > 0 && targetPerm.isCreature(game)) {
                    game.addEffect(new DiesReplacementEffect(new MageObjectReference(targetPerm, game), Duration.EndOfTurn), source);
                }
            }
            return true;
        }
        Player targetPlayer = game.getPlayer(targetId);
        if (targetPlayer != null) {
            if (amount > 0) {
                targetPlayer.damage(amount, source.getSourceId(), source, game);
            }
            return true;
        }
        return false;
    }
}
