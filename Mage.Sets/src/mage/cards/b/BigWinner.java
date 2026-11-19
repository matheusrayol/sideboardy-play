package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BigWinner extends CardImpl {

    public BigWinner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.OGRE);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(5);
        this.toughness = new MageInt(2);

        // This creature has trample as long as you control a stickered permanent.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(TrampleAbility.getInstance(), Duration.WhileOnBattlefield),
                BigWinnerCondition.instance,
                "this creature has trample as long as you control a stickered permanent"
        )));
    }

    private BigWinner(final BigWinner card) {
        super(card);
    }

    @Override
    public BigWinner copy() {
        return new BigWinner(this);
    }
}

enum BigWinnerCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT, source.getControllerId(), game
        )) {
            if (permanent.getCounters(game).keySet().stream()
                    .anyMatch(k -> k.toLowerCase(Locale.ROOT).contains("sticker"))) {
                return true;
            }
        }
        return false;
    }
}
