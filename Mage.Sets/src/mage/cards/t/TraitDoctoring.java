package mage.cards.t;

import mage.abilities.effects.common.CipherEffect;
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
public final class TraitDoctoring extends CardImpl {

    public TraitDoctoring(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{U}");

        // Change the text of target permanent by replacing all instances of one color word with another or one basic land type with another until end of turn.
        this.getSpellAbility().addEffect(new MindBend.ColorOrLandTypeChangeEffect(
                Duration.EndOfTurn,
                "change the text of target permanent by replacing all instances of one color word with another "
                        + "or one basic land type with another until end of turn"
        ));
        this.getSpellAbility().addTarget(new TargetPermanent());

        // Cipher
        this.getSpellAbility().addEffect(new CipherEffect());
    }

    private TraitDoctoring(final TraitDoctoring card) {
        super(card);
    }

    @Override
    public TraitDoctoring copy() {
        return new TraitDoctoring(this);
    }
}
