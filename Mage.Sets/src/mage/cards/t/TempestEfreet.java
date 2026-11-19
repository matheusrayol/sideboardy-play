package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetOpponent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TempestEfreet extends CardImpl {

    public TempestEfreet(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}{R}{R}");

        this.subtype.add(SubType.EFREET);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Remove Tempest Efreet from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // {T}, Sacrifice Tempest Efreet: Target opponent may pay 10 life. If that player doesn't, they reveal a card at random from their hand. Exchange ownership of the revealed card and Tempest Efreet. Put the revealed card into your hand and Tempest Efreet from anywhere into that player's graveyard. This change in ownership is permanent.
        Ability ability = new SimpleActivatedAbility(new TempestEfreetEffect(), new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);
    }

    private TempestEfreet(final TempestEfreet card) {
        super(card);
    }

    @Override
    public TempestEfreet copy() {
        return new TempestEfreet(this);
    }
}

class TempestEfreetEffect extends OneShotEffect {

    TempestEfreetEffect() {
        super(Outcome.GainLife);
        staticText = "Target opponent may pay 10 life. If that player doesn't, they reveal a card at random from their hand. "
                + "Exchange ownership of the revealed card and {this}. Put the revealed card into your hand "
                + "and {this} from anywhere into that player's graveyard. This change in ownership is permanent.";
    }

    private TempestEfreetEffect(final TempestEfreetEffect effect) {
        super(effect);
    }

    @Override
    public TempestEfreetEffect copy() {
        return new TempestEfreetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player opponent = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || opponent == null) {
            return false;
        }
        Cost payLifeCost = new PayLifeCost(10);
        if (payLifeCost.canPay(source, source, opponent.getId(), game)
                && opponent.chooseUse(Outcome.Detriment, "Pay 10 life to prevent exchanging ownership of a card in your hand and Tempest Efreet?", source, game)
                && payLifeCost.pay(source, game, source, opponent.getId(), false, null)) {
            return true;
        }
        Card revealedCard = opponent.getHand().getRandom(game);
        if (revealedCard == null) {
            return true;
        }
        opponent.revealCards(source, new CardsImpl(revealedCard), game);
        Card efreetCard = game.getCard(source.getSourceId());
        if (efreetCard == null) {
            return true;
        }
        revealedCard.setOwnerId(controller.getId());
        efreetCard.setOwnerId(opponent.getId());
        controller.moveCards(revealedCard, Zone.HAND, source, game);
        opponent.moveCards(efreetCard, Zone.GRAVEYARD, source, game);
        return true;
    }
}
