package mage.cards.l;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.CantBeBlockedTargetEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.target.common.TargetControlledPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.common.SpellsCastWatcher;

import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class LockjawSlobberingTeleporter extends CardImpl {

    public LockjawSlobberingTeleporter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.INHUMAN);
        this.subtype.add(SubType.DOG);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // At the beginning of combat on your turn, if you've cast a noncreature spell this turn, put a +1/+1 counter on Lockjaw.
        // When you do, Lockjaw and up to one other target creature you control can't be blocked this turn.
        this.addAbility(new BeginningOfCombatTriggeredAbility(new LockjawEffect())
                .withInterveningIf(LockjawCondition.instance), new SpellsCastWatcher());
    }

    private LockjawSlobberingTeleporter(final LockjawSlobberingTeleporter card) {
        super(card);
    }

    @Override
    public LockjawSlobberingTeleporter copy() {
        return new LockjawSlobberingTeleporter(this);
    }
}

enum LockjawCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        if (watcher == null) {
            return false;
        }
        List<Spell> spells = watcher.getSpellsCastThisTurn(source.getControllerId());
        return spells != null && spells.stream().anyMatch(spell -> spell != null && !spell.isCreature(game));
    }

    @Override
    public String toString() {
        return "you've cast a noncreature spell this turn";
    }
}

class LockjawEffect extends OneShotEffect {

    private static final FilterControlledCreaturePermanent filter =
            new FilterControlledCreaturePermanent("other target creature you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    LockjawEffect() {
        super(Outcome.BoostCreature);
        this.staticText = "put a +1/+1 counter on {this}. When you do, {this} and up to one other target creature you control can't be blocked this turn";
    }

    private LockjawEffect(final LockjawEffect effect) {
        super(effect);
    }

    @Override
    public LockjawEffect copy() {
        return new LockjawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent lockjaw = source.getSourcePermanentIfItStillExists(game);
        if (lockjaw == null || !lockjaw.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game)) {
            return false;
        }
        ReflexiveTriggeredAbility reflexive = new ReflexiveTriggeredAbility(
                new LockjawCantBeBlockedEffect(new MageObjectReference(lockjaw, game)),
                false,
                "{this} and up to one other target creature you control can't be blocked this turn"
        );
        reflexive.addTarget(new TargetControlledPermanent(0, 1, filter, false));
        game.fireReflexiveTriggeredAbility(reflexive, source);
        return true;
    }
}

class LockjawCantBeBlockedEffect extends OneShotEffect {

    private final MageObjectReference lockjawMor;

    LockjawCantBeBlockedEffect(MageObjectReference lockjawMor) {
        super(Outcome.Benefit);
        this.lockjawMor = lockjawMor;
    }

    private LockjawCantBeBlockedEffect(final LockjawCantBeBlockedEffect effect) {
        super(effect);
        this.lockjawMor = effect.lockjawMor;
    }

    @Override
    public LockjawCantBeBlockedEffect copy() {
        return new LockjawCantBeBlockedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent lockjaw = lockjawMor.getPermanent(game);
        if (lockjaw != null) {
            game.addEffect(new CantBeBlockedTargetEffect(Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(lockjaw, game)), source);
        }
        Permanent other = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (other != null) {
            game.addEffect(new CantBeBlockedTargetEffect(Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(other, game)), source);
        }
        return true;
    }
}
