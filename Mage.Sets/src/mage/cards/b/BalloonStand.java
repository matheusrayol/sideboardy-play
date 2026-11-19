package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class BalloonStand extends CardImpl {

    public BalloonStand(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "");

        this.subtype.add(SubType.ATTRACTION);

        // Visit — Choose one —
        // • Create a 1/1 red Balloon creature token with flying.
        // • Sacrifice a Balloon. If you do, target creature gains flying until end of turn.
        Ability ability = new BalloonStandTriggeredAbility();
        Mode mode = new Mode(
                new GainAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn)
                        .setText("Sacrifice a Balloon. If you do, target creature gains flying until end of turn")
        );
        mode.addTarget(new TargetCreaturePermanent());
        ability.addMode(mode);
        this.addAbility(ability.withFlavorWord("Visit"));
    }

    private BalloonStand(final BalloonStand card) {
        super(card);
    }

    @Override
    public BalloonStand copy() {
        return new BalloonStand(this);
    }
}

class BalloonStandTriggeredAbility extends TriggeredAbilityImpl {

    BalloonStandTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CreateTokenEffect(new BalloonStandToken()), false);
    }

    private BalloonStandTriggeredAbility(final BalloonStandTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public BalloonStandTriggeredAbility copy() {
        return new BalloonStandTriggeredAbility(this);
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
        return result == 2 || result == 6;
    }

    @Override
    public String getRule() {
        return "Choose one — "
                + "• Create a 1/1 red Balloon creature token with flying. "
                + "• Sacrifice a Balloon. If you do, target creature gains flying until end of turn.";
    }
}

class BalloonStandToken extends TokenImpl {

    BalloonStandToken() {
        super("Balloon Token", "1/1 red Balloon creature token with flying");
        cardType.add(CardType.CREATURE);
        color.setRed(true);
        subtype.add(SubType.BALLOON);
        power = new MageInt(1);
        toughness = new MageInt(1);
        this.addAbility(FlyingAbility.getInstance());
    }

    private BalloonStandToken(final BalloonStandToken token) {
        super(token);
    }

    @Override
    public BalloonStandToken copy() {
        return new BalloonStandToken(this);
    }
}
