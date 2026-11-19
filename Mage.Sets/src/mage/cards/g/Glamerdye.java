package mage.cards.g;

import mage.abilities.keyword.RetraceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.s.SleightOfMind;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.target.common.TargetSpellOrPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class Glamerdye extends CardImpl {

    public Glamerdye(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Change the text of target spell or permanent by replacing all instances of one color word with another.
        this.getSpellAbility().addEffect(new SleightOfMind.ColorWordChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one color word with another"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());

        // Retrace
        this.addAbility(new RetraceAbility(this));
    }

    private Glamerdye(final Glamerdye card) {
        super(card);
    }

    @Override
    public Glamerdye copy() {
        return new Glamerdye(this);
    }
}
