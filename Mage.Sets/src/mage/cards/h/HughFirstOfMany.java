package mage.cards.h;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.continuous.EachSpellYouCastHasReplicateEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterSpell;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HughFirstOfMany extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("artifact spells you cast");

    static {
        filter.add(CardType.ARTIFACT.getPredicate());
    }

    public HughFirstOfMany(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{1}{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BORG);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Artifact spells you cast have replicate {2}{U}.
        this.addAbility(new SimpleStaticAbility(new EachSpellYouCastHasReplicateEffect(
                filter,
                "Whenever you cast an artifact spell, copy it for each time you paid its replicate cost. Each copy becomes a token.",
                new ManaCostsImpl<>("{2}{U}")
        )));
    }

    private HughFirstOfMany(final HughFirstOfMany card) {
        super(card);
    }

    @Override
    public HughFirstOfMany copy() {
        return new HughFirstOfMany(this);
    }
}
