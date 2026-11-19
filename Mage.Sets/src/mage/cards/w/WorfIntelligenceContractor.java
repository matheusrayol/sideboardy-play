package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.AddCardSubTypeTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.targetadjustment.ForEachPlayerTargetsAdjuster;
import mage.target.targetpointer.EachTargetPointer;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class WorfIntelligenceContractor extends CardImpl {

    private static final FilterCreaturePermanent filter =
            new FilterCreaturePermanent("creature with mana value 3 or less");

    static {
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, 4));
    }

    public WorfIntelligenceContractor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.SPY);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // When Worf enters, for each opponent, gain control of up to one target creature that player controls with mana value 3 or less until end of turn.
        // Untap those creatures. Until end of turn, they become Warriors in addition to their other types and gain haste.
        Ability ability = new EntersBattlefieldTriggeredAbility(new WorfIntelligenceContractorEffect());
        ability.addTarget(new TargetPermanent(0, 1, filter, false));
        ability.setTargetAdjuster(new ForEachPlayerTargetsAdjuster(false, true));
        this.addAbility(ability);
    }

    private WorfIntelligenceContractor(final WorfIntelligenceContractor card) {
        super(card);
    }

    @Override
    public WorfIntelligenceContractor copy() {
        return new WorfIntelligenceContractor(this);
    }
}

class WorfIntelligenceContractorEffect extends OneShotEffect {

    WorfIntelligenceContractorEffect() {
        super(Outcome.GainControl);
        this.setTargetPointer(new EachTargetPointer());
        this.staticText = "for each opponent, gain control of up to one target creature that player controls "
                + "with mana value 3 or less until end of turn. Untap those creatures. "
                + "Until end of turn, they become Warriors in addition to their other types and gain haste";
    }

    private WorfIntelligenceContractorEffect(final WorfIntelligenceContractorEffect effect) {
        super(effect);
    }

    @Override
    public WorfIntelligenceContractorEffect copy() {
        return new WorfIntelligenceContractorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Permanent creature = game.getPermanent(targetId);
            if (creature == null) {
                continue;
            }
            game.addEffect(new GainControlTargetEffect(Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(creature, game)), source);
            creature.untap(game);
            game.addEffect(new AddCardSubTypeTargetEffect(SubType.WARRIOR, Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(creature, game)), source);
            game.addEffect(new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.EndOfTurn)
                    .setTargetPointer(new FixedTarget(creature, game)), source);
        }
        return true;
    }
}
