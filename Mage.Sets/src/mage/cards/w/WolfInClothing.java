package mage.cards.w;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class WolfInClothing extends CardImpl {

    public WolfInClothing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.WOLF);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // When this creature enters, you may put a name sticker on it. When you do, up to X target creatures each get -1/-1 until end of turn, where X is the number of unique vowels on that sticker.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new WolfInClothingEffect(), true));
    }

    private WolfInClothing(final WolfInClothing card) {
        super(card);
    }

    @Override
    public WolfInClothing copy() {
        return new WolfInClothing(this);
    }
}

class WolfInClothingEffect extends OneShotEffect {

    WolfInClothingEffect() {
        super(Outcome.UnboostCreature);
        staticText = "put a name sticker on {this}. When you do, up to X target creatures each get -1/-1 until end of turn, "
                + "where X is the number of unique vowels on that sticker";
    }

    private WolfInClothingEffect(final WolfInClothingEffect effect) {
        super(effect);
    }

    @Override
    public WolfInClothingEffect copy() {
        return new WolfInClothingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            permanent.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        ReflexiveTriggeredAbility reflexive = new ReflexiveTriggeredAbility(
                new BoostTargetEffect(-1, -1, Duration.EndOfTurn), false
        );
        reflexive.addTarget(new TargetCreaturePermanent(0, 3));
        game.fireReflexiveTriggeredAbility(reflexive, source);
        return true;
    }
}
