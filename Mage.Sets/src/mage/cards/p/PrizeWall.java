package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
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
public final class PrizeWall extends CardImpl {

    public PrizeWall(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.WALL);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // {U}, {T}: You get {TK}.
        Ability a1 = new SimpleActivatedAbility(new GetTicketCountersControllerEffect(1), new ManaCostsImpl<>("{U}"));
        a1.addCost(new TapSourceCost());
        this.addAbility(a1);

        // {4}{U}, {T}: You may put a sticker on a nonland permanent you own. Activate only as a sorcery.
        Ability a2 = new ActivateAsSorceryActivatedAbility(
                Zone.BATTLEFIELD, new PrizeWallStickerEffect(), new ManaCostsImpl<>("{4}{U}")
        );
        a2.addCost(new TapSourceCost());
        this.addAbility(a2);
    }

    private PrizeWall(final PrizeWall card) {
        super(card);
    }

    @Override
    public PrizeWall copy() {
        return new PrizeWall(this);
    }
}

class PrizeWallStickerEffect extends OneShotEffect {

    PrizeWallStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a sticker on a nonland permanent you own";
    }

    private PrizeWallStickerEffect(final PrizeWallStickerEffect effect) {
        super(effect);
    }

    @Override
    public PrizeWallStickerEffect copy() {
        return new PrizeWallStickerEffect(this);
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
