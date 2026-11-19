package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.keyword.DeathtouchAbility;
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
public final class CarnivalCarnivore extends CardImpl {

    public CarnivalCarnivore(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{B}");

        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.CRAB);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // When Carnival Carnivore enters, you get {TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(1));
        ability.addEffect(new CarnivalCarnivoreStickerEffect());
        this.addAbility(ability);
    }

    private CarnivalCarnivore(final CarnivalCarnivore card) {
        super(card);
    }

    @Override
    public CarnivalCarnivore copy() {
        return new CarnivalCarnivore(this);
    }
}

class CarnivalCarnivoreStickerEffect extends OneShotEffect {

    CarnivalCarnivoreStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private CarnivalCarnivoreStickerEffect(final CarnivalCarnivoreStickerEffect effect) {
        super(effect);
    }

    @Override
    public CarnivalCarnivoreStickerEffect copy() {
        return new CarnivalCarnivoreStickerEffect(this);
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
