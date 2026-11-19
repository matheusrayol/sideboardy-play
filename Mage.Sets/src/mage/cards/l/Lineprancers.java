package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.MustBeBlockedByTargetSourceEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Lineprancers extends CardImpl {

    public Lineprancers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.CENTAUR);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // When Lineprancers enters, you get {TK}{TK}, then you may put a power and toughness sticker on a creature you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(2));
        ability.addEffect(new LineprancersStickerEffect());
        this.addAbility(ability);

        // {3}{G}: Target creature you don't control blocks target creature you control with a power and toughness sticker on it other than Lineprancers this turn if able.
        Ability a2 = new SimpleActivatedAbility(
                new MustBeBlockedByTargetSourceEffect(Duration.EndOfTurn)
                        .setText("Target creature you don't control blocks target creature you control with a power and toughness sticker on it other than {this} this turn if able"),
                new ManaCostsImpl<>("{3}{G}")
        );
        a2.addTarget(new TargetPermanent(StaticFilters.FILTER_CREATURE_YOU_DONT_CONTROL));
        this.addAbility(a2);
    }

    private Lineprancers(final Lineprancers card) {
        super(card);
    }

    @Override
    public Lineprancers copy() {
        return new Lineprancers(this);
    }
}

class LineprancersStickerEffect extends OneShotEffect {

    LineprancersStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a power and toughness sticker on a creature you own";
    }

    private LineprancersStickerEffect(final LineprancersStickerEffect effect) {
        super(effect);
    }

    @Override
    public LineprancersStickerEffect copy() {
        return new LineprancersStickerEffect(this);
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
            if (permanent != null) {
                permanent.addCounters(new Counter("p/t sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
