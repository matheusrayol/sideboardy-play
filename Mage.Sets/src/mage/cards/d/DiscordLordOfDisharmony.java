package mage.cards.d;

import mage.MageInt;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DiscordLordOfDisharmony extends CardImpl {

    public DiscordLordOfDisharmony(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CHIMERA);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // At the beginning of your end step, choose a random nonland Magic card name. Until your next end step, you may cast a copy of a spell with that name, and mana of any type can be spent to cast it. If you cast a spell this way, copy this ability if Discord is on the battlefield.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(new InfoEffect(
                "choose a random nonland Magic card name. Until your next end step, you may cast a copy of a spell with that name, "
                        + "and mana of any type can be spent to cast it. If you cast a spell this way, copy this ability if {this} is on the battlefield"
        )));
    }

    private DiscordLordOfDisharmony(final DiscordLordOfDisharmony card) {
        super(card);
    }

    @Override
    public DiscordLordOfDisharmony copy() {
        return new DiscordLordOfDisharmony(this);
    }
}
