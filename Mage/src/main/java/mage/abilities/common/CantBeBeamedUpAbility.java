package mage.abilities.common;

import mage.abilities.MageSingleton;
import mage.abilities.StaticAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.constants.Zone;

import java.io.ObjectStreamException;

/**
 * @author Sideboardy
 */
public class CantBeBeamedUpAbility extends StaticAbility implements MageSingleton {

    private static final CantBeBeamedUpAbility instance = new CantBeBeamedUpAbility();

    private Object readResolve() throws ObjectStreamException {
        return instance;
    }

    public static CantBeBeamedUpAbility getInstance() {
        return instance;
    }

    private CantBeBeamedUpAbility() {
        super(Zone.BATTLEFIELD, new InfoEffect("{this} can't be beamed up."));
    }

    @Override
    public CantBeBeamedUpAbility copy() {
        return instance;
    }
}
