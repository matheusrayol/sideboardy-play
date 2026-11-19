package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DeathIsIrrelevant extends CardImpl {

    public DeathIsIrrelevant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{6}{B}{B}");

        // Artifact creatures you control have indestructible.
        this.addAbility(new SimpleStaticAbility(new GainAbilityControlledEffect(
                IndestructibleAbility.getInstance(),
                Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE
        )));

        // At the beginning of your end step, assimilate target creature card from a graveyard.
        Ability ability = new BeginningOfEndStepTriggeredAbility(new DeathIsIrrelevantEffect());
        ability.addTarget(new TargetCardInGraveyard(StaticFilters.FILTER_CARD_CREATURE_A_GRAVEYARD));
        this.addAbility(ability);
    }

    private DeathIsIrrelevant(final DeathIsIrrelevant card) {
        super(card);
    }

    @Override
    public DeathIsIrrelevant copy() {
        return new DeathIsIrrelevant(this);
    }
}

class DeathIsIrrelevantEffect extends OneShotEffect {

    DeathIsIrrelevantEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "assimilate target creature card from a graveyard. "
                + "<i>(Put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private DeathIsIrrelevantEffect(final DeathIsIrrelevantEffect effect) {
        super(effect);
    }

    @Override
    public DeathIsIrrelevantEffect copy() {
        return new DeathIsIrrelevantEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        game.setEnterWithCounters(card.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
        controller.moveCards(card, Zone.BATTLEFIELD, source, game);
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent != null) {
            game.addEffect(new DeathIsIrrelevantAssimilateEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        }
        return true;
    }
}

class DeathIsIrrelevantAssimilateEffect extends ContinuousEffectImpl {

    DeathIsIrrelevantAssimilateEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private DeathIsIrrelevantAssimilateEffect(final DeathIsIrrelevantAssimilateEffect effect) {
        super(effect);
    }

    @Override
    public DeathIsIrrelevantAssimilateEffect copy() {
        return new DeathIsIrrelevantAssimilateEffect(this);
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
