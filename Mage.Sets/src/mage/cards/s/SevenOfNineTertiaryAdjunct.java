package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SevenOfNineTertiaryAdjunct extends CardImpl {

    public SevenOfNineTertiaryAdjunct(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BORG);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // That Is Irrelevant — {T}: Draw a card, then discard a card.
        // When you discard a nonland card this way, put two charge counters on up to one target artifact.
        this.addAbility(new SimpleActivatedAbility(
                new SevenOfNineTertiaryAdjunctEffect(), new TapSourceCost()
        ).withFlavorWord("That Is Irrelevant"));
    }

    private SevenOfNineTertiaryAdjunct(final SevenOfNineTertiaryAdjunct card) {
        super(card);
    }

    @Override
    public SevenOfNineTertiaryAdjunct copy() {
        return new SevenOfNineTertiaryAdjunct(this);
    }
}

class SevenOfNineTertiaryAdjunctEffect extends OneShotEffect {

    SevenOfNineTertiaryAdjunctEffect() {
        super(Outcome.DrawCard);
        this.staticText = "draw a card, then discard a card. "
                + "When you discard a nonland card this way, put two charge counters on up to one target artifact";
    }

    private SevenOfNineTertiaryAdjunctEffect(final SevenOfNineTertiaryAdjunctEffect effect) {
        super(effect);
    }

    @Override
    public SevenOfNineTertiaryAdjunctEffect copy() {
        return new SevenOfNineTertiaryAdjunctEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.drawCards(1, source, game);
        Cards discarded = controller.discard(1, false, false, source, game);
        if (discarded.getCards(game).stream().anyMatch(card -> !card.isLand(game))) {
            ReflexiveTriggeredAbility ability = new ReflexiveTriggeredAbility(
                    new AddCountersTargetEffect(CounterType.CHARGE.createInstance(2)),
                    false,
                    "put two charge counters on up to one target artifact"
            );
            ability.addTarget(new TargetPermanent(0, 1, StaticFilters.FILTER_PERMANENT_ARTIFACT, false));
            game.fireReflexiveTriggeredAbility(ability, source);
        }
        return true;
    }
}
