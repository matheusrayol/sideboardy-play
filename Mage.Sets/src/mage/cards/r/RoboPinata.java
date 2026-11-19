package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
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
public final class RoboPinata extends CardImpl {

    public RoboPinata(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{W}");

        this.subtype.add(SubType.CLOWN);
        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // When Robo-Piñata dies, choose one —
        // • You get {TK}{TK}.
        // • You may put a sticker on a nonland permanent you own.
        Ability ability = new DiesSourceTriggeredAbility(new GetTicketCountersControllerEffect(2));
        ability.addMode(new Mode(new RoboPinataStickerEffect()));
        this.addAbility(ability);
    }

    private RoboPinata(final RoboPinata card) {
        super(card);
    }

    @Override
    public RoboPinata copy() {
        return new RoboPinata(this);
    }
}

class RoboPinataStickerEffect extends OneShotEffect {

    RoboPinataStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a sticker on a nonland permanent you own";
    }

    private RoboPinataStickerEffect(final RoboPinataStickerEffect effect) {
        super(effect);
    }

    @Override
    public RoboPinataStickerEffect copy() {
        return new RoboPinataStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
