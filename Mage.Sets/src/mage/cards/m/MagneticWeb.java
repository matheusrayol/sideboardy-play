package mage.cards.m;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AttacksAllTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.RequirementEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class MagneticWeb extends CardImpl {

    private static final FilterCreaturePermanent filterMagnetCreature = new FilterCreaturePermanent("a creature with a magnet counter on it");

    static {
        filterMagnetCreature.add((Predicate<Permanent>) (input, game) -> input.getCounters(game).getCount("magnet") > 0);
    }

    public MagneticWeb(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // If a creature with a magnet counter on it attacks, all creatures with magnet counters on them attack if able.
        this.addAbility(new SimpleStaticAbility(new MagneticWebAttackRequirementEffect()));

        // Whenever a creature with a magnet counter on it attacks, all creatures with magnet counters on them block that creature this turn if able.
        this.addAbility(new AttacksAllTriggeredAbility(
                new MagneticWebBlockEffect(),
                false,
                filterMagnetCreature,
                SetTargetPointer.PERMANENT,
                false
        ));

        // {1}, {T}: Put a magnet counter on target creature.
        Ability ability = new SimpleActivatedAbility(
                new AddCountersTargetEffect(new Counter("magnet")),
                new GenericManaCost(1)
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private MagneticWeb(final MagneticWeb card) {
        super(card);
    }

    @Override
    public MagneticWeb copy() {
        return new MagneticWeb(this);
    }
}

class MagneticWebAttackRequirementEffect extends RequirementEffect {

    MagneticWebAttackRequirementEffect() {
        super(Duration.WhileOnBattlefield);
        this.staticText = "if a creature with a magnet counter on it attacks, all creatures with magnet counters on them attack if able";
    }

    private MagneticWebAttackRequirementEffect(final MagneticWebAttackRequirementEffect effect) {
        super(effect);
    }

    @Override
    public MagneticWebAttackRequirementEffect copy() {
        return new MagneticWebAttackRequirementEffect(this);
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        if (permanent.getCounters(game).getCount("magnet") <= 0) {
            return false;
        }
        for (UUID attackerId : game.getCombat().getAttackers()) {
            Permanent attacker = game.getPermanent(attackerId);
            if (attacker != null && attacker.getCounters(game).getCount("magnet") > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mustAttack(Game game) {
        return true;
    }

    @Override
    public boolean mustBlock(Game game) {
        return false;
    }
}

class MagneticWebBlockEffect extends RequirementEffect {

    private MageObjectReference attackerMor;

    MagneticWebBlockEffect() {
        super(Duration.EndOfTurn);
        this.staticText = "all creatures with magnet counters on them block that creature this turn if able";
    }

    private MagneticWebBlockEffect(final MagneticWebBlockEffect effect) {
        super(effect);
        this.attackerMor = effect.attackerMor;
    }

    @Override
    public MagneticWebBlockEffect copy() {
        return new MagneticWebBlockEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Permanent attacker = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (attacker != null) {
            attackerMor = new MageObjectReference(attacker, game);
        } else {
            discard();
        }
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        return permanent.getCounters(game).getCount("magnet") > 0;
    }

    @Override
    public boolean mustAttack(Game game) {
        return false;
    }

    @Override
    public boolean mustBlock(Game game) {
        return true;
    }

    @Override
    public UUID mustBlockAttacker(Ability source, Game game) {
        if (attackerMor == null) {
            return null;
        }
        Permanent attacker = attackerMor.getPermanent(game);
        return attacker != null ? attacker.getId() : null;
    }
}
