package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksPlayerWithCreaturesTriggeredAbility;
import mage.abilities.effects.common.combat.CantBlockTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.EnteredThisTurnPredicate;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.ThatPlayerControlsTargetAdjuster;
import mage.watchers.common.PermanentsEnteredBattlefieldWatcher;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class WhirlwindKillerCyclone extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("creatures you control that entered this turn");

    static {
        filter.add(EnteredThisTurnPredicate.instance);
    }

    public WhirlwindKillerCyclone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MUTANT);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Whenever one or more creatures you control that entered this turn attack a player, target creature that player controls can't block this turn.
        Ability ability = new AttacksPlayerWithCreaturesTriggeredAbility(
                new CantBlockTargetEffect(Duration.EndOfTurn)
                        .setText("target creature that player controls can't block this turn"),
                filter, SetTargetPointer.PLAYER
        );
        ability.addTarget(new TargetCreaturePermanent());
        ability.setTargetAdjuster(new ThatPlayerControlsTargetAdjuster());
        this.addAbility(ability, new PermanentsEnteredBattlefieldWatcher());
    }

    private WhirlwindKillerCyclone(final WhirlwindKillerCyclone card) {
        super(card);
    }

    @Override
    public WhirlwindKillerCyclone copy() {
        return new WhirlwindKillerCyclone(this);
    }
}
