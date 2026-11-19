package mage.cards.g;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

/**
 *
 * @author muz
 */
public final class GowronEagerForBattle extends CardImpl {

    public GowronEagerForBattle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever another creature dies, put a +1/+1 counter on Gowron.
        this.addAbility(new DiesCreatureTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                false,
                true
        ));

        // Sacrifice another creature: Gowron gains trample until end of turn. If you sacrificed a Klingon this way, you draw a card and lose 1 life.
        this.addAbility(new SimpleActivatedAbility(
                new GowronEagerForBattleEffect(),
                new SacrificeTargetCost(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE)
        ));
    }

    private GowronEagerForBattle(final GowronEagerForBattle card) {
        super(card);
    }

    @Override
    public GowronEagerForBattle copy() {
        return new GowronEagerForBattle(this);
    }
}

class GowronEagerForBattleEffect extends OneShotEffect {

    GowronEagerForBattleEffect() {
        super(Outcome.AddAbility);
        staticText = "{this} gains trample until end of turn. If you sacrificed a Klingon this way, you draw a card and lose 1 life";
    }

    private GowronEagerForBattleEffect(final GowronEagerForBattleEffect effect) {
        super(effect);
    }

    @Override
    public GowronEagerForBattleEffect copy() {
        return new GowronEagerForBattleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.addEffect(new GainAbilitySourceEffect(TrampleAbility.getInstance(), Duration.EndOfTurn), source);
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return true;
        }
        boolean sacrificedKlingon = false;
        for (Cost cost : source.getCosts()) {
            if (cost instanceof SacrificeTargetCost) {
                for (Permanent sacrificed : ((SacrificeTargetCost) cost).getPermanents()) {
                    if (sacrificed != null && sacrificed.hasSubtype(SubType.KLINGON, game)) {
                        sacrificedKlingon = true;
                        break;
                    }
                }
            }
        }
        if (sacrificedKlingon) {
            controller.drawCards(1, source, game);
            controller.loseLife(1, game, source, false);
        }
        return true;
    }
}
