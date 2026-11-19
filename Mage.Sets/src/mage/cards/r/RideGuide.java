package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.TokenImpl;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class RideGuide extends CardImpl {

    public RideGuide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // When this creature enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new RideGuideAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));
    }

    private RideGuide(final RideGuide card) {
        super(card);
    }

    @Override
    public RideGuide copy() {
        return new RideGuide(this);
    }
}

class RideGuideAttractionToken extends TokenImpl {

    RideGuideAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private RideGuideAttractionToken(final RideGuideAttractionToken token) {
        super(token);
    }

    @Override
    public RideGuideAttractionToken copy() {
        return new RideGuideAttractionToken(this);
    }
}
