package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.VigilanceAbility;
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
public final class GrabbyTabby extends CardImpl {

    public GrabbyTabby(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.CAT);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // This creature has vigilance as long as you control a stickered permanent.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(VigilanceAbility.getInstance(), Duration.WhileOnBattlefield),
                GrabbyTabbyCondition.instance,
                "this creature has vigilance as long as you control a stickered permanent"
        )));
    }

    private GrabbyTabby(final GrabbyTabby card) {
        super(card);
    }

    @Override
    public GrabbyTabby copy() {
        return new GrabbyTabby(this);
    }
}

enum GrabbyTabbyCondition implements Condition {
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
