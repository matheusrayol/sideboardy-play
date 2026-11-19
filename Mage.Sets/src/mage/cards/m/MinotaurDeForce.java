package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.keyword.HasteAbility;
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
public final class MinotaurDeForce extends CardImpl {

    public MinotaurDeForce(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.MINOTAUR);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // When this creature enters, you get {TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(1));
        ability.addEffect(new MinotaurDeForceStickerEffect());
        this.addAbility(ability);
    }

    private MinotaurDeForce(final MinotaurDeForce card) {
        super(card);
    }

    @Override
    public MinotaurDeForce copy() {
        return new MinotaurDeForce(this);
    }
}

class MinotaurDeForceStickerEffect extends OneShotEffect {

    MinotaurDeForceStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private MinotaurDeForceStickerEffect(final MinotaurDeForceStickerEffect effect) {
        super(effect);
    }

    @Override
    public MinotaurDeForceStickerEffect copy() {
        return new MinotaurDeForceStickerEffect(this);
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
