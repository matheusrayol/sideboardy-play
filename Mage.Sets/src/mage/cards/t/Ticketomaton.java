package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
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
public final class Ticketomaton extends CardImpl {

    public Ticketomaton(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.subtype.add(SubType.ROBOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // When Ticketomaton enters, you get {TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(1));
        ability.addEffect(new TicketomatonStickerEffect());
        this.addAbility(ability);
    }

    private Ticketomaton(final Ticketomaton card) {
        super(card);
    }

    @Override
    public Ticketomaton copy() {
        return new Ticketomaton(this);
    }
}

class TicketomatonStickerEffect extends OneShotEffect {

    TicketomatonStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private TicketomatonStickerEffect(final TicketomatonStickerEffect effect) {
        super(effect);
    }

    @Override
    public TicketomatonStickerEffect copy() {
        return new TicketomatonStickerEffect(this);
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
