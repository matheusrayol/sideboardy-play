package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.FilterPermanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class SigardianEvangel extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("permanent you don't control");

    static {
        filter.add(TargetController.NOT_YOU.getControllerPredicate());
    }

    public SigardianEvangel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // When Sigardian Evangel enters the battlefield, conjure a card named Sigardian Evangel into your hand. Discard that card at the beginning of the next end step.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new InfoEffect(
                "conjure a card named Sigardian Evangel into your hand. Discard that card at the beginning of the next end step"
        )));

        // When Sigardian Evangel enters the battlefield, tap target permanent you don't control.
        Ability ability = new EntersBattlefieldTriggeredAbility(new TapTargetEffect());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private SigardianEvangel(final SigardianEvangel card) {
        super(card);
    }

    @Override
    public SigardianEvangel copy() {
        return new SigardianEvangel(this);
    }
}
