package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SpyKit extends CardImpl {

    public SpyKit(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        this.subtype.add(SubType.EQUIPMENT);

        // Equipped creature gets +1/+1 and has all names of nonlegendary creature cards in addition to its name.
        Ability ability = new SimpleStaticAbility(new BoostEquippedEffect(1, 1));
        ability.addEffect(new InfoEffect("and has all names of nonlegendary creature cards in addition to its name"));
        this.addAbility(ability);

        // Equip {2}
        this.addAbility(new EquipAbility(2));
    }

    private SpyKit(final SpyKit card) {
        super(card);
    }

    @Override
    public SpyKit copy() {
        return new SpyKit(this);
    }
}
