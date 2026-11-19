package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ContractFromBelow extends CardImpl {

    public ContractFromBelow(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{B}");

        // Remove Contract from Below from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // Discard your hand, ante the top card of your library, then draw seven cards.
        this.getSpellAbility().addEffect(new ContractFromBelowEffect());
    }

    private ContractFromBelow(final ContractFromBelow card) {
        super(card);
    }

    @Override
    public ContractFromBelow copy() {
        return new ContractFromBelow(this);
    }
}

class ContractFromBelowEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    ContractFromBelowEffect() {
        super(Outcome.DrawCard);
        staticText = "Discard your hand, ante the top card of your library, then draw seven cards";
    }

    private ContractFromBelowEffect(final ContractFromBelowEffect effect) {
        super(effect);
    }

    @Override
    public ContractFromBelowEffect copy() {
        return new ContractFromBelowEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        controller.discard(controller.getHand(), false, source, game);
        Card topCard = controller.getLibrary().getFromTop(game);
        if (topCard != null) {
            controller.moveCardsToExile(topCard, source, game, true, ANTE_EXILE_ID, "Ante");
        }
        controller.drawCards(7, source, game);
        return true;
    }
}
