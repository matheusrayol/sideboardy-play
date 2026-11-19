package mage.cards.u;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToHandEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
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
public final class UnlawfulEntry extends CardImpl {

    public UnlawfulEntry(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{U}");

        // Target creature gets +1/+0 and gains flying until end of turn.
        this.getSpellAbility().addEffect(new BoostTargetEffect(1, 0, Duration.EndOfTurn)
                .setText("Target creature gets +1/+0"));
        this.getSpellAbility().addEffect(new GainAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn)
                .setText("and gains flying until end of turn"));
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());

        // Whenever you put an art sticker on a creature, you may return this card from your graveyard to your hand.
        this.addAbility(new UnlawfulEntryTriggeredAbility());
    }

    private UnlawfulEntry(final UnlawfulEntry card) {
        super(card);
    }

    @Override
    public UnlawfulEntry copy() {
        return new UnlawfulEntry(this);
    }
}

class UnlawfulEntryTriggeredAbility extends TriggeredAbilityImpl {

    UnlawfulEntryTriggeredAbility() {
        super(Zone.GRAVEYARD, new ReturnSourceFromGraveyardToHandEffect(), true);
    }

    private UnlawfulEntryTriggeredAbility(final UnlawfulEntryTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public UnlawfulEntryTriggeredAbility copy() {
        return new UnlawfulEntryTriggeredAbility(this);
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
        return "Whenever you put an art sticker on a creature, you may return {this} from your graveyard to your hand.";
    }
}
