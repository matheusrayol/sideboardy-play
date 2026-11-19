package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CallerOfTheUntamed extends CardImpl {

    public CallerOfTheUntamed(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Before you shuffle your deck to start the game, you may reveal this card from your deck and exile a creature card you drafted that isn't in your deck.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Before you shuffle your deck to start the game, you may reveal {this} from your deck "
                        + "and exile a creature card you drafted that isn't in your deck - not implemented.")
        ));

        // {X}, {T}: Create a token that's a copy of a card you exiled with cards named Caller of the Untamed. X is the mana value of that card.
        Ability ability = new SimpleActivatedAbility(new CallerOfTheUntamedEffect(), new ManaCostsImpl<>("{X}"));
        ability.addCost(new TapSourceCost());
        ability.setCostAdjuster(CallerOfTheUntamedAdjuster.instance);
        this.addAbility(ability);
    }

    private CallerOfTheUntamed(final CallerOfTheUntamed card) {
        super(card);
    }

    @Override
    public CallerOfTheUntamed copy() {
        return new CallerOfTheUntamed(this);
    }
}

enum CallerOfTheUntamedAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareX(Ability ability, Game game) {
        Object raw = game.getState().getValue(ability.getControllerId() + "_caller_of_the_untamed_exile");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            ability.setVariableCostsValue(0);
            return;
        }
        int minMv = Integer.MAX_VALUE;
        int maxMv = 0;
        for (Object id : (Collection<?>) raw) {
            if (id instanceof UUID) {
                Card c = game.getCard((UUID) id);
                if (c != null) {
                    minMv = Math.min(minMv, c.getManaValue());
                    maxMv = Math.max(maxMv, c.getManaValue());
                }
            }
        }
        if (minMv == Integer.MAX_VALUE) {
            ability.setVariableCostsValue(0);
        } else if (minMv == maxMv) {
            ability.setVariableCostsValue(minMv);
        } else {
            ability.setVariableCostsMinMax(minMv, maxMv);
        }
    }
}

class CallerOfTheUntamedEffect extends OneShotEffect {

    CallerOfTheUntamedEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "Create a token that's a copy of a card you exiled with cards named {this}. X is the mana value of that card";
    }

    private CallerOfTheUntamedEffect(final CallerOfTheUntamedEffect effect) {
        super(effect);
    }

    @Override
    public CallerOfTheUntamedEffect copy() {
        return new CallerOfTheUntamedEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Object raw = game.getState().getValue(controller.getId() + "_caller_of_the_untamed_exile");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return true;
        }
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        CardsImpl matching = new CardsImpl();
        for (Object id : (Collection<?>) raw) {
            if (id instanceof UUID) {
                Card c = game.getCard((UUID) id);
                if (c != null && c.getManaValue() == xValue) {
                    matching.add(c);
                }
            }
        }
        if (matching.isEmpty()) {
            return true;
        }
        Card chosen = matching.size() == 1 ? matching.getRandom(game) : null;
        if (chosen == null) {
            TargetCard target = new TargetCard(Zone.EXILED, new FilterCard("exiled card with mana value " + xValue));
            target.withNotTarget(true);
            if (controller.choose(Outcome.PutCreatureInPlay, matching, target, source, game)) {
                chosen = matching.get(target.getFirstTarget(), game);
            }
        }
        if (chosen != null) {
            new CreateTokenCopyTargetEffect().setTargetPointer(new FixedTarget(chosen, game)).apply(game, source);
        }
        return true;
    }
}
