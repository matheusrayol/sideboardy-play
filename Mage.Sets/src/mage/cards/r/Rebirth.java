package mage.cards.r;

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
public final class Rebirth extends CardImpl {

    public Rebirth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{G}{G}{G}");

        // Remove Rebirth from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // Each player may ante the top card of their library. If a player does, that player's life total becomes 20.
        this.getSpellAbility().addEffect(new RebirthEffect());
    }

    private Rebirth(final Rebirth card) {
        super(card);
    }

    @Override
    public Rebirth copy() {
        return new Rebirth(this);
    }
}

class RebirthEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    RebirthEffect() {
        super(Outcome.GainLife);
        staticText = "Each player may ante the top card of their library. If a player does, that player's life total becomes 20";
    }

    private RebirthEffect(final RebirthEffect effect) {
        super(effect);
    }

    @Override
    public RebirthEffect copy() {
        return new RebirthEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            Card topCard = player.getLibrary().getFromTop(game);
            if (topCard != null && player.chooseUse(
                    Outcome.GainLife,
                    "Ante the top card of your library? (If you do, your life total becomes 20.)",
                    source,
                    game
            )) {
                player.moveCardsToExile(topCard, source, game, true, ANTE_EXILE_ID, "Ante");
                player.setLife(20, game, source);
            }
        }
        return true;
    }
}
