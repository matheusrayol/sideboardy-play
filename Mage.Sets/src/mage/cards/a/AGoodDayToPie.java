package mage.cards.a;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToHandEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class AGoodDayToPie extends CardImpl {

    public AGoodDayToPie(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");

        // Tap up to two target creatures.
        this.getSpellAbility().addEffect(new TapTargetEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent(0, 2));

        // Whenever you put a name sticker on a creature, you may return this card from your graveyard to your hand.
        this.addAbility(new AGoodDayToPieTriggeredAbility());
    }

    private AGoodDayToPie(final AGoodDayToPie card) {
        super(card);
    }

    @Override
    public AGoodDayToPie copy() {
        return new AGoodDayToPie(this);
    }
}

class AGoodDayToPieTriggeredAbility extends TriggeredAbilityImpl {

    AGoodDayToPieTriggeredAbility() {
        super(Zone.GRAVEYARD, new ReturnSourceFromGraveyardToHandEffect(), true);
    }

    private AGoodDayToPieTriggeredAbility(final AGoodDayToPieTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AGoodDayToPieTriggeredAbility copy() {
        return new AGoodDayToPieTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER_ADDED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())
                || event.getData() == null
                || !event.getData().toLowerCase(Locale.ROOT).contains("sticker")) {
            return false;
        }
        Permanent p = game.getPermanentOrLKIBattlefield(event.getTargetId());
        return p != null && p.isCreature(game);
    }

    @Override
    public String getRule() {
        return "Whenever you put a name sticker on a creature, you may return {this} from your graveyard to your hand.";
    }
}
