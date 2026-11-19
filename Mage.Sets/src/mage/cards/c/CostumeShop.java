package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CostumeShop extends CardImpl {

    public CostumeShop(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — You may put a sticker on a nonland permanent you own.
        this.addAbility(new CostumeShopTriggeredAbility().withFlavorWord("Visit"));
    }

    private CostumeShop(final CostumeShop card) {
        super(card);
    }

    @Override
    public CostumeShop copy() {
        return new CostumeShop(this);
    }
}

class CostumeShopTriggeredAbility extends TriggeredAbilityImpl {

    CostumeShopTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CostumeShopEffect(), false);
    }

    private CostumeShopTriggeredAbility(final CostumeShopTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public CostumeShopTriggeredAbility copy() {
        return new CostumeShopTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        if (drEvent.getRollDieType() != RollDieType.NUMERICAL) {
            return false;
        }
        int result = drEvent.getResult();
        return result == 2 || result == 3 || result == 6;
    }

    @Override
    public String getRule() {
        return "You may put a sticker on a nonland permanent you own.";
    }
}

class CostumeShopEffect extends OneShotEffect {

    CostumeShopEffect() {
        super(Outcome.BoostCreature);
        staticText = "You may put a sticker on a nonland permanent you own";
    }

    private CostumeShopEffect(final CostumeShopEffect effect) {
        super(effect);
    }

    @Override
    public CostumeShopEffect copy() {
        return new CostumeShopEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
