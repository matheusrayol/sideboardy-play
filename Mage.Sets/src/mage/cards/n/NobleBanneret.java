package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.keyword.LifelinkAbility;
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
import mage.util.CardUtil;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class NobleBanneret extends CardImpl {

    public NobleBanneret(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}{W}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Draft Noble Banneret face up.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Draft {this} face up - not implemented.")
        ));

        // As you draft a creature card, you may reveal it, note its name, then turn this card face down.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("As you draft a creature card, you may reveal it, note its name, then turn this card face down - not implemented.")
        ));

        // As long as you control one or more creatures with a name you noted for cards named Noble Banneret, this creature and those creatures get +1/+1 and have lifelink.
        this.addAbility(new SimpleStaticAbility(new NobleBanneretEffect()));
    }

    private NobleBanneret(final NobleBanneret card) {
        super(card);
    }

    @Override
    public NobleBanneret copy() {
        return new NobleBanneret(this);
    }
}

class NobleBanneretEffect extends ContinuousEffectImpl {

    NobleBanneretEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        staticText = "As long as you control one or more creatures with a name you noted for cards named {this}, "
                + "{this} and those creatures get +1/+1 and have lifelink";
    }

    private NobleBanneretEffect(final NobleBanneretEffect effect) {
        super(effect);
    }

    @Override
    public NobleBanneretEffect copy() {
        return new NobleBanneretEffect(this);
    }

    @Override
    public boolean apply(Layer layer, SubLayer sublayer, Ability source, Game game) {
        Object raw = game.getState().getValue(source.getControllerId() + "_noble_banneret_names");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return false;
        }
        Collection<?> notedNames = (Collection<?>) raw;
        boolean controlsNoted = false;
        for (Permanent perm : game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURE, source.getControllerId(), source, game)) {
            if (matchesAnyNotedName(perm, notedNames, game)) {
                controlsNoted = true;
                break;
            }
        }
        if (!controlsNoted) {
            return false;
        }
        for (Permanent perm : game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURE, source.getControllerId(), source, game)) {
            if (perm.getId().equals(source.getSourceId()) || matchesAnyNotedName(perm, notedNames, game)) {
                if (layer == Layer.PTChangingEffects_7 && sublayer == SubLayer.ModifyPT_7c) {
                    perm.addPower(1);
                    perm.addToughness(1);
                } else if (layer == Layer.AbilityAddingRemovingEffects_6) {
                    perm.addAbility(LifelinkAbility.getInstance(), source.getSourceId(), game);
                }
            }
        }
        return true;
    }

    private static boolean matchesAnyNotedName(Permanent perm, Collection<?> notedNames, Game game) {
        for (Object item : notedNames) {
            if (item instanceof String && CardUtil.haveSameNames(perm, (String) item, game)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7 || layer == Layer.AbilityAddingRemovingEffects_6;
    }
}
