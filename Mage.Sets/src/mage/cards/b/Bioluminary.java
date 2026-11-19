package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
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
public final class Bioluminary extends CardImpl {

    public Bioluminary(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Whenever Bioluminary deals combat damage to a player, you get {TK}{TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new DealsCombatDamageToAPlayerTriggeredAbility(
                new GetTicketCountersControllerEffect(2), false
        );
        ability.addEffect(new BioluminaryStickerEffect());
        this.addAbility(ability);
    }

    private Bioluminary(final Bioluminary card) {
        super(card);
    }

    @Override
    public Bioluminary copy() {
        return new Bioluminary(this);
    }
}

class BioluminaryStickerEffect extends OneShotEffect {

    BioluminaryStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private BioluminaryStickerEffect(final BioluminaryStickerEffect effect) {
        super(effect);
    }

    @Override
    public BioluminaryStickerEffect copy() {
        return new BioluminaryStickerEffect(this);
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
