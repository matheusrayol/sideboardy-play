package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SpirePhantasm extends CardImpl {

    public SpirePhantasm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{U}");

        this.subtype.add(SubType.GARGOYLE);
        this.subtype.add(SubType.ILLUSION);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Reveal Spire Phantasm as you draft it. The next time a player drafts a card from this booster pack, guess that card's name. Then that player reveals the drafted card.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it. The next time a player drafts a card from this booster pack, "
                        + "guess that card's name. Then that player reveals the drafted card - not implemented.")
        ));

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Spire Phantasm enters, if you guessed correctly for a card named Spire Phantasm, draw a card.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DrawCardSourceControllerEffect(1))
                .withInterveningIf(SpirePhantasmCondition.instance));
    }

    private SpirePhantasm(final SpirePhantasm card) {
        super(card);
    }

    @Override
    public SpirePhantasm copy() {
        return new SpirePhantasm(this);
    }
}

enum SpirePhantasmCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return Boolean.TRUE.equals(game.getState().getValue(source.getControllerId() + "_spire_phantasm_guessed"));
    }

    @Override
    public String toString() {
        return "you guessed correctly for a card named {this}";
    }
}
