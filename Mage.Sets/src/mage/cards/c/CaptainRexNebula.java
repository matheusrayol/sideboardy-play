package mage.cards.c;

import mage.MageInt;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsDamageSourceTriggeredAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAnyTarget;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CaptainRexNebula extends CardImpl {

    public CaptainRexNebula(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PILOT);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // At the beginning of combat on your turn, choose target nonland permanent you control. Until end of turn, it becomes a Vehicle artifact with base power and toughness each equal to its mana value, and it gains crew 2 and "Crash Land — Whenever this Vehicle deals damage, roll a six-sided die. If the result is equal to this Vehicle's mana value, sacrifice this Vehicle, then it deals that much damage to any target."
        Ability ability = new BeginningOfCombatTriggeredAbility(
                TargetController.YOU, new CaptainRexNebulaEffect(), false
        );
        ability.addTarget(new TargetControlledPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND));
        this.addAbility(ability);
    }

    private CaptainRexNebula(final CaptainRexNebula card) {
        super(card);
    }

    @Override
    public CaptainRexNebula copy() {
        return new CaptainRexNebula(this);
    }
}

class CaptainRexNebulaEffect extends OneShotEffect {

    CaptainRexNebulaEffect() {
        super(Outcome.BecomeCreature);
        staticText = "choose target nonland permanent you control. Until end of turn, "
                + "it becomes a Vehicle artifact with base power and toughness each equal to its mana value, "
                + "and it gains crew 2 and \"Crash Land — Whenever this Vehicle deals damage, roll a six-sided die. "
                + "If the result is equal to this Vehicle's mana value, sacrifice this Vehicle, "
                + "then it deals that much damage to any target.\"";
    }

    private CaptainRexNebulaEffect(final CaptainRexNebulaEffect effect) {
        super(effect);
    }

    @Override
    public CaptainRexNebulaEffect copy() {
        return new CaptainRexNebulaEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetPermanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetPermanent == null) {
            return false;
        }
        game.addEffect(new CaptainRexVehicleContinuousEffect(
                new MageObjectReference(targetPermanent, game),
                targetPermanent.getManaValue()
        ), source);
        return true;
    }
}

class CaptainRexVehicleContinuousEffect extends ContinuousEffectImpl {

    private final MageObjectReference targetMor;
    private final int manaValue;

    CaptainRexVehicleContinuousEffect(MageObjectReference targetMor, int manaValue) {
        super(Duration.EndOfTurn, Outcome.BecomeCreature);
        this.targetMor = targetMor;
        this.manaValue = manaValue;
    }

    private CaptainRexVehicleContinuousEffect(final CaptainRexVehicleContinuousEffect effect) {
        super(effect);
        this.targetMor = effect.targetMor;
        this.manaValue = effect.manaValue;
    }

    @Override
    public CaptainRexVehicleContinuousEffect copy() {
        return new CaptainRexVehicleContinuousEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        Permanent permanent = targetMor.getPermanent(game);
        if (permanent == null) {
            discard();
            return false;
        }
        switch (layer) {
            case TypeChangingEffects_4:
                permanent.removeAllCardTypes(game);
                permanent.addCardType(game, CardType.ARTIFACT);
                permanent.addSubType(game, SubType.VEHICLE);
                return true;
            case AbilityAddingRemovingEffects_6:
                permanent.addAbility(new CrewAbility(2), source.getSourceId(), game);
                permanent.addAbility(
                        new DealsDamageSourceTriggeredAbility(new CaptainRexCrashLandEffect())
                                .withFlavorWord("Crash Land"),
                        source.getSourceId(),
                        game
                );
                return true;
            case PTChangingEffects_7:
                if (sublayer == SubLayer.SetPT_7b) {
                    permanent.getPower().setModifiedBaseValue(manaValue);
                    permanent.getToughness().setModifiedBaseValue(manaValue);
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4
                || layer == Layer.AbilityAddingRemovingEffects_6
                || layer == Layer.PTChangingEffects_7;
    }
}

class CaptainRexCrashLandEffect extends OneShotEffect {

    CaptainRexCrashLandEffect() {
        super(Outcome.Damage);
        staticText = "roll a six-sided die. If the result is equal to this Vehicle's mana value, "
                + "sacrifice this Vehicle, then it deals that much damage to any target";
    }

    private CaptainRexCrashLandEffect(final CaptainRexCrashLandEffect effect) {
        super(effect);
    }

    @Override
    public CaptainRexCrashLandEffect copy() {
        return new CaptainRexCrashLandEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        MageObject sourceObj = source.getSourcePermanentOrLKI(game);
        int mv = sourceObj != null ? sourceObj.getManaValue() : 0;
        int roll = controller.rollDice(Outcome.Benefit, source, game, 6);
        if (roll == mv) {
            Permanent perm = source.getSourcePermanentIfItStillExists(game);
            if (perm != null) {
                perm.sacrifice(source, game);
            }
            ReflexiveTriggeredAbility reflexive = new ReflexiveTriggeredAbility(
                    new DamageTargetEffect(roll), false
            );
            reflexive.addTarget(new TargetAnyTarget());
            game.fireReflexiveTriggeredAbility(reflexive, source);
        }
        return true;
    }
}
