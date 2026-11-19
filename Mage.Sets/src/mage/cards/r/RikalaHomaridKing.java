package mage.cards.r;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.TapForManaAllTriggeredManaAbility;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.mana.BasicManaEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class RikalaHomaridKing extends CardImpl {

    private static final FilterControlledPermanent filterIsland = new FilterControlledPermanent(SubType.ISLAND, "an Island you control");

    public RikalaHomaridKing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.LOBSTER);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // At the beginning of your upkeep, put a tide counter on Rikala. Then if there are four or more tide counters on Rikala, remove them.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(new RikalaHomaridKingEffect()));

        // Creatures you control get +1/+0 for each tide counter on Rikala.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                new CountersSourceCount(CounterType.TIDE),
                StaticValue.get(0),
                Duration.WhileOnBattlefield
        )));

        // Whenever you tap an Island for mana, if there are three or more tide counters on Rikala, add an additional {U}.
        this.addAbility(new TapForManaAllTriggeredManaAbility(
                new BasicManaEffect(Mana.BlueMana(1)),
                filterIsland,
                SetTargetPointer.NONE
        ).withInterveningIf(new SourceHasCounterCondition(CounterType.TIDE, ComparisonType.MORE_THAN, 2)));
    }

    private RikalaHomaridKing(final RikalaHomaridKing card) {
        super(card);
    }

    @Override
    public RikalaHomaridKing copy() {
        return new RikalaHomaridKing(this);
    }
}

class RikalaHomaridKingEffect extends OneShotEffect {

    RikalaHomaridKingEffect() {
        super(Outcome.Benefit);
        this.staticText = "put a tide counter on {this}. Then if there are four or more tide counters on {this}, remove them";
    }

    private RikalaHomaridKingEffect(final RikalaHomaridKingEffect effect) {
        super(effect);
    }

    @Override
    public RikalaHomaridKingEffect copy() {
        return new RikalaHomaridKingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        if (sourcePermanent == null) {
            return false;
        }
        sourcePermanent.addCounters(CounterType.TIDE.createInstance(), source.getControllerId(), source, game);
        int count = sourcePermanent.getCounters(game).getCount(CounterType.TIDE);
        if (count >= 4) {
            sourcePermanent.removeCounters(CounterType.TIDE.getName(), count, source, game);
        }
        return true;
    }
}
