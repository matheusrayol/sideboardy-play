package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldControlledTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ParkBleater extends CardImpl {

    public ParkBleater(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.subtype.add(SubType.GOAT);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever another creature you own enters, you get {TK}.
        this.addAbility(new EntersBattlefieldControlledTriggeredAbility(
                Zone.BATTLEFIELD,
                new GetTicketCountersControllerEffect(1),
                StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE,
                false
        ));

        // {W}, {T}: You may put a sticker on a creature you own that entered this turn.
        Ability ability = new SimpleActivatedAbility(new ParkBleaterEffect(), new ManaCostsImpl<>("{W}"));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private ParkBleater(final ParkBleater card) {
        super(card);
    }

    @Override
    public ParkBleater copy() {
        return new ParkBleater(this);
    }
}

class ParkBleaterEffect extends OneShotEffect {

    ParkBleaterEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a sticker on a creature you own that entered this turn";
    }

    private ParkBleaterEffect(final ParkBleaterEffect effect) {
        super(effect);
    }

    @Override
    public ParkBleaterEffect copy() {
        return new ParkBleaterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_CREATURE, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null && permanent.getTurnsOnBattlefield() == 0) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
