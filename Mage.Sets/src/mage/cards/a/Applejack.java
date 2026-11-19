package mage.cards.a;

import mage.MageInt;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.permanent.token.FoodToken;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Applejack extends CardImpl {

    public Applejack(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PONY);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Family Gathering — At the beginning of your end step, put a toy you own onto the battlefield as a 2/2 creature token with that toy's name, colors, and creature types. If the toy has wings, the token has flying. If the toy has a horn, scry 2. If it has neither, create a Food token.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                new CreateTokenEffect(new FoodToken())
                        .setText("put a toy you own onto the battlefield as a 2/2 creature token with that toy's name, colors, and creature types. "
                                + "If the toy has wings, the token has flying. If the toy has a horn, scry 2. If it has neither, create a Food token")
        ).withFlavorWord("Family Gathering"));
    }

    private Applejack(final Applejack card) {
        super(card);
    }

    @Override
    public Applejack copy() {
        return new Applejack(this);
    }
}
