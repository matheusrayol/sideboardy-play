package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class HeirloomEpic extends CardImpl {

    public HeirloomEpic(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        this.subtype.add(SubType.BOOK);

        // {4}, {T}: Draw a card. For each mana in this ability's activation cost, you may tap an untapped creature you control rather than pay that mana. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(new DrawCardSourceControllerEffect(1), new GenericManaCost(4));
        ability.addCost(new TapSourceCost());
        ability.addEffect(new InfoEffect("For each mana in this ability's activation cost, you may tap an untapped creature you control rather than pay that mana"));
        ability.setCostAdjuster(HeirloomEpicAdjuster.instance);
        this.addAbility(ability);
    }

    private HeirloomEpic(final HeirloomEpic card) {
        super(card);
    }

    @Override
    public HeirloomEpic copy() {
        return new HeirloomEpic(this);
    }
}

enum HeirloomEpicAdjuster implements CostAdjuster {
    instance;

    @Override
    public void reduceCost(Ability ability, Game game) {
        Player controller = game.getPlayer(ability.getControllerId());
        if (controller == null) {
            return;
        }
        int maxTap = Math.min(
                ability.getManaCostsToPay().manaValue(),
                game.getBattlefield().count(StaticFilters.FILTER_CONTROLLED_UNTAPPED_CREATURES, ability.getControllerId(), ability, game)
        );
        if (maxTap <= 0) {
            return;
        }
        if (game.inCheckPlayableState()) {
            CardUtil.reduceCost(ability, maxTap);
            return;
        }
        TargetPermanent target = new TargetControlledPermanent(0, maxTap, StaticFilters.FILTER_CONTROLLED_UNTAPPED_CREATURES, true);
        target.withChooseHint("to tap rather than pay mana");
        if (controller.choose(Outcome.Tap, target, ability, game) && !target.getTargets().isEmpty()) {
            for (UUID permanentId : target.getTargets()) {
                Permanent permanent = game.getPermanent(permanentId);
                if (permanent != null) {
                    permanent.tap(ability, game);
                }
            }
            CardUtil.reduceCost(ability, target.getTargets().size());
        }
    }
}
