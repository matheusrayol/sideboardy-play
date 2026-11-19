package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.RollDiceEvent;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BamboozlingBeeble extends CardImpl {

    public BamboozlingBeeble(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}");

        this.subtype.add(SubType.BEEBLE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Protection from Robots
        this.addAbility(new ProtectionAbility(new FilterPermanent(SubType.ROBOT, "Robots")));

        // {1}, {T}: The next time target player would roll one or more dice this turn, instead they roll that many dice plus one and you choose one of those rolls to ignore.
        Ability ability = new SimpleActivatedAbility(new BamboozlingBeebleEffect(), new ManaCostsImpl<>("{1}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);
    }

    private BamboozlingBeeble(final BamboozlingBeeble card) {
        super(card);
    }

    @Override
    public BamboozlingBeeble copy() {
        return new BamboozlingBeeble(this);
    }
}

class BamboozlingBeebleEffect extends ReplacementEffectImpl {

    BamboozlingBeebleEffect() {
        super(Duration.EndOfTurn, Outcome.Benefit);
        staticText = "The next time target player would roll one or more dice this turn, "
                + "instead they roll that many dice plus one and you choose one of those rolls to ignore";
    }

    private BamboozlingBeebleEffect(final BamboozlingBeebleEffect effect) {
        super(effect);
    }

    @Override
    public BamboozlingBeebleEffect copy() {
        return new BamboozlingBeebleEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ROLL_DICE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return ((RollDiceEvent) event).getRollDieType() == RollDieType.NUMERICAL
                && event.getTargetId().equals(getTargetPointer().getFirst(game, source));
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        RollDiceEvent rdEvent = (RollDiceEvent) event;
        rdEvent.incAmount(1);
        rdEvent.incIgnoreLowestAmount(1);
        discard();
        return false;
    }
}
