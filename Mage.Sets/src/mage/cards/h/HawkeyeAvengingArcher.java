package mage.cards.h;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealtDamageAndDiedTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.common.TargetAnyTarget;

/**
 *
 * @author muz
 */
public final class HawkeyeAvengingArcher extends CardImpl {

    public HawkeyeAvengingArcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARCHER);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // Whenever a creature an opponent controls dies, if Hawkeye dealt damage to it this turn, draw a card.
        this.addAbility(new HawkeyeAvengingArcherTriggeredAbility());

        // {T}: Hawkeye deals 1 damage to any target.
        Ability ability = new SimpleActivatedAbility(new DamageTargetEffect(1), new TapSourceCost());
        ability.addTarget(new TargetAnyTarget());
        this.addAbility(ability);
    }

    private HawkeyeAvengingArcher(final HawkeyeAvengingArcher card) {
        super(card);
    }

    @Override
    public HawkeyeAvengingArcher copy() {
        return new HawkeyeAvengingArcher(this);
    }
}

class HawkeyeAvengingArcherTriggeredAbility extends DealtDamageAndDiedTriggeredAbility {

    HawkeyeAvengingArcherTriggeredAbility() {
        super(new DrawCardSourceControllerEffect(1), false, StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE);
    }

    private HawkeyeAvengingArcherTriggeredAbility(final HawkeyeAvengingArcherTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return super.checkTrigger(event, game)
                && game.getOpponents(getControllerId()).contains(((ZoneChangeEvent) event).getTarget().getControllerId());
    }

    @Override
    public HawkeyeAvengingArcherTriggeredAbility copy() {
        return new HawkeyeAvengingArcherTriggeredAbility(this);
    }

    @Override
    public String getRule() {
        return "Whenever a creature an opponent controls dies, if {this} dealt damage to it this turn, draw a card.";
    }
}
