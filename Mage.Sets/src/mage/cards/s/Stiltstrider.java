package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.keyword.ReachAbility;
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
public final class Stiltstrider extends CardImpl {

    public Stiltstrider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{G}");

        this.subtype.add(SubType.RHINO);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // When Stiltstrider enters, you get {TK}{TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(2));
        ability.addEffect(new StiltstriderStickerEffect());
        this.addAbility(ability);
    }

    private Stiltstrider(final Stiltstrider card) {
        super(card);
    }

    @Override
    public Stiltstrider copy() {
        return new Stiltstrider(this);
    }
}

class StiltstriderStickerEffect extends OneShotEffect {

    StiltstriderStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private StiltstriderStickerEffect(final StiltstriderStickerEffect effect) {
        super(effect);
    }

    @Override
    public StiltstriderStickerEffect copy() {
        return new StiltstriderStickerEffect(this);
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
