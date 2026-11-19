package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Darkpact extends CardImpl {

    public Darkpact(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{B}{B}{B}");

        // Remove Darkpact from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // You own target card in the ante. Exchange that card with the top card of your library.
        this.getSpellAbility().addEffect(new DarkpactEffect());
    }

    private Darkpact(final Darkpact card) {
        super(card);
    }

    @Override
    public Darkpact copy() {
        return new Darkpact(this);
    }
}

class DarkpactEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    DarkpactEffect() {
        super(Outcome.Benefit);
        staticText = "You own target card in the ante. Exchange that card with the top card of your library";
    }

    private DarkpactEffect(final DarkpactEffect effect) {
        super(effect);
    }

    @Override
    public DarkpactEffect copy() {
        return new DarkpactEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        ExileZone anteZone = game.getExile().getExileZone(ANTE_EXILE_ID);
        if (anteZone != null && !anteZone.isEmpty()) {
            Card anteCard = anteZone.getCards(game).iterator().next();
            Card topCard = controller.getLibrary().getFromTop(game);
            if (topCard != null) {
                controller.moveCardsToExile(topCard, source, game, true, ANTE_EXILE_ID, "Ante");
            }
            controller.putCardsOnTopOfLibrary(new CardsImpl(anteCard), game, source, false);
        }
        return true;
    }
}
