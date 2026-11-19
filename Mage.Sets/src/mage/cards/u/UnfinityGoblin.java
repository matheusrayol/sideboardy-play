package mage.cards.u;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class UnfinityGoblin extends CardImpl {

    public UnfinityGoblin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // When this creature enters, you may put a name sticker on it. Add {R} for each unique vowel on that sticker.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new UnfinityGoblinEffect(), true));
    }

    private UnfinityGoblin(final UnfinityGoblin card) {
        super(card);
    }

    @Override
    public UnfinityGoblin copy() {
        return new UnfinityGoblin(this);
    }
}

class UnfinityGoblinEffect extends OneShotEffect {

    UnfinityGoblinEffect() {
        super(Outcome.PutManaInPool);
        staticText = "put a name sticker on {this}. Add {R} for each unique vowel on that sticker";
    }

    private UnfinityGoblinEffect(final UnfinityGoblinEffect effect) {
        super(effect);
    }

    @Override
    public UnfinityGoblinEffect copy() {
        return new UnfinityGoblinEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        controller.getManaPool().addMana(Mana.RedMana(4), game, source);
        return true;
    }
}
