package mage.cards.w;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class WickerPicker extends CardImpl {

    public WickerPicker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.subtype.add(SubType.SCARECROW);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Creature spells you cast have sticker kicker {1}. (You may pay an additional {1} as you cast a creature spell. If you do, you get {TK}, then you may put a sticker on it.)
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Creature spells you cast have sticker kicker {1}. "
                        + "(You may pay an additional {1} as you cast a creature spell. If you do, you get {TK}, then you may put a sticker on it.)"
        )));
    }

    private WickerPicker(final WickerPicker card) {
        super(card);
    }

    @Override
    public WickerPicker copy() {
        return new WickerPicker(this);
    }
}
