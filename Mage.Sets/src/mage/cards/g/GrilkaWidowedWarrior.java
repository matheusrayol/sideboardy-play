package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.dynamicvalue.common.SavedDamageValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GrilkaWidowedWarrior extends CardImpl {

    private static final FilterCreaturePermanent filter =
            new FilterCreaturePermanent("creature an opponent controls");

    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public GrilkaWidowedWarrior(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // When Grilka enters, gain control of target creature an opponent controls until end of turn. Untap it.
        // Until end of turn, it gains trample, haste, and "Whenever this creature deals combat damage to a player, create that many tapped Treasure tokens."
        Ability ability = new EntersBattlefieldTriggeredAbility(new GrilkaWidowedWarriorEffect());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private GrilkaWidowedWarrior(final GrilkaWidowedWarrior card) {
        super(card);
    }

    @Override
    public GrilkaWidowedWarrior copy() {
        return new GrilkaWidowedWarrior(this);
    }
}

class GrilkaWidowedWarriorEffect extends OneShotEffect {

    GrilkaWidowedWarriorEffect() {
        super(Outcome.GainControl);
        this.staticText = "gain control of target creature an opponent controls until end of turn. Untap it. "
                + "Until end of turn, it gains trample, haste, and "
                + "\"Whenever this creature deals combat damage to a player, create that many tapped Treasure tokens.\"";
    }

    private GrilkaWidowedWarriorEffect(final GrilkaWidowedWarriorEffect effect) {
        super(effect);
    }

    @Override
    public GrilkaWidowedWarriorEffect copy() {
        return new GrilkaWidowedWarriorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (creature == null) {
            return false;
        }
        game.addEffect(new GainControlTargetEffect(Duration.EndOfTurn)
                .setTargetPointer(new FixedTarget(creature, game)), source);
        creature.untap(game);
        game.addEffect(new GainAbilityTargetEffect(TrampleAbility.getInstance(), Duration.EndOfTurn)
                .setTargetPointer(new FixedTarget(creature, game)), source);
        game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.EndOfTurn)
                .setTargetPointer(new FixedTarget(creature, game)), source);
        game.addEffect(new GainAbilityTargetEffect(
                new DealsCombatDamageToAPlayerTriggeredAbility(
                        new CreateTokenEffect(new TreasureToken(), SavedDamageValue.MANY, true, false),
                        false
                ),
                Duration.EndOfTurn
        ).setTargetPointer(new FixedTarget(creature, game)), source);
        return true;
    }
}
