package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetArtifactPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class TimmerianFiends extends CardImpl {

    public TimmerianFiends(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{B}");

        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Remove Timmerian Fiends from your deck before playing if you're not playing for ante.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Remove this card from your deck before playing if you're not playing for ante")
        ));

        // {B}{B}{B}, Sacrifice this creature: The owner of target artifact may ante the top card of their library. If that player doesn't, exchange ownership of that artifact and Timmerian Fiends. Put the artifact card into your graveyard and Timmerian Fiends from anywhere into that player's graveyard. This change in ownership is permanent.
        Ability ability = new SimpleActivatedAbility(new TimmerianFiendsEffect(), new ManaCostsImpl<>("{B}{B}{B}"));
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetArtifactPermanent());
        this.addAbility(ability);
    }

    private TimmerianFiends(final TimmerianFiends card) {
        super(card);
    }

    @Override
    public TimmerianFiends copy() {
        return new TimmerianFiends(this);
    }
}

class TimmerianFiendsEffect extends OneShotEffect {

    private static final UUID ANTE_EXILE_ID = UUID.fromString("00000000-0000-0000-0000-00000000a17e");

    TimmerianFiendsEffect() {
        super(Outcome.Detriment);
        staticText = "The owner of target artifact may ante the top card of their library. "
                + "If that player doesn't, exchange ownership of that artifact and {this}. "
                + "Put the artifact card into your graveyard and {this} from anywhere into that player's graveyard. "
                + "This change in ownership is permanent";
    }

    private TimmerianFiendsEffect(final TimmerianFiendsEffect effect) {
        super(effect);
    }

    @Override
    public TimmerianFiendsEffect copy() {
        return new TimmerianFiendsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetArtifact = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetArtifact == null) {
            return false;
        }
        Player owner = game.getPlayer(targetArtifact.getOwnerId());
        if (owner == null) {
            return false;
        }
        Card topCard = owner.getLibrary().getFromTop(game);
        if (topCard != null && owner.chooseUse(
                Outcome.Detriment,
                "Ante the top card of your library to prevent Timmerian Fiends?",
                source,
                game
        )) {
            owner.moveCardsToExile(topCard, source, game, true, ANTE_EXILE_ID, "Ante");
        } else {
            owner.moveCards(targetArtifact, Zone.GRAVEYARD, source, game);
        }
        return true;
    }
}
