package mage.cards.j;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class JeweledBird extends CardImpl {

    public JeweledBird(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        // Remove Jeweled Bird from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // {T}: Ante this artifact. If you do, put all other cards you own from the ante into your graveyard, then draw a card.
        this.addAbility(new SimpleActivatedAbility(new JeweledBirdEffect(), new TapSourceCost()));
    }

    private JeweledBird(final JeweledBird card) {
        super(card);
    }

    @Override
    public JeweledBird copy() {
        return new JeweledBird(this);
    }
}

class JeweledBirdEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    JeweledBirdEffect() {
        super(Outcome.DrawCard);
        staticText = "Ante {this}. If you do, put all other cards you own from the ante into your graveyard, then draw a card";
    }

    private JeweledBirdEffect(final JeweledBirdEffect effect) {
        super(effect);
    }

    @Override
    public JeweledBirdEffect copy() {
        return new JeweledBirdEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent sourcePerm = source.getSourcePermanentIfItStillExists(game);
        if (sourcePerm != null && controller.moveCardsToExile(sourcePerm, source, game, true, ANTE_EXILE_ID, "Ante")) {
            ExileZone anteZone = game.getExile().getExileZone(ANTE_EXILE_ID);
            if (anteZone != null) {
                Cards toGraveyard = new CardsImpl();
                for (Card c : anteZone.getCards(game)) {
                    if (c.isOwnedBy(controller.getId()) && !c.getId().equals(source.getSourceId())) {
                        toGraveyard.add(c);
                    }
                }
                if (!toGraveyard.isEmpty()) {
                    controller.moveCards(toGraveyard, Zone.GRAVEYARD, source, game);
                }
            }
            controller.drawCards(1, source, game);
        }
        return true;
    }
}
