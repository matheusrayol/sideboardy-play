package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.PowerPredicate;
import mage.game.Game;
import mage.target.TargetPermanent;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CustodiPeacekeeper extends CardImpl {

    public CustodiPeacekeeper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Reveal Custodi Peacekeeper as you draft it and note how many cards you've drafted this draft round, including this card.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it and note how many cards you've drafted this draft round, including this card - not implemented.")
        ));

        // {W}, {T}: Tap target creature with power less than or equal to the highest number you noted for cards named Custodi Peacekeeper.
        Ability ability = new SimpleActivatedAbility(
                new TapTargetEffect().setText("Tap target creature with power less than or equal to the highest number you noted for cards named {this}"),
                new ManaCostsImpl<>("{W}")
        );
        ability.addCost(new TapSourceCost());
        ability.setTargetAdjuster(CustodiPeacekeeperTargetAdjuster.instance);
        this.addAbility(ability);
    }

    private CustodiPeacekeeper(final CustodiPeacekeeper card) {
        super(card);
    }

    @Override
    public CustodiPeacekeeper copy() {
        return new CustodiPeacekeeper(this);
    }
}

enum CustodiPeacekeeperTargetAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        ability.getTargets().clear();
        Object raw = game.getState().getValue(ability.getControllerId() + "_custodi_peacekeeper_max");
        int maxPower = raw instanceof Integer ? (Integer) raw : Integer.MIN_VALUE;
        FilterCreaturePermanent filter = new FilterCreaturePermanent("creature with power less than or equal to the highest noted number");
        filter.add(new PowerPredicate(ComparisonType.OR_LESS, maxPower));
        ability.addTarget(new TargetPermanent(filter));
    }
}
