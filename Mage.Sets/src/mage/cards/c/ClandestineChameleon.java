package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
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
public final class ClandestineChameleon extends CardImpl {

    public ClandestineChameleon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // When Clandestine Chameleon enters, you get {TK}{TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(2));
        ability.addEffect(new ClandestineChameleonStickerEffect());
        this.addAbility(ability);

        // Clandestine Chameleon has all abilities of ability stickers on other permanents you own and cards in your graveyard.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Clandestine Chameleon has all abilities of ability stickers on other permanents you own and cards in your graveyard"
        )));
    }

    private ClandestineChameleon(final ClandestineChameleon card) {
        super(card);
    }

    @Override
    public ClandestineChameleon copy() {
        return new ClandestineChameleon(this);
    }
}

class ClandestineChameleonStickerEffect extends OneShotEffect {

    ClandestineChameleonStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private ClandestineChameleonStickerEffect(final ClandestineChameleonStickerEffect effect) {
        super(effect);
    }

    @Override
    public ClandestineChameleonStickerEffect copy() {
        return new ClandestineChameleonStickerEffect(this);
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
