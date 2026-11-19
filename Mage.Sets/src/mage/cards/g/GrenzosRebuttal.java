package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.players.Player;
import mage.players.PlayerList;
import mage.target.TargetPermanent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class GrenzosRebuttal extends CardImpl {

    public GrenzosRebuttal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{R}{R}");

        // Create a 4/4 red Ogre creature token.
        this.getSpellAbility().addEffect(new CreateTokenEffect(new GrenzosRebuttalOgreToken()));

        // Starting with you, each player chooses an artifact, a creature, and a land from among the permanents controlled by the player to their left. Destroy each permanent chosen this way.
        this.getSpellAbility().addEffect(new GrenzosRebuttalEffect());
    }

    private GrenzosRebuttal(final GrenzosRebuttal card) {
        super(card);
    }

    @Override
    public GrenzosRebuttal copy() {
        return new GrenzosRebuttal(this);
    }
}

class GrenzosRebuttalOgreToken extends TokenImpl {

    GrenzosRebuttalOgreToken() {
        super("Ogre Token", "4/4 red Ogre creature token");
        cardType.add(CardType.CREATURE);
        color.setRed(true);
        subtype.add(SubType.OGRE);
        power = new MageInt(4);
        toughness = new MageInt(4);
    }

    private GrenzosRebuttalOgreToken(final GrenzosRebuttalOgreToken token) {
        super(token);
    }

    @Override
    public GrenzosRebuttalOgreToken copy() {
        return new GrenzosRebuttalOgreToken(this);
    }
}

class GrenzosRebuttalEffect extends OneShotEffect {

    GrenzosRebuttalEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "Starting with you, each player chooses an artifact, a creature, and a land from among the permanents "
                + "controlled by the player to their left. Destroy each permanent chosen this way";
    }

    private GrenzosRebuttalEffect(final GrenzosRebuttalEffect effect) {
        super(effect);
    }

    @Override
    public GrenzosRebuttalEffect copy() {
        return new GrenzosRebuttalEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<UUID> chosenIds = new LinkedHashSet<>();
        PlayerList players = game.getState().getPlayersInRange(source.getControllerId(), game);
        for (UUID chooserId : players) {
            Player chooser = game.getPlayer(chooserId);
            if (chooser == null || !chooser.isInGame()) {
                continue;
            }
            PlayerList fromChooser = game.getState().getPlayersInRange(chooserId, game);
            UUID leftPlayerId = fromChooser.getNext();
            if (leftPlayerId == null || leftPlayerId.equals(chooserId)) {
                continue;
            }
            chooseOneOfType(chooser, leftPlayerId, new FilterArtifactPermanent("artifact controlled by player to your left"), chosenIds, source, game);
            chooseOneOfType(chooser, leftPlayerId, new FilterCreaturePermanent("creature controlled by player to your left"), chosenIds, source, game);
            chooseOneOfType(chooser, leftPlayerId, new FilterLandPermanent("land controlled by player to your left"), chosenIds, source, game);
        }
        for (UUID permanentId : chosenIds) {
            Permanent permanent = game.getPermanent(permanentId);
            if (permanent != null) {
                permanent.destroy(source, game);
            }
        }
        return true;
    }

    private static void chooseOneOfType(
            Player chooser,
            UUID ownerControllerId,
            FilterPermanent baseFilter,
            Set<UUID> chosenIds,
            Ability source,
            Game game
    ) {
        FilterPermanent filter = baseFilter.copy();
        filter.add(new ControllerIdPredicate(ownerControllerId));
        TargetPermanent target = new TargetPermanent(1, 1, filter, true);
        if (target.canChoose(chooser.getId(), source, game)
                && chooser.choose(Outcome.DestroyPermanent, target, source, game)) {
            UUID selected = target.getFirstTarget();
            if (selected != null) {
                chosenIds.add(selected);
            }
        }
    }
}
