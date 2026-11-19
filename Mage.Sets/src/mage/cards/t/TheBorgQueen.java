package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TheBorgQueen extends CardImpl {

    public TheBorgQueen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BORG);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Artifact creatures you control get +2/+2.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                2, 2, Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE
        )));

        // Whenever The Borg Queen enters or attacks, target opponent exiles cards from the top of their library until they exile a creature card.
        // Assimilate that card. They put the rest on the bottom in a random order.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new TheBorgQueenEffect());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);
    }

    private TheBorgQueen(final TheBorgQueen card) {
        super(card);
    }

    @Override
    public TheBorgQueen copy() {
        return new TheBorgQueen(this);
    }
}

class TheBorgQueenEffect extends OneShotEffect {

    TheBorgQueenEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "target opponent exiles cards from the top of their library until they exile a creature card. "
                + "Assimilate that card. They put the rest on the bottom in a random order. "
                + "<i>(To assimilate a card, put it onto the battlefield under your control with a +1/+1 counter. "
                + "It's a Borg artifact creature and loses all other creature types.)</i>";
    }

    private TheBorgQueenEffect(final TheBorgQueenEffect effect) {
        super(effect);
    }

    @Override
    public TheBorgQueenEffect copy() {
        return new TheBorgQueenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        Cards exiled = new CardsImpl();
        Card creatureCard = null;
        while (opponent.getLibrary().hasCards()) {
            Card card = opponent.getLibrary().getFromTop(game);
            if (card == null) {
                break;
            }
            opponent.moveCards(card, Zone.EXILED, source, game);
            exiled.add(card);
            if (card.isCreature(game)) {
                creatureCard = card;
                break;
            }
        }
        if (creatureCard != null) {
            game.setEnterWithCounters(creatureCard.getId(), new Counters().addCounter(CounterType.P1P1.createInstance()));
            controller.moveCards(creatureCard, Zone.BATTLEFIELD, source, game);
            Permanent permanent = CardUtil.getPermanentFromCardPutToBattlefield(creatureCard, game);
            if (permanent != null) {
                game.addEffect(new TheBorgQueenTypeEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
            }
        }
        exiled.retainZone(Zone.EXILED, game);
        if (!exiled.isEmpty()) {
            opponent.putCardsOnBottomOfLibrary(exiled, game, source, false);
        }
        return true;
    }
}

class TheBorgQueenTypeEffect extends ContinuousEffectImpl {

    TheBorgQueenTypeEffect() {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private TheBorgQueenTypeEffect(final TheBorgQueenTypeEffect effect) {
        super(effect);
    }

    @Override
    public TheBorgQueenTypeEffect copy() {
        return new TheBorgQueenTypeEffect(this);
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
