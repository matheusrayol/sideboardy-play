package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.card.PutIntoGraveFromBattlefieldThisTurnPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;
import mage.target.targetadjustment.ForEachPlayerTargetsAdjuster;
import mage.target.targetpointer.EachTargetPointer;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;
import mage.watchers.common.CardsPutIntoGraveyardWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AssimilateTheWounded extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard(
            "creature card from that player's graveyard that was put there from the battlefield this turn"
    );

    static {
        filter.add(PutIntoGraveFromBattlefieldThisTurnPredicate.instance);
    }

    public AssimilateTheWounded(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{B}");

        // For each opponent, assimilate up to one target creature card from that player's graveyard that was put there from the battlefield this turn.
        this.getSpellAbility().addEffect(new AssimilateTheWoundedEffect());
        this.getSpellAbility().addTarget(new TargetCardInGraveyard(0, 1, filter));
        this.getSpellAbility().setTargetAdjuster(new ForEachPlayerTargetsAdjuster(true, true));
        this.getSpellAbility().addWatcher(new CardsPutIntoGraveyardWatcher());
    }

    private AssimilateTheWounded(final AssimilateTheWounded card) {
        super(card);
    }

    @Override
    public AssimilateTheWounded copy() {
        return new AssimilateTheWounded(this);
    }
}

class AssimilateTheWoundedEffect extends OneShotEffect {

    AssimilateTheWoundedEffect() {
        super(Outcome.PutCreatureInPlay);
        this.setTargetPointer(new EachTargetPointer());
        this.staticText = "for each opponent, assimilate up to one target creature card from that player's graveyard "
                + "that was put there from the battlefield this turn. "
                + "<i>(Put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private AssimilateTheWoundedEffect(final AssimilateTheWoundedEffect effect) {
        super(effect);
    }

    @Override
    public AssimilateTheWoundedEffect copy() {
        return new AssimilateTheWoundedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID targetId : getTargetPointer().getTargets(game, source)) {
            Card card = game.getCard(targetId);
            if (card == null || game.getState().getZone(card.getId()) != Zone.GRAVEYARD) {
                continue;
            }
            game.setEnterWithCounters(card.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
            controller.moveCards(card, Zone.BATTLEFIELD, source, game);
            Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
            if (permanent != null) {
                game.addEffect(new AssimilateTheWoundedTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
            }
        }
        return true;
    }
}

class AssimilateTheWoundedTypeEffect extends ContinuousEffectImpl {

    AssimilateTheWoundedTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private AssimilateTheWoundedTypeEffect(final AssimilateTheWoundedTypeEffect effect) {
        super(effect);
    }

    @Override
    public AssimilateTheWoundedTypeEffect copy() {
        return new AssimilateTheWoundedTypeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            discard();
            return false;
        }
        permanent.addCardType(game, CardType.ARTIFACT);
        permanent.addCardType(game, CardType.CREATURE);
        permanent.removeAllCreatureTypes(game);
        permanent.addSubType(game, SubType.BORG);
        return true;
    }
}
