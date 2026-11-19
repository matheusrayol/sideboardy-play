package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.LookLibraryAndPickControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.PutCards;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class WizardsOfThe extends CardImpl {

    public WizardsOfThe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // When this creature enters, you may put a name sticker on it, then look at the top X cards of your library, where X is the number of unique vowels on that sticker. Put one of those cards into your hand and the rest on the bottom of your library in any order.
        Ability ability = new EntersBattlefieldTriggeredAbility(new WizardsOfTheStickerEffect(), false);
        ability.addEffect(new LookLibraryAndPickControllerEffect(
                3, 1, PutCards.HAND, PutCards.BOTTOM_ANY
        ).setText("then look at the top X cards of your library, where X is the number of unique vowels on that sticker. "
                + "Put one of those cards into your hand and the rest on the bottom of your library in any order"));
        this.addAbility(ability);
    }

    private WizardsOfThe(final WizardsOfThe card) {
        super(card);
    }

    @Override
    public WizardsOfThe copy() {
        return new WizardsOfThe(this);
    }
}

class WizardsOfTheStickerEffect extends OneShotEffect {

    WizardsOfTheStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "you may put a name sticker on {this}";
    }

    private WizardsOfTheStickerEffect(final WizardsOfTheStickerEffect effect) {
        super(effect);
    }

    @Override
    public WizardsOfTheStickerEffect copy() {
        return new WizardsOfTheStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        return true;
    }
}
