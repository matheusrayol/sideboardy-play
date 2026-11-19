package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.RollDiceEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TokenImpl;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CommandPerformance extends CardImpl {

    public CommandPerformance(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{U}");

        // Choose two —
        // • Open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)
        // • Roll to visit your Attractions.
        // • You get {TK}{TK}.
        // • You may put a sticker on a nonland permanent you own.
        this.getSpellAbility().getModes().setMinModes(2);
        this.getSpellAbility().getModes().setMaxModes(2);
        this.getSpellAbility().addEffect(
                new CreateTokenEffect(new CommandPerformanceAttractionToken())
                        .setText("Open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        );
        this.getSpellAbility().addMode(new Mode(new RollDiceEffect(6).setText("Roll to visit your Attractions")));
        this.getSpellAbility().addMode(new Mode(new GetTicketCountersControllerEffect(2)));
        this.getSpellAbility().addMode(new Mode(new CommandPerformanceStickerEffect()));
    }

    private CommandPerformance(final CommandPerformance card) {
        super(card);
    }

    @Override
    public CommandPerformance copy() {
        return new CommandPerformance(this);
    }
}

class CommandPerformanceAttractionToken extends TokenImpl {

    CommandPerformanceAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private CommandPerformanceAttractionToken(final CommandPerformanceAttractionToken token) {
        super(token);
    }

    @Override
    public CommandPerformanceAttractionToken copy() {
        return new CommandPerformanceAttractionToken(this);
    }
}

class CommandPerformanceStickerEffect extends OneShotEffect {

    CommandPerformanceStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "You may put a sticker on a nonland permanent you own";
    }

    private CommandPerformanceStickerEffect(final CommandPerformanceStickerEffect effect) {
        super(effect);
    }

    @Override
    public CommandPerformanceStickerEffect copy() {
        return new CommandPerformanceStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
