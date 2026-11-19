package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.CantBeBlockedSourceEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Trespasser extends CardImpl {

    public Trespasser(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // When this creature enters, you may put a name sticker on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new TrespasserEnterEffect(), true));

        // {3}{U}: This creature gets +1/+0 until end of turn for each name sticker on it. It can't be blocked this turn.
        Ability ability = new SimpleActivatedAbility(
                new BoostSourceEffect(TrespasserStickerCount.instance, StaticValue.get(0), Duration.EndOfTurn)
                        .setText("{this} gets +1/+0 until end of turn for each name sticker on it"),
                new ManaCostsImpl<>("{3}{U}")
        );
        ability.addEffect(new CantBeBlockedSourceEffect(Duration.EndOfTurn).setText("It can't be blocked this turn"));
        this.addAbility(ability);
    }

    private Trespasser(final Trespasser card) {
        super(card);
    }

    @Override
    public Trespasser copy() {
        return new Trespasser(this);
    }
}

class TrespasserEnterEffect extends OneShotEffect {

    TrespasserEnterEffect() {
        super(Outcome.BoostCreature);
        staticText = "put a name sticker on {this}";
    }

    private TrespasserEnterEffect(final TrespasserEnterEffect effect) {
        super(effect);
    }

    @Override
    public TrespasserEnterEffect copy() {
        return new TrespasserEnterEffect(this);
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

enum TrespasserStickerCount implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Permanent permanent = sourceAbility.getSourcePermanentOrLKI(game);
        return permanent != null ? permanent.getCounters(game).getCount("name sticker") : 0;
    }

    @Override
    public TrespasserStickerCount copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "name sticker on {this}";
    }
}
