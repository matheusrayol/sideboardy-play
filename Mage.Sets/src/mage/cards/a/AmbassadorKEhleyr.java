package mage.cards.a;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.common.ParleyCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardAllEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.game.Game;

/**
 *
 * @author muz
 */
public final class AmbassadorKEhleyr extends CardImpl {

    public AmbassadorKEhleyr(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.KLINGON);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Parley — At the beginning of combat on your turn, each player reveals the top card of their library, then draws a card. For each nonland card revealed this way, put a +1/+1 counter on Ambassador K'Ehleyr.
        this.addAbility(new BeginningOfCombatTriggeredAbility(new AmbassadorKEhleyrEffect())
                .setAbilityWord(AbilityWord.PARLEY));
    }

    private AmbassadorKEhleyr(final AmbassadorKEhleyr card) {
        super(card);
    }

    @Override
    public AmbassadorKEhleyr copy() {
        return new AmbassadorKEhleyr(this);
    }
}

class AmbassadorKEhleyrEffect extends OneShotEffect {

    AmbassadorKEhleyrEffect() {
        super(Outcome.BoostCreature);
        staticText = "each player reveals the top card of their library, then draws a card. For each nonland card revealed this way, put a +1/+1 counter on {this}";
    }

    private AmbassadorKEhleyrEffect(final AmbassadorKEhleyrEffect effect) {
        super(effect);
    }

    @Override
    public AmbassadorKEhleyrEffect copy() {
        return new AmbassadorKEhleyrEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int count = ParleyCount.getInstance().calculate(game, source, this);
        new DrawCardAllEffect(1).apply(game, source);
        if (count > 0) {
            new AddCountersSourceEffect(CounterType.P1P1.createInstance(count)).apply(game, source);
        }
        return true;
    }
}
