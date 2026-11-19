package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PalianoVanguard extends CardImpl {

    public PalianoVanguard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Draft Paliano Vanguard face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Draft {this} face up - not implemented.")
        ));

        // As you draft a creature card, you may reveal it, note its creature types, then turn this card face down.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("As you draft a creature card, you may reveal it, note its creature types, then turn this card face down - not implemented.")
        ));

        // Other creatures you control of a type you noted for cards named Paliano Vanguard get +1/+1.
        this.addAbility(new SimpleStaticAbility(new PalianoVanguardEffect()));
    }

    private PalianoVanguard(final PalianoVanguard card) {
        super(card);
    }

    @Override
    public PalianoVanguard copy() {
        return new PalianoVanguard(this);
    }
}

class PalianoVanguardEffect extends ContinuousEffectImpl {

    PalianoVanguardEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Other creatures you control of a type you noted for cards named {this} get +1/+1";
    }

    private PalianoVanguardEffect(final PalianoVanguardEffect effect) {
        super(effect);
    }

    @Override
    public PalianoVanguardEffect copy() {
        return new PalianoVanguardEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Object raw = game.getState().getValue(source.getControllerId() + "_paliano_vanguard_types");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return false;
        }
        Collection<?> notedTypes = (Collection<?>) raw;
        for (Permanent perm : game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURE, source.getControllerId(), source, game)) {
            if (perm.getId().equals(source.getSourceId())) {
                continue;
            }
            for (Object item : notedTypes) {
                if (item instanceof SubType && perm.hasSubtype((SubType) item, game)) {
                    perm.addPower(1);
                    perm.addToughness(1);
                    break;
                }
            }
        }
        return true;
    }
}
