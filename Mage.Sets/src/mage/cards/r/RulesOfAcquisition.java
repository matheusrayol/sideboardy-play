package mage.cards.r;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterNonlandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class RulesOfAcquisition extends CardImpl {

    private static final FilterPermanent filter = new FilterNonlandPermanent("nonland permanent an opponent controls");

    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public RulesOfAcquisition(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{U}{U}");

        // Choose one —
        // • Follow the 3rd Rule — Gain control of target nonland permanent an opponent controls. That opponent draws three cards.
        this.getSpellAbility().addEffect(new RulesOfAcquisitionRule3Effect());
        this.getSpellAbility().addTarget(new TargetPermanent(filter));
        this.getSpellAbility().withFirstModeFlavorWord("Follow the 3rd Rule");

        // • Follow the 98th Rule — Target opponent gains control of target nonland permanent you control. If they do, you draw three cards.
        Mode mode2 = new Mode(new RulesOfAcquisitionRule98Effect());
        mode2.addTarget(new TargetOpponent());
        mode2.addTarget(new TargetControlledPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND));
        mode2.withFlavorWord("Follow the 98th Rule");
        this.getSpellAbility().addMode(mode2);
    }

    private RulesOfAcquisition(final RulesOfAcquisition card) {
        super(card);
    }

    @Override
    public RulesOfAcquisition copy() {
        return new RulesOfAcquisition(this);
    }
}

class RulesOfAcquisitionRule3Effect extends OneShotEffect {

    RulesOfAcquisitionRule3Effect() {
        super(Outcome.GainControl);
        this.staticText = "Gain control of target nonland permanent an opponent controls. That opponent draws three cards";
    }

    private RulesOfAcquisitionRule3Effect(final RulesOfAcquisitionRule3Effect effect) {
        super(effect);
    }

    @Override
    public RulesOfAcquisitionRule3Effect copy() {
        return new RulesOfAcquisitionRule3Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        Player previousController = game.getPlayer(permanent.getControllerId());
        ContinuousEffect effect = new GainControlTargetEffect(Duration.EndOfGame, true);
        effect.setTargetPointer(new FixedTarget(permanent, game));
        game.addEffect(effect, source);
        if (previousController != null) {
            previousController.drawCards(3, source, game);
        }
        return true;
    }
}

class RulesOfAcquisitionRule98Effect extends OneShotEffect {

    RulesOfAcquisitionRule98Effect() {
        super(Outcome.DrawCard);
        this.staticText = "Target opponent gains control of target nonland permanent you control. If they do, you draw three cards";
    }

    private RulesOfAcquisitionRule98Effect(final RulesOfAcquisitionRule98Effect effect) {
        super(effect);
    }

    @Override
    public RulesOfAcquisitionRule98Effect copy() {
        return new RulesOfAcquisitionRule98Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player opponent = game.getPlayer(source.getFirstTarget());
        Permanent permanent = game.getPermanent(source.getTargets().get(1).getFirstTarget());
        if (opponent == null || permanent == null) {
            return false;
        }
        ContinuousEffect effect = new GainControlTargetEffect(Duration.EndOfGame, true, opponent.getId());
        effect.setTargetPointer(new FixedTarget(permanent, game));
        game.addEffect(effect, source);
        game.processAction();
        if (permanent.isControlledBy(opponent.getId())) {
            Player controller = game.getPlayer(source.getControllerId());
            if (controller != null) {
                controller.drawCards(3, source, game);
            }
        }
        return true;
    }
}
