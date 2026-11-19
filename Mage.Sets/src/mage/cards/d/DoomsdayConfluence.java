package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.SacrificeAllEffect;
import mage.abilities.effects.common.discard.DiscardEachPlayerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.token.DalekToken;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;

/**
 * @author Sideboardy
 */
public final class DoomsdayConfluence extends CardImpl {

    public DoomsdayConfluence(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{X}{X}{B}");

        // Choose X. You may choose the same mode more than once.
        // • Each player sacrifices a nonartifact creature of their choice.
        // • Create a 3/3 black Dalek artifact creature token with menace.
        // • Each opponent discards a card.
        this.getSpellAbility().addEffect(new DoomsdayConfluenceEffect());
    }

    private DoomsdayConfluence(final DoomsdayConfluence card) {
        super(card);
    }

    @Override
    public DoomsdayConfluence copy() {
        return new DoomsdayConfluence(this);
    }
}

class DoomsdayConfluenceEffect extends OneShotEffect {

    private static final String MODE_SACRIFICE = "Each player sacrifices a nonartifact creature of their choice";
    private static final String MODE_DALEK = "Create a 3/3 black Dalek artifact creature token with menace";
    private static final String MODE_DISCARD = "Each opponent discards a card";

    private static final FilterPermanent nonArtifactCreatureFilter
            = new FilterCreaturePermanent("nonartifact creature");

    static {
        nonArtifactCreatureFilter.add(Predicates.not(CardType.ARTIFACT.getPredicate()));
    }

    DoomsdayConfluenceEffect() {
        super(Outcome.Benefit);
        this.staticText = "Choose X. You may choose the same mode more than once.<br>"
                + "&bull; Each player sacrifices a nonartifact creature of their choice.<br>"
                + "&bull; Create a 3/3 black Dalek artifact creature token with menace.<br>"
                + "&bull; Each opponent discards a card.";
    }

    private DoomsdayConfluenceEffect(final DoomsdayConfluenceEffect effect) {
        super(effect);
    }

    @Override
    public DoomsdayConfluenceEffect copy() {
        return new DoomsdayConfluenceEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        if (xValue <= 0) {
            return true;
        }

        Set<String> modes = new LinkedHashSet<>();
        modes.add(MODE_SACRIFICE);
        modes.add(MODE_DALEK);
        modes.add(MODE_DISCARD);

        List<String> chosenModes = new ArrayList<>(xValue);
        for (int i = 0; i < xValue; i++) {
            if (!controller.canRespond()) {
                return false;
            }
            Choice choice = new ChoiceImpl(true);
            choice.setMessage("Choose mode (" + (i + 1) + " of " + xValue + ")");
            choice.setChoices(modes);
            if (!controller.choose(Outcome.Benefit, choice, game)) {
                return false;
            }
            chosenModes.add(choice.getChoice());
        }

        for (String mode : chosenModes) {
            switch (mode) {
                case MODE_SACRIFICE:
                    new SacrificeAllEffect(1, nonArtifactCreatureFilter).apply(game, source);
                    break;
                case MODE_DALEK:
                    new CreateTokenEffect(new DalekToken()).apply(game, source);
                    break;
                case MODE_DISCARD:
                    new DiscardEachPlayerEffect(TargetController.OPPONENT).apply(game, source);
                    break;
                default:
                    break;
            }
        }
        return true;
    }
}
