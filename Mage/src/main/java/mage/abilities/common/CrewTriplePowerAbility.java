package mage.abilities.common;

import mage.abilities.StaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.constants.Zone;

/**
 * @author Sideboardy
 */
public class CrewTriplePowerAbility extends StaticAbility {

    public CrewTriplePowerAbility() {
        super(Zone.BATTLEFIELD, new InfoEffect("{this} crews Vehicles as though its power were tripled."));
    }

    private CrewTriplePowerAbility(final CrewTriplePowerAbility ability) {
        super(ability);
    }

    @Override
    public CrewTriplePowerAbility copy() {
        return new CrewTriplePowerAbility(this);
    }
}
