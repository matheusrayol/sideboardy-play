package mage.cards.r;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Regicide extends CardImpl {

    public Regicide(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{B}");

        // Reveal Regicide as you draft it. The player to your right chooses a color, you choose another color, then the player to your left chooses a third color.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it. The player to your right chooses a color, "
                        + "you choose another color, then the player to your left chooses a third color - not implemented.")
        ));

        // Destroy target creature that's one or more of the colors chosen as you drafted cards named Regicide.
        this.getSpellAbility().addEffect(
                new DestroyTargetEffect().setText("Destroy target creature that's one or more of the colors chosen as you drafted cards named {this}")
        );
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private Regicide(final Regicide card) {
        super(card);
    }

    @Override
    public Regicide copy() {
        return new Regicide(this);
    }
}
