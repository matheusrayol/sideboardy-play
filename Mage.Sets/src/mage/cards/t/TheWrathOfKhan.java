package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.common.delayed.AtTheBeginOfCombatDelayedTriggeredAbility;
import mage.abilities.costs.common.ExileFromHandCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.SacrificeOpponentsEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetOpponentsCreaturePermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class TheWrathOfKhan extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("nontoken permanent");

    static {
        filter.add(TokenPredicate.FALSE);
    }

    public TheWrathOfKhan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{R}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_III);

        // I — This Saga deals 4 damage to target creature an opponent controls. At the beginning of the next combat this turn, gain control of that creature until end of turn. Untap it. It gains haste until end of turn.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_I,
                new TheWrathOfKhanChapterIEffect(),
                new TargetOpponentsCreaturePermanent()
        );

        // II — You may exile a card from your hand. If you do, draw a card.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II,
                new DoIfCostPaid(new DrawCardSourceControllerEffect(1), new ExileFromHandCost(new TargetCardInHand()))
        );

        // III — Each opponent sacrifices a nontoken permanent of their choice.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III,
                new SacrificeOpponentsEffect(filter)
        );

        this.addAbility(sagaAbility);
    }

    private TheWrathOfKhan(final TheWrathOfKhan card) {
        super(card);
    }

    @Override
    public TheWrathOfKhan copy() {
        return new TheWrathOfKhan(this);
    }
}

class TheWrathOfKhanChapterIEffect extends OneShotEffect {

    TheWrathOfKhanChapterIEffect() {
        super(Outcome.Damage);
        this.staticText = "{this} deals 4 damage to target creature an opponent controls. "
                + "At the beginning of the next combat this turn, gain control of that creature until end of turn. "
                + "Untap it. It gains haste until end of turn";
    }

    private TheWrathOfKhanChapterIEffect(final TheWrathOfKhanChapterIEffect effect) {
        super(effect);
    }

    @Override
    public TheWrathOfKhanChapterIEffect copy() {
        return new TheWrathOfKhanChapterIEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        permanent.damage(4, source, game);
        AtTheBeginOfCombatDelayedTriggeredAbility delayed = new AtTheBeginOfCombatDelayedTriggeredAbility(
                new TheWrathOfKhanStealEffect(new MageObjectReference(permanent, game))
        );
        game.addDelayedTriggeredAbility(delayed, source);
        return true;
    }
}

class TheWrathOfKhanStealEffect extends OneShotEffect {

    private final MageObjectReference mor;

    TheWrathOfKhanStealEffect(MageObjectReference mor) {
        super(Outcome.GainControl);
        this.mor = mor;
        this.staticText = "gain control of that creature until end of turn. Untap it. It gains haste until end of turn";
    }

    private TheWrathOfKhanStealEffect(final TheWrathOfKhanStealEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public TheWrathOfKhanStealEffect copy() {
        return new TheWrathOfKhanStealEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = mor.getPermanent(game);
        if (permanent == null) {
            return false;
        }
        FixedTarget target = new FixedTarget(permanent, game);
        ContinuousEffect gainControl = new GainControlTargetEffect(Duration.EndOfTurn);
        gainControl.setTargetPointer(target.copy());
        game.addEffect(gainControl, source);
        permanent.untap(game);
        ContinuousEffect gainHaste = new GainAbilityTargetEffect(HasteAbility.getInstance(), Duration.EndOfTurn);
        gainHaste.setTargetPointer(target.copy());
        game.addEffect(gainHaste, source);
        return true;
    }
}
