package mage.cards.s;

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
public final class StepRightUp extends CardImpl {

    public StepRightUp(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{B}");

        // Open two Attractions. (Put the top two cards of your Attraction deck onto the battlefield.)
        this.getSpellAbility().addEffect(
                new CreateTokenEffect(new StepRightUpAttractionToken(), 2)
                        .setText("Open two Attractions. (Put the top two cards of your Attraction deck onto the battlefield.)")
        );
    }

    private StepRightUp(final StepRightUp card) {
        super(card);
    }

    @Override
    public StepRightUp copy() {
        return new StepRightUp(this);
    }
}

class StepRightUpAttractionToken extends TokenImpl {

    StepRightUpAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private StepRightUpAttractionToken(final StepRightUpAttractionToken token) {
        super(token);
    }

    @Override
    public StepRightUpAttractionToken copy() {
        return new StepRightUpAttractionToken(this);
    }
}
