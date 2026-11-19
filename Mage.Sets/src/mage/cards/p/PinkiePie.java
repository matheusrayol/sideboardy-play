package mage.cards.p;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PinkiePie extends CardImpl {

    public PinkiePie(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PONY);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever you cast a spell with a smile in its art, create a tapped Treasure token.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new CreateTokenEffect(new TreasureToken(), 1, true),
                StaticFilters.FILTER_SPELL_A,
                false
        ).setTriggerPhrase("Whenever you cast a spell with a smile in its art, "));

        // Everypony's Invited — Your party consists of each creature you control, and your party is always full.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "Your party consists of each creature you control, and your party is always full"
        )).withFlavorWord("Everypony's Invited"));
    }

    private PinkiePie(final PinkiePie card) {
        super(card);
    }

    @Override
    public PinkiePie copy() {
        return new PinkiePie(this);
    }
}
