package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksWithCreaturesTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.combat.CantBlockTargetEffect;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.predicate.permanent.GoadedPredicate;
import mage.game.Game;
import mage.game.events.DamagedBatchForOnePlayerEvent;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PuppetMasterStringPuller extends CardImpl {

    public PuppetMasterStringPuller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ARTIFICER);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever you attack, goad target creature an opponent controls. It can't block this turn.
        Ability ability = new AttacksWithCreaturesTriggeredAbility(new GoadTargetEffect(), 1);
        ability.addEffect(new CantBlockTargetEffect(Duration.EndOfTurn).setText("It can't block this turn"));
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE));
        this.addAbility(ability);

        // Whenever one or more goaded creatures deal combat damage to one of your opponents, create a Treasure token.
        this.addAbility(new PuppetMasterTriggeredAbility());
    }

    private PuppetMasterStringPuller(final PuppetMasterStringPuller card) {
        super(card);
    }

    @Override
    public PuppetMasterStringPuller copy() {
        return new PuppetMasterStringPuller(this);
    }
}

class PuppetMasterTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<DamagedPlayerEvent> {

    PuppetMasterTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new TreasureToken()), false);
    }

    private PuppetMasterTriggeredAbility(final PuppetMasterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public PuppetMasterTriggeredAbility copy() {
        return new PuppetMasterTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_BATCH_FOR_ONE_PLAYER;
    }

    @Override
    public boolean checkEvent(DamagedPlayerEvent event, Game game) {
        if (!event.isCombatDamage() || event.getAmount() <= 0) {
            return false;
        }
        if (!game.getOpponents(this.getControllerId()).contains(event.getTargetId())) {
            return false;
        }
        Permanent permanent = game.getPermanentOrLKIBattlefield(event.getSourceId());
        return permanent != null && GoadedPredicate.instance.apply(permanent, game);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return !getFilteredEvents((DamagedBatchForOnePlayerEvent) event, game).isEmpty();
    }

    @Override
    public String getRule() {
        return "Whenever one or more goaded creatures deal combat damage to one of your opponents, create a Treasure token.";
    }
}
