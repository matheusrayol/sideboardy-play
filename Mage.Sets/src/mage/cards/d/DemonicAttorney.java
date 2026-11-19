package mage.cards.d;

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
public final class DemonicAttorney extends CardImpl {

    public DemonicAttorney(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{B}{B}");

        // Remove Demonic Attorney from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // Each player antes the top card of their library.
        this.getSpellAbility().addEffect(new DemonicAttorneyEffect());
    }

    private DemonicAttorney(final DemonicAttorney card) {
        super(card);
    }

    @Override
    public DemonicAttorney copy() {
        return new DemonicAttorney(this);
    }
}

class DemonicAttorneyEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    DemonicAttorneyEffect() {
        super(Outcome.Detriment);
        staticText = "Each player antes the top card of their library";
    }

    private DemonicAttorneyEffect(final DemonicAttorneyEffect effect) {
        super(effect);
    }

    @Override
    public DemonicAttorneyEffect copy() {
        return new DemonicAttorneyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                Card top = player.getLibrary().getFromTop(game);
                if (top != null) {
                    player.moveCardsToExile(top, source, game, true, ANTE_EXILE_ID, "Ante");
                }
            }
        }
        return true;
    }
}
