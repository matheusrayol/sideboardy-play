package mage.cards.q;

import mage.ConditionalMana;
import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.StationAbility;
import mage.abilities.keyword.StationLevelAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ConditionalAnyColorManaAbility;
import mage.abilities.mana.builder.ConditionalManaBuilder;
import mage.abilities.mana.conditional.ManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class QueensDiamond extends CardImpl {

    private static final DynamicValue chargeCount = new CountersSourceCount(CounterType.CHARGE);

    public QueensDiamond(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPACECRAFT);

        // {T}: Choose a color. Add one mana of that color for each charge counter on Queen's Diamond. Spend this mana only to cast your commander.
        this.addAbility(new ConditionalAnyColorManaAbility(
                new TapSourceCost(), chargeCount, chargeCount, new QueensDiamondManaBuilder(), true
        ));

        // Station
        this.addAbility(new StationAbility());

        // STATION 8+
        // Flying, vigilance, haste
        // 5/5
        this.addAbility(new StationLevelAbility(8)
                .withLevelAbility(FlyingAbility.getInstance())
                .withLevelAbility(VigilanceAbility.getInstance())
                .withLevelAbility(HasteAbility.getInstance())
                .withPT(5, 5));
    }

    private QueensDiamond(final QueensDiamond card) {
        super(card);
    }

    @Override
    public QueensDiamond copy() {
        return new QueensDiamond(this);
    }
}

class QueensDiamondManaBuilder extends ConditionalManaBuilder {

    @Override
    public ConditionalMana build(Object... options) {
        return new QueensDiamondConditionalMana(this.mana);
    }

    @Override
    public String getRule() {
        return "Spend this mana only to cast your commander";
    }
}

class QueensDiamondConditionalMana extends ConditionalMana {

    QueensDiamondConditionalMana(Mana mana) {
        super(mana);
        staticText = "Spend this mana only to cast your commander";
        addCondition(new QueensDiamondManaCondition());
    }
}

class QueensDiamondManaCondition extends ManaCondition {

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costsToPay) {
        if (!(source instanceof SpellAbility)) {
            return false;
        }
        Player controller = game.getPlayer(source.getControllerId());
        MageObject object = game.getObject(source.getSourceId());
        return controller != null
                && object != null
                && game.isCommanderObject(controller, object);
    }
}
