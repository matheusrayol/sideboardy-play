package mage.cards.b;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.AffinityAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInExile;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class BorgCube extends CardImpl {

    public BorgCube(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}{W}{B}");

        this.subtype.add(SubType.SPACECRAFT);

        // Affinity for artifact creatures
        this.addAbility(new AffinityAbility(AffinityType.ARTIFACT_CREATURES));

        // Whenever this Spacecraft enters or attacks, assimilate a creature card exiled with it.
        // If you can't, exile up to one target creature.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new BorgCubeEffect());
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        this.addAbility(ability);

        // Station
        this.addAbility(new StationAbility());

        // STATION 12+
        // Artifact creatures you control have flying.
        // 6/6
        this.addAbility(new StationLevelAbility(12)
                .withLevelAbility(new SimpleStaticAbility(new GainAbilityControlledEffect(
                        FlyingAbility.getInstance(),
                        Duration.WhileOnBattlefield,
                        StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE
                )))
                .withPT(6, 6));
    }

    private BorgCube(final BorgCube card) {
        super(card);
    }

    @Override
    public BorgCube copy() {
        return new BorgCube(this);
    }
}

class BorgCubeEffect extends OneShotEffect {

    BorgCubeEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "assimilate a creature card exiled with it. If you can't, exile up to one target creature. "
                + "<i>(To assimilate a card, put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private BorgCubeEffect(final BorgCubeEffect effect) {
        super(effect);
    }

    @Override
    public BorgCubeEffect copy() {
        return new BorgCubeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileZoneId = CardUtil.getExileZoneId(game, source);
        ExileZone exileZone = game.getExile().getExileZone(exileZoneId);
        if (exileZone != null && exileZone.count(StaticFilters.FILTER_CARD_CREATURE, game) > 0) {
            TargetCardInExile target = new TargetCardInExile(
                    1, 1, StaticFilters.FILTER_CARD_CREATURE, exileZoneId
            );
            target.withNotTarget(true);
            controller.choose(Outcome.PutCreatureInPlay, target, source, game);
            Card card = game.getCard(target.getFirstTarget());
            if (card != null) {
                game.setEnterWithCounters(card.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
                controller.moveCards(card, Zone.BATTLEFIELD, source, game);
                Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
                if (permanent != null) {
                    game.addEffect(new BorgCubeAssimilateTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
                }
                return true;
            }
        }
        Permanent targetCreature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetCreature != null) {
            MageObject sourceObject = source.getSourceObject(game);
            String exileName = sourceObject == null ? "Borg Cube" : sourceObject.getIdName();
            controller.moveCardsToExile(targetCreature, source, game, true, exileZoneId, exileName);
        }
        return true;
    }
}

class BorgCubeAssimilateTypeEffect extends ContinuousEffectImpl {

    BorgCubeAssimilateTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private BorgCubeAssimilateTypeEffect(final BorgCubeAssimilateTypeEffect effect) {
        super(effect);
    }

    @Override
    public BorgCubeAssimilateTypeEffect copy() {
        return new BorgCubeAssimilateTypeEffect(this);
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
