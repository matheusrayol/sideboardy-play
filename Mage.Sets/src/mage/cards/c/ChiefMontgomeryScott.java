package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.counters.Counters;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ChiefMontgomeryScott extends CardImpl {

    static final DifferentlyNamedPermanentCount landCount =
            new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    public ChiefMontgomeryScott(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Strange new worlds — {6}, {T}: You may put a creature or Spacecraft card from your hand onto the battlefield.
        // It enters with X additional +1/+1 counters on it if it's a creature and X additional charge counters on it if it's a Spacecraft,
        // where X is the number of differently named lands you control.
        Ability ability = new SimpleActivatedAbility(new ChiefMontgomeryScottEffect(), new GenericManaCost(6));
        ability.addCost(new TapSourceCost());
        ability.setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS);
        ability.addHint(landCount.getHint());
        this.addAbility(ability);
    }

    private ChiefMontgomeryScott(final ChiefMontgomeryScott card) {
        super(card);
    }

    @Override
    public ChiefMontgomeryScott copy() {
        return new ChiefMontgomeryScott(this);
    }
}

class ChiefMontgomeryScottEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("a creature or Spacecraft card");

    static {
        filter.add(Predicates.or(
                CardType.CREATURE.getPredicate(),
                SubType.SPACECRAFT.getPredicate()
        ));
    }

    ChiefMontgomeryScottEffect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "you may put a creature or Spacecraft card from your hand onto the battlefield. "
                + "It enters with X additional +1/+1 counters on it if it's a creature and X additional charge counters on it "
                + "if it's a Spacecraft, where X is the number of differently named lands you control";
    }

    private ChiefMontgomeryScottEffect(final ChiefMontgomeryScottEffect effect) {
        super(effect);
    }

    @Override
    public ChiefMontgomeryScottEffect copy() {
        return new ChiefMontgomeryScottEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand(0, 1, filter);
        if (!controller.choose(Outcome.PutCardInPlay, target, source, game)) {
            return true;
        }
        Card card = game.getCard(target.getFirstTarget());
        if (card == null) {
            return true;
        }
        int x = ChiefMontgomeryScott.landCount.calculate(game, source, this);
        if (x > 0) {
            Counters counters = new Counters();
            if (card.isCreature(game)) {
                counters.addCounter(CounterType.P1P1.createInstance(x));
            }
            if (card.hasSubtype(SubType.SPACECRAFT, game)) {
                counters.addCounter(CounterType.CHARGE.createInstance(x));
            }
            if (!counters.isEmpty()) {
                game.setEnterWithCounters(card.getId(), counters);
            }
        }
        return controller.moveCards(card, Zone.BATTLEFIELD, source, game);
    }
}
