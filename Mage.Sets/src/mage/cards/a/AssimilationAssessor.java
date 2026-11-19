package mage.cards.a;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInExile;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class AssimilationAssessor extends CardImpl {

    public AssimilationAssessor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{B}");

        this.subtype.add(SubType.BORG);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // When this creature enters, exile target opponent's graveyard.
        Ability ability = new EntersBattlefieldTriggeredAbility(new AssimilationAssessorExileEffect());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);

        // Whenever this creature attacks, assimilate a creature card exiled with it.
        this.addAbility(new AttacksTriggeredAbility(new AssimilationAssessorAttackEffect()));
    }

    private AssimilationAssessor(final AssimilationAssessor card) {
        super(card);
    }

    @Override
    public AssimilationAssessor copy() {
        return new AssimilationAssessor(this);
    }
}

class AssimilationAssessorExileEffect extends OneShotEffect {

    AssimilationAssessorExileEffect() {
        super(Outcome.Exile);
        this.staticText = "exile target opponent's graveyard";
    }

    private AssimilationAssessorExileEffect(final AssimilationAssessorExileEffect effect) {
        super(effect);
    }

    @Override
    public AssimilationAssessorExileEffect copy() {
        return new AssimilationAssessorExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        Cards cards = new CardsImpl(opponent.getGraveyard());
        if (cards.isEmpty()) {
            return true;
        }
        MageObject sourceObject = source.getSourceObject(game);
        String exileName = sourceObject == null ? "Assimilation Assessor" : sourceObject.getIdName();
        return controller.moveCardsToExile(
                cards.getCards(game), source, game, true,
                CardUtil.getExileZoneId(game, source), exileName
        );
    }
}

class AssimilationAssessorAttackEffect extends OneShotEffect {

    AssimilationAssessorAttackEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "assimilate a creature card exiled with it. "
                + "<i>(Put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private AssimilationAssessorAttackEffect(final AssimilationAssessorAttackEffect effect) {
        super(effect);
    }

    @Override
    public AssimilationAssessorAttackEffect copy() {
        return new AssimilationAssessorAttackEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        UUID exileZoneId = CardUtil.getExileZoneId(game, source);
        ExileZone exileZone = game.getExile().getExileZone(exileZoneId);
        if (exileZone == null || exileZone.count(StaticFilters.FILTER_CARD_CREATURE, game) == 0) {
            return true;
        }
        TargetCardInExile target = new TargetCardInExile(
                1, 1, StaticFilters.FILTER_CARD_CREATURE, exileZoneId
        );
        target.withNotTarget(true);
        controller.choose(Outcome.PutCreatureInPlay, target, source, game);
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return false;
        }
        game.setEnterWithCounters(card.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
        controller.moveCards(card, Zone.BATTLEFIELD, source, game);
        Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(card, game);
        if (permanent != null) {
            game.addEffect(new AssimilationAssessorTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        }
        return true;
    }
}

class AssimilationAssessorTypeEffect extends ContinuousEffectImpl {

    AssimilationAssessorTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private AssimilationAssessorTypeEffect(final AssimilationAssessorTypeEffect effect) {
        super(effect);
    }

    @Override
    public AssimilationAssessorTypeEffect copy() {
        return new AssimilationAssessorTypeEffect(this);
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
