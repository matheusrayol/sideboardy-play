package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class TPringNeverParted extends CardImpl {

    private static final FilterControlledCreaturePermanent filter1 =
            new FilterControlledCreaturePermanent("another target creature you control");
    private static final FilterCreaturePermanent filter2 =
            new FilterCreaturePermanent("target creature an opponent controls");

    static {
        filter1.add(AnotherPredicate.instance);
        filter2.add(TargetController.OPPONENT.getControllerPredicate());
    }

    public TPringNeverParted(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VULCAN);
        this.subtype.add(SubType.CITIZEN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Kal-if-fee — When T'Pring enters, choose another target creature you control and target creature an opponent controls.
        // You may have the chosen creatures fight each other. Whenever a creature that fought this way dies this turn, you draw a card.
        Ability ability = new EntersBattlefieldTriggeredAbility(new TPringEffect());
        ability.addTarget(new TargetControlledPermanent(filter1));
        ability.addTarget(new TargetPermanent(filter2));
        ability.withFlavorWord("Kal-if-fee");
        this.addAbility(ability);
    }

    private TPringNeverParted(final TPringNeverParted card) {
        super(card);
    }

    @Override
    public TPringNeverParted copy() {
        return new TPringNeverParted(this);
    }
}

class TPringEffect extends OneShotEffect {

    TPringEffect() {
        super(Outcome.Benefit);
        this.staticText = "choose another target creature you control and target creature an opponent controls. "
                + "You may have the chosen creatures fight each other. "
                + "Whenever a creature that fought this way dies this turn, you draw a card";
    }

    private TPringEffect(final TPringEffect effect) {
        super(effect);
    }

    @Override
    public TPringEffect copy() {
        return new TPringEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent perm1 = game.getPermanent(source.getTargets().get(0).getFirstTarget());
        Permanent perm2 = game.getPermanent(source.getTargets().get(1).getFirstTarget());
        if (controller == null || perm1 == null || perm2 == null) {
            return false;
        }
        if (controller.chooseUse(
                Outcome.Benefit,
                "Have " + perm1.getLogName() + " and " + perm2.getLogName() + " fight each other?",
                source,
                game
        )) {
            MageObjectReference mor1 = new MageObjectReference(perm1, game);
            MageObjectReference mor2 = new MageObjectReference(perm2, game);
            game.addDelayedTriggeredAbility(new TPringDelayedTriggeredAbility(mor1, mor2), source);
            perm1.fight(perm2, source, game);
        }
        return true;
    }
}

class TPringDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference mor1;
    private final MageObjectReference mor2;

    TPringDelayedTriggeredAbility(MageObjectReference mor1, MageObjectReference mor2) {
        super(new DrawCardSourceControllerEffect(1), Duration.EndOfTurn, false, false);
        this.mor1 = mor1;
        this.mor2 = mor2;
        setTriggerPhrase("Whenever a creature that fought this way dies this turn, ");
    }

    private TPringDelayedTriggeredAbility(final TPringDelayedTriggeredAbility ability) {
        super(ability);
        this.mor1 = ability.mor1;
        this.mor2 = ability.mor2;
    }

    @Override
    public TPringDelayedTriggeredAbility copy() {
        return new TPringDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (!zEvent.isDiesEvent()) {
            return false;
        }
        Permanent target = zEvent.getTarget();
        return target != null && (mor1.refersTo(target, game) || mor2.refersTo(target, game));
    }
}
