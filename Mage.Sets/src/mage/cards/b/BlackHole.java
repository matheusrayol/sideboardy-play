package mage.cards.b;

import mage.abilities.effects.common.DestroyTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BlackHole extends CardImpl {

    public BlackHole(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{B}");

        // Destroy target creature and up to X other target creatures, where X is the number of Attractions you've visited this turn.
        this.getSpellAbility().addEffect(new DestroyTargetEffect().setText(
                "destroy target creature and up to X other target creatures, where X is the number of Attractions you've visited this turn"
        ));
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private BlackHole(final BlackHole card) {
        super(card);
    }

    @Override
    public BlackHole copy() {
        return new BlackHole(this);
    }
}
