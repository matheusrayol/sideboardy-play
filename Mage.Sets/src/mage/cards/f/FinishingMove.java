package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageWithPowerFromOneToAnotherTargetEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class FinishingMove extends CardImpl {

    public FinishingMove(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{G}");

        // You get {TK}{TK}, then you may put a sticker on a nonland permanent you own. Target creature you control deals damage equal to its power to target creature you don't control.
        this.getSpellAbility().addEffect(new GetTicketCountersControllerEffect(2));
        this.getSpellAbility().addEffect(new FinishingMoveStickerEffect());
        this.getSpellAbility().addEffect(new DamageWithPowerFromOneToAnotherTargetEffect());
        this.getSpellAbility().addTarget(new TargetControlledCreaturePermanent());
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_CREATURE_YOU_DONT_CONTROL));
    }

    private FinishingMove(final FinishingMove card) {
        super(card);
    }

    @Override
    public FinishingMove copy() {
        return new FinishingMove(this);
    }
}

class FinishingMoveStickerEffect extends OneShotEffect {

    FinishingMoveStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private FinishingMoveStickerEffect(final FinishingMoveStickerEffect effect) {
        super(effect);
    }

    @Override
    public FinishingMoveStickerEffect copy() {
        return new FinishingMoveStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
