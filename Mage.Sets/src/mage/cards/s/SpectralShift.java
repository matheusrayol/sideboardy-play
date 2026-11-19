package mage.cards.s;

import mage.abilities.Mode;
import mage.abilities.keyword.EntwineAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.m.MagicalHack;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.target.common.TargetSpellOrPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SpectralShift extends CardImpl {

    public SpectralShift(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Choose one —
        // • Change the text of target spell or permanent by replacing all instances of one basic land type with another.
        this.getSpellAbility().addEffect(new MagicalHack.BasicLandTypeChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one basic land type with another. "
                        + "<i>(This effect lasts indefinitely.)</i>"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());

        // • Change the text of target spell or permanent by replacing all instances of one color word with another.
        Mode mode = new Mode(new SleightOfMind.ColorWordChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one color word with another. "
                        + "<i>(This effect lasts indefinitely.)</i>"
        ));
        mode.addTarget(new TargetSpellOrPermanent());
        this.getSpellAbility().addMode(mode);

        // Entwine {2}
        this.addAbility(new EntwineAbility("{2}"));
    }

    private SpectralShift(final SpectralShift card) {
        super(card);
    }

    @Override
    public SpectralShift copy() {
        return new SpectralShift(this);
    }
}
