package mage.cards.d;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.filter.FilterPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.util.functions.CopyApplier;

import java.util.List;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class DominionSaboteur extends CardImpl {

    public DominionSaboteur(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{U}");

        this.subtype.add(SubType.SHAPESHIFTER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // You may have this creature enter as a copy of any artifact or creature on the battlefield, except it isn't legendary.
        // If you do, it enters with additional counters on it equal to the same number and kinds of counters the copied permanent has on it.
        this.addAbility(new EntersBattlefieldAbility(new DominionSaboteurEffect(), true));
    }

    private DominionSaboteur(final DominionSaboteur card) {
        super(card);
    }

    @Override
    public DominionSaboteur copy() {
        return new DominionSaboteur(this);
    }
}

class DominionSaboteurEffect extends OneShotEffect {

    private static final FilterPermanent filter = new FilterPermanent("artifact or creature");

    static {
        filter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.CREATURE.getPredicate()
        ));
    }

    DominionSaboteurEffect() {
        super(Outcome.Copy);
        this.staticText = "as a copy of any artifact or creature on the battlefield, except it isn't legendary. "
                + "If you do, it enters with additional counters on it equal to the same number and kinds of counters the copied permanent has on it";
    }

    private DominionSaboteurEffect(final DominionSaboteurEffect effect) {
        super(effect);
    }

    @Override
    public DominionSaboteurEffect copy() {
        return new DominionSaboteurEffect(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePermanent = game.getPermanentEntering(source.getSourceId());
        if (sourcePermanent == null) {
            sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        }
        if (controller == null || sourcePermanent == null) {
            return false;
        }
        Target target = new TargetPermanent(filter);
        target.withNotTarget(true);
        if (!target.canChoose(controller.getId(), source, game)) {
            return true;
        }
        controller.choose(Outcome.Copy, target, source, game);
        Permanent copyFromPermanent = game.getPermanent(target.getFirstTarget());
        if (copyFromPermanent == null) {
            return true;
        }
        game.copyPermanent(Duration.Custom, copyFromPermanent, sourcePermanent.getId(), source, DominionSaboteurCopyApplier.instance);
        List<UUID> appliedEffects = (List<UUID>) this.getValue("appliedEffects");
        for (Counter counter : copyFromPermanent.getCounters(game).values()) {
            sourcePermanent.addCounters(counter.copy(), source.getControllerId(), source, game, appliedEffects);
        }
        return true;
    }
}

class DominionSaboteurCopyApplier extends CopyApplier {

    static final DominionSaboteurCopyApplier instance = new DominionSaboteurCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.removeSuperType(SuperType.LEGENDARY);
        return true;
    }
}
