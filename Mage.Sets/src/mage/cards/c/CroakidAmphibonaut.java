package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class CroakidAmphibonaut extends CardImpl {

    public CroakidAmphibonaut(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}");

        this.subtype.add(SubType.FROG);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // This creature has flying as long as you control a stickered permanent.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(FlyingAbility.getInstance(), Duration.WhileOnBattlefield),
                CroakidAmphibonautCondition.instance,
                "this creature has flying as long as you control a stickered permanent"
        )));
    }

    private CroakidAmphibonaut(final CroakidAmphibonaut card) {
        super(card);
    }

    @Override
    public CroakidAmphibonaut copy() {
        return new CroakidAmphibonaut(this);
    }
}

enum CroakidAmphibonautCondition implements Condition {
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
