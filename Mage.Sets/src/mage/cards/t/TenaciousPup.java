package mage.cards.t;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.common.GainLifeEffect;
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
public final class TenaciousPup extends CardImpl {

    public TenaciousPup(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");

        this.subtype.add(SubType.WOLF);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // When Tenacious Pup enters the battlefield, you gain 1 life. You get a one-time boon with "When you cast a creature spell, that creature enters the battlefield with an additional +1/+1 counter, trample counter, and vigilance counter on it."
        Ability ability = new EntersBattlefieldTriggeredAbility(new GainLifeEffect(1));
        ability.addEffect(new InfoEffect(
                "You get a one-time boon with \"When you cast a creature spell, that creature enters the battlefield with an additional +1/+1 counter, trample counter, and vigilance counter on it.\""
        ));
        this.addAbility(ability);
    }

    private TenaciousPup(final TenaciousPup card) {
        super(card);
    }

    @Override
    public TenaciousPup copy() {
        return new TenaciousPup(this);
    }
}
