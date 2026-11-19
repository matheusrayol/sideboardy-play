package mage.cards.c;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.common.CopyPermanentEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.util.functions.CopyApplier;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ColossalCloneMaster extends CardImpl {

    static final DifferentlyNamedPermanentCount landCount =
            new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    public ColossalCloneMaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{U}");

        this.subtype.add(SubType.GIANT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Strange new worlds — You may have this creature enter as a copy of any creature on the battlefield,
        // except it isn't legendary and it has "This creature's power and toughness are each equal to the number of differently named lands you control."
        EntersBattlefieldAbility ability = new EntersBattlefieldAbility(
                new CopyPermanentEffect(StaticFilters.FILTER_PERMANENT_CREATURE, ColossalCloneMasterCopyApplier.instance),
                true
        );
        ability.setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS);
        ability.addHint(landCount.getHint());
        this.addAbility(ability);
    }

    private ColossalCloneMaster(final ColossalCloneMaster card) {
        super(card);
    }

    @Override
    public ColossalCloneMaster copy() {
        return new ColossalCloneMaster(this);
    }
}

class ColossalCloneMasterCopyApplier extends CopyApplier {

    static final ColossalCloneMasterCopyApplier instance = new ColossalCloneMasterCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        blueprint.getAbilities().add(new SimpleStaticAbility(
                Zone.ALL,
                new SetBasePowerToughnessSourceEffect(ColossalCloneMaster.landCount)
                        .setText("{this}'s power and toughness are each equal to the number of differently named lands you control")
        ));
        return true;
    }

    @Override
    public String getText() {
        return ", except it isn't legendary and it has \"This creature's power and toughness are each equal to the number of differently named lands you control.\"";
    }
}
