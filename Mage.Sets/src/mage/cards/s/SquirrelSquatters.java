package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.SquirrelToken;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SquirrelSquatters extends CardImpl {

    public SquirrelSquatters(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{G}");

        this.subtype.add(SubType.SQUIRREL);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new SquirrelSquattersAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // Whenever this creature attacks, create a 1/1 green Squirrel creature token that's tapped and attacking for each Attraction you've visited this turn.
        this.addAbility(new AttacksTriggeredAbility(
                new CreateTokenEffect(new SquirrelToken(), 1, true, true)
                        .setText("create a 1/1 green Squirrel creature token that's tapped and attacking for each Attraction you've visited this turn")
        ));
    }

    private SquirrelSquatters(final SquirrelSquatters card) {
        super(card);
    }

    @Override
    public SquirrelSquatters copy() {
        return new SquirrelSquatters(this);
    }
}

class SquirrelSquattersAttractionToken extends TokenImpl {

    SquirrelSquattersAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private SquirrelSquattersAttractionToken(final SquirrelSquattersAttractionToken token) {
        super(token);
    }

    @Override
    public SquirrelSquattersAttractionToken copy() {
        return new SquirrelSquattersAttractionToken(this);
    }
}
