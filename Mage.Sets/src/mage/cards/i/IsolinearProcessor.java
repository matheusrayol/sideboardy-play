package mage.cards.i;

import java.util.UUID;
import mage.ConditionalMana;
import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.DrawDiscardControllerEffect;
import mage.abilities.mana.ConditionalColorlessManaAbility;
import mage.abilities.mana.builder.ConditionalManaBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.game.Game;

/**
 *
 * @author muz
 */
public final class IsolinearProcessor extends CardImpl {

    public IsolinearProcessor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // {T}: Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source.
        this.addAbility(new ConditionalColorlessManaAbility(new TapSourceCost(), 1, new IsolinearProcessorManaBuilder()));

        // {2}, {T}: Draw a card, then discard a card.
        Ability ability = new SimpleActivatedAbility(new DrawDiscardControllerEffect(1, 1), new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private IsolinearProcessor(final IsolinearProcessor card) {
        super(card);
    }

    @Override
    public IsolinearProcessor copy() {
        return new IsolinearProcessor(this);
    }
}

class IsolinearProcessorManaBuilder extends ConditionalManaBuilder {

    @Override
    public ConditionalMana build(Object... options) {
        return new IsolinearProcessorConditionalMana(this.mana);
    }

    @Override
    public String getRule() {
        return "Spend this mana only to cast an artifact spell or activate an ability of an artifact source";
    }
}

class IsolinearProcessorConditionalMana extends ConditionalMana {

    IsolinearProcessorConditionalMana(Mana mana) {
        super(mana);
        staticText = "Spend this mana only to cast an artifact spell or activate an ability of an artifact source";
        addCondition(new IsolinearProcessorCondition());
    }
}

class IsolinearProcessorCondition implements Condition {

    @Override
    public boolean apply(Game game, Ability source) {
        MageObject object = game.getObject(source);
        return object != null && object.isArtifact(game);
    }
}
