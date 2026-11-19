package mage.cards.u;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrDiesSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class USSEnterpriseCTimeTossed extends CardImpl {

    public USSEnterpriseCTimeTossed(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // When U.S.S. Enterprise-C enters or dies, you may discard your hand. If you do, draw a card for each card discarded this way.
        this.addAbility(new EntersBattlefieldOrDiesSourceTriggeredAbility(new USSEnterpriseCEffect(), false));

        // Station
        this.addAbility(new StationAbility());

        // STATION 3+
        // Flying
        // 6/6
        this.addAbility(new StationLevelAbility(3)
                .withLevelAbility(FlyingAbility.getInstance())
                .withPT(6, 6));
    }

    private USSEnterpriseCTimeTossed(final USSEnterpriseCTimeTossed card) {
        super(card);
    }

    @Override
    public USSEnterpriseCTimeTossed copy() {
        return new USSEnterpriseCTimeTossed(this);
    }
}

class USSEnterpriseCEffect extends OneShotEffect {

    USSEnterpriseCEffect() {
        super(Outcome.DrawCard);
        this.staticText = "you may discard your hand. If you do, draw a card for each card discarded this way";
    }

    private USSEnterpriseCEffect(final USSEnterpriseCEffect effect) {
        super(effect);
    }

    @Override
    public USSEnterpriseCEffect copy() {
        return new USSEnterpriseCEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (!controller.chooseUse(
                Outcome.DrawCard,
                "Discard your hand and draw a card for each card discarded this way?",
                source,
                game
        )) {
            return true;
        }
        int handSize = controller.getHand().size();
        int discardedCount = controller.discard(handSize, false, false, source, game).size();
        if (discardedCount > 0) {
            controller.drawCards(discardedCount, source, game);
        }
        return true;
    }
}
