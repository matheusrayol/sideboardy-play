package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.LifelinkAbility;
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
public final class SanguineSipper extends CardImpl {

    public SanguineSipper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // This creature has lifelink as long as you control a stickered permanent.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(LifelinkAbility.getInstance(), Duration.WhileOnBattlefield),
                SanguineSipperCondition.instance,
                "this creature has lifelink as long as you control a stickered permanent"
        )));
    }

    private SanguineSipper(final SanguineSipper card) {
        super(card);
    }

    @Override
    public SanguineSipper copy() {
        return new SanguineSipper(this);
    }
}

enum SanguineSipperCondition implements Condition {
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
