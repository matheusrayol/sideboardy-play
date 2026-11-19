package mage.cards.a;

import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.keyword.FlashbackAbility;
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
public final class AlterReality extends CardImpl {

    public AlterReality(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Change the text of target spell or permanent by replacing all instances of one color word with another. (This effect lasts indefinitely.)
        this.getSpellAbility().addEffect(new SleightOfMind.ColorWordChangeEffect(
                Duration.Custom,
                "change the text of target spell or permanent by replacing all instances of one color word with another. "
                        + "<i>(This effect lasts indefinitely.)</i>"
        ));
        this.getSpellAbility().addTarget(new TargetSpellOrPermanent());

        // Flashback {1}{U}
        this.addAbility(new FlashbackAbility(this, new ManaCostsImpl<>("{1}{U}")));
    }

    private AlterReality(final AlterReality card) {
        super(card);
    }

    @Override
    public AlterReality copy() {
        return new AlterReality(this);
    }
}
