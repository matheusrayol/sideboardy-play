package mage.cards.r;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.dynamicvalue.common.SourcePermanentPowerValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.AmassEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetSacrifice;

/**
 *
 * @author muz
 */
public final class RhovanionRampager extends CardImpl {

    public RhovanionRampager(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.WOLF);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Whenever this creature attacks, you may sacrifice another creature. If you do, put a number of +1/+1 counters on this creature equal to the sacrificed creature's power.
        this.addAbility(new AttacksTriggeredAbility(new RhovanionRampagerSacrificeEffect(), true));

        // When this creature dies, amass Goblins X, where X is this creature's power.
        this.addAbility(new DiesSourceTriggeredAbility(
                new AmassEffect(SourcePermanentPowerValue.NOT_NEGATIVE, SubType.GOBLIN)
        ));
    }

    private RhovanionRampager(final RhovanionRampager card) {
        super(card);
    }

    @Override
    public RhovanionRampager copy() {
        return new RhovanionRampager(this);
    }
}

class RhovanionRampagerSacrificeEffect extends OneShotEffect {

    RhovanionRampagerSacrificeEffect() {
        super(Outcome.BoostCreature);
        staticText = "sacrifice another creature. If you do, put a number of +1/+1 counters on {this} equal to the sacrificed creature's power";
    }

    private RhovanionRampagerSacrificeEffect(final RhovanionRampagerSacrificeEffect effect) {
        super(effect);
    }

    @Override
    public RhovanionRampagerSacrificeEffect copy() {
        return new RhovanionRampagerSacrificeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetSacrifice target = new TargetSacrifice(StaticFilters.FILTER_CONTROLLED_ANOTHER_CREATURE);
        if (!target.canChoose(controller.getId(), source, game)) {
            return false;
        }
        if (!target.choose(Outcome.Sacrifice, controller.getId(), source.getSourceId(), source, game)) {
            return false;
        }
        Permanent victim = game.getPermanent(target.getFirstTarget());
        if (victim == null) {
            return false;
        }
        int power = Math.max(0, victim.getPower().getValue());
        if (!victim.sacrifice(source, game)) {
            return false;
        }
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        if (sourcePermanent != null && power > 0) {
            sourcePermanent.addCounters(CounterType.P1P1.createInstance(power), source.getControllerId(), source, game);
        }
        return true;
    }
}
