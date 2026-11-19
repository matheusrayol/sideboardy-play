package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
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
import mage.target.common.TargetCardInOpponentsGraveyard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class BorgQueenPerfectionManifest extends CardImpl {

    public BorgQueenPerfectionManifest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BORG);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // Artifact creatures you control get +2/+0.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                2, 0, Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE
        )));

        // When Borg Queen enters, assimilate target creature card from an opponent's graveyard.
        Ability ability = new EntersBattlefieldTriggeredAbility(new BorgQueenPerfectionManifestEffect());
        ability.addTarget(new TargetCardInOpponentsGraveyard(StaticFilters.FILTER_CARD_CREATURE));
        this.addAbility(ability);
    }

    private BorgQueenPerfectionManifest(final BorgQueenPerfectionManifest card) {
        super(card);
    }

    @Override
    public BorgQueenPerfectionManifest copy() {
        return new BorgQueenPerfectionManifest(this);
    }
}

class BorgQueenPerfectionManifestEffect extends OneShotEffect {

    BorgQueenPerfectionManifestEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "assimilate target creature card from an opponent's graveyard. "
                + "<i>(Put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private BorgQueenPerfectionManifestEffect(final BorgQueenPerfectionManifestEffect effect) {
        super(effect);
    }

    @Override
    public BorgQueenPerfectionManifestEffect copy() {
        return new BorgQueenPerfectionManifestEffect(this);
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
            game.addEffect(new BorgQueenPerfectionManifestTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        }
        return true;
    }
}

class BorgQueenPerfectionManifestTypeEffect extends ContinuousEffectImpl {

    BorgQueenPerfectionManifestTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private BorgQueenPerfectionManifestTypeEffect(final BorgQueenPerfectionManifestTypeEffect effect) {
        super(effect);
    }

    @Override
    public BorgQueenPerfectionManifestTypeEffect copy() {
        return new BorgQueenPerfectionManifestTypeEffect(this);
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
