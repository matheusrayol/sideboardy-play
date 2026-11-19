package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.DoctorsCompanionAbility;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class SusanForeman extends CardImpl {

    public SusanForeman(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // If you would planeswalk, instead look at the top two cards of your planar deck, put one on the bottom of your planar deck and the other on top, then planeswalk.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "If you would planeswalk, instead look at the top two cards of your planar deck, "
                        + "put one on the bottom of your planar deck and the other on top, then planeswalk."
        )));

        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

        // Doctor's companion
        this.addAbility(DoctorsCompanionAbility.getInstance());
    }

    private SusanForeman(final SusanForeman card) {
        super(card);
    }

    @Override
    public SusanForeman copy() {
        return new SusanForeman(this);
    }
}
