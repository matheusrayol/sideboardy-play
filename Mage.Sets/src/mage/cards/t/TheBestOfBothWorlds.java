package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheBestOfBothWorlds extends CardImpl {

    private static final FilterCreaturePermanent filter =
            new FilterCreaturePermanent("nonartifact creature an opponent controls");

    static {
        filter.add(Predicates.not(CardType.ARTIFACT.getPredicate()));
        filter.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public TheBestOfBothWorlds(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{W}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I — Assimilate target nonartifact creature an opponent controls.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_I,
                new TheBestOfBothWorldsChapter1Effect(),
                new TargetPermanent(filter)
        );

        // II — Artifact creatures you control get +2/+2 until end of turn.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II,
                new BoostControlledEffect(2, 2, Duration.EndOfTurn, StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE)
        );

        // III — Exile the Borg assimilated with this Saga, then return it to the battlefield under its owner's control.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III,
                new TheBestOfBothWorldsChapter3Effect()
        );

        this.addAbility(sagaAbility);
    }

    private TheBestOfBothWorlds(final TheBestOfBothWorlds card) {
        super(card);
    }

    @Override
    public TheBestOfBothWorlds copy() {
        return new TheBestOfBothWorlds(this);
    }
}

class TheBestOfBothWorldsChapter1Effect extends OneShotEffect {

    TheBestOfBothWorldsChapter1Effect() {
        super(Outcome.GainControl);
        this.staticText = "assimilate target nonartifact creature an opponent controls. "
                + "<i>(Gain control of it and put a +1/+1 counter on it. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private TheBestOfBothWorldsChapter1Effect(final TheBestOfBothWorldsChapter1Effect effect) {
        super(effect);
    }

    @Override
    public TheBestOfBothWorldsChapter1Effect copy() {
        return new TheBestOfBothWorldsChapter1Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        game.addEffect(new GainControlTargetEffect(Duration.Custom)
                .setTargetPointer(new FixedTarget(permanent, game)), source);
        permanent.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
        game.addEffect(new TheBestOfBothWorldsTypeEffect()
                .setTargetPointer(new FixedTarget(permanent, game)), source);
        int zcc = CardUtil.getActualSourceObjectZoneChangeCounter(game, source);
        game.getState().setValue(
                "TheBestOfBothWorlds_" + source.getSourceId() + "_" + zcc,
                new MageObjectReference(permanent, game)
        );
        return true;
    }
}

class TheBestOfBothWorldsTypeEffect extends ContinuousEffectImpl {

    TheBestOfBothWorldsTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private TheBestOfBothWorldsTypeEffect(final TheBestOfBothWorldsTypeEffect effect) {
        super(effect);
    }

    @Override
    public TheBestOfBothWorldsTypeEffect copy() {
        return new TheBestOfBothWorldsTypeEffect(this);
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

class TheBestOfBothWorldsChapter3Effect extends OneShotEffect {

    TheBestOfBothWorldsChapter3Effect() {
        super(Outcome.Detriment);
        this.staticText = "exile the Borg assimilated with this Saga, then return it to the battlefield under its owner's control";
    }

    private TheBestOfBothWorldsChapter3Effect(final TheBestOfBothWorldsChapter3Effect effect) {
        super(effect);
    }

    @Override
    public TheBestOfBothWorldsChapter3Effect copy() {
        return new TheBestOfBothWorldsChapter3Effect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int zcc = CardUtil.getActualSourceObjectZoneChangeCounter(game, source);
        Object val = game.getState().getValue("TheBestOfBothWorlds_" + source.getSourceId() + "_" + zcc);
        if (!(val instanceof MageObjectReference)) {
            return true;
        }
        Permanent permanent = ((MageObjectReference) val).getPermanent(game);
        if (permanent == null) {
            return true;
        }
        Card mainCard = permanent.getMainCard();
        controller.moveCards(permanent, Zone.EXILED, source, game);
        if (mainCard != null && game.getState().getZone(mainCard.getId()) == Zone.EXILED) {
            controller.moveCards(mainCard, Zone.BATTLEFIELD, source, game, false, false, true, null);
        }
        return true;
    }
}
