package mage.cards.w;

import mage.abilities.keyword.BuybackAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MindBend;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class WhimOfVolrath extends CardImpl {

    public WhimOfVolrath(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Buyback {2}
        this.addAbility(new BuybackAbility("{2}"));

        // Change the text of target permanent by replacing all instances of one color word with another or one basic land type with another until end of turn.
        this.getSpellAbility().addEffect(new MindBend.ColorOrLandTypeChangeEffect(
                Duration.EndOfTurn,
                "change the text of target permanent by replacing all instances of one color word with another "
                        + "or one basic land type with another until end of turn"
        ));
        this.getSpellAbility().addTarget(new TargetPermanent());
    }

    private WhimOfVolrath(final WhimOfVolrath card) {
        super(card);
    }

    @Override
    public WhimOfVolrath copy() {
        return new WhimOfVolrath(this);
    }
}
