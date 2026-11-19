package mage.cards.q;

import mage.MageInt;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.MenaceAbility;
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
public final class QuickFixer extends CardImpl {

    public QuickFixer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.AZRA);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // Whenever this creature deals combat damage to a player, open an Attraction.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
                new CreateTokenEffect(new QuickFixerAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)"),
                false
        ));
    }

    private QuickFixer(final QuickFixer card) {
        super(card);
    }

    @Override
    public QuickFixer copy() {
        return new QuickFixer(this);
    }
}

class QuickFixerAttractionToken extends TokenImpl {

    QuickFixerAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private QuickFixerAttractionToken(final QuickFixerAttractionToken token) {
        super(token);
    }

    @Override
    public QuickFixerAttractionToken copy() {
        return new QuickFixerAttractionToken(this);
    }
}
