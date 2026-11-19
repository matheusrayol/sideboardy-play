package mage.cards.k;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.GoadedPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class KangSuspiciousCaptain extends CardImpl {

    private static final FilterCreaturePermanent filter =
            new FilterCreaturePermanent("creature an opponent controls");

    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public KangSuspiciousCaptain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When Kang enters, choose target creature an opponent controls. Kang and that creature are goaded for as long as you control Kang.
        Ability etb = new EntersBattlefieldTriggeredAbility(new KangGoadEffect());
        etb.addTarget(new TargetPermanent(filter));
        this.addAbility(etb);

        // Whenever one or more goaded creatures attack, put a +1/+1 counter on target creature you control.
        Ability trigger = new KangAttackTriggeredAbility();
        trigger.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(trigger);
    }

    private KangSuspiciousCaptain(final KangSuspiciousCaptain card) {
        super(card);
    }

    @Override
    public KangSuspiciousCaptain copy() {
        return new KangSuspiciousCaptain(this);
    }
}

class KangGoadEffect extends OneShotEffect {

    KangGoadEffect() {
        super(Outcome.Detriment);
        this.staticText = "choose target creature an opponent controls. {this} and that creature are goaded for as long as you control {this}. "
                + "<i>(They attack each combat if able and attack a player other than you if able.)</i>";
    }

    private KangGoadEffect(final KangGoadEffect effect) {
        super(effect);
    }

    @Override
    public KangGoadEffect copy() {
        return new KangGoadEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent kang = source.getSourcePermanentIfItStillExists(game);
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller == null || kang == null || !kang.isControlledBy(controller.getId())) {
            return false;
        }
        MageObjectReference targetMor = target != null ? new MageObjectReference(target, game) : null;
        game.addEffect(new KangContinuousGoadEffect(new MageObjectReference(kang, game), targetMor), source);
        return true;
    }
}

class KangContinuousGoadEffect extends ContinuousEffectImpl {

    private final MageObjectReference kangMor;
    private final MageObjectReference targetMor;

    KangContinuousGoadEffect(MageObjectReference kangMor, MageObjectReference targetMor) {
        super(Duration.Custom, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        this.kangMor = kangMor;
        this.targetMor = targetMor;
    }

    private KangContinuousGoadEffect(final KangContinuousGoadEffect effect) {
        super(effect);
        this.kangMor = effect.kangMor;
        this.targetMor = effect.targetMor;
    }

    @Override
    public KangContinuousGoadEffect copy() {
        return new KangContinuousGoadEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent kang = kangMor.getPermanent(game);
        if (kang == null || !kang.isControlledBy(source.getControllerId())) {
            discard();
            return false;
        }
        kang.addGoadingPlayer(source.getControllerId());
        if (targetMor != null) {
            Permanent target = targetMor.getPermanent(game);
            if (target != null) {
                target.addGoadingPlayer(source.getControllerId());
            }
        }
        return true;
    }
}

class KangAttackTriggeredAbility extends TriggeredAbilityImpl {

    KangAttackTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false);
        setTriggerPhrase("Whenever one or more goaded creatures attack, ");
    }

    private KangAttackTriggeredAbility(final KangAttackTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public KangAttackTriggeredAbility copy() {
        return new KangAttackTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_ATTACKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getCombat()
                .getAttackers()
                .stream()
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .anyMatch(p -> GoadedPredicate.instance.apply(p, game));
    }
}
