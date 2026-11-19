package mage.cards.l;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SkipUntapOptionalAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledPermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class LwaxanaTroiCharmingGuest extends CardImpl {

    private static final FilterControlledCreaturePermanent filter =
            new FilterControlledCreaturePermanent("another target creature you control");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public LwaxanaTroiCharmingGuest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.BETAZOID);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // You may choose not to untap Lwaxana Troi during your untap step.
        this.addAbility(new SkipUntapOptionalAbility());

        // {2}, {T}: Choose another target creature you control. For as long as Lwaxana Troi remains tapped, if a triggered ability of that creature triggers, that ability triggers an additional time.
        Ability ability = new SimpleActivatedAbility(new LwaxanaTroiEffect(), new GenericManaCost(2));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetControlledPermanent(filter));
        this.addAbility(ability);
    }

    private LwaxanaTroiCharmingGuest(final LwaxanaTroiCharmingGuest card) {
        super(card);
    }

    @Override
    public LwaxanaTroiCharmingGuest copy() {
        return new LwaxanaTroiCharmingGuest(this);
    }
}

class LwaxanaTroiEffect extends OneShotEffect {

    LwaxanaTroiEffect() {
        super(Outcome.Benefit);
        this.staticText = "choose another target creature you control. For as long as {this} remains tapped, "
                + "if a triggered ability of that creature triggers, that ability triggers an additional time";
    }

    private LwaxanaTroiEffect(final LwaxanaTroiEffect effect) {
        super(effect);
    }

    @Override
    public LwaxanaTroiEffect copy() {
        return new LwaxanaTroiEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent lwaxana = source.getSourcePermanentIfItStillExists(game);
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (lwaxana == null || target == null || !lwaxana.isTapped()) {
            return false;
        }
        game.addEffect(new LwaxanaTroiReplacementEffect(
                new MageObjectReference(lwaxana, game),
                new MageObjectReference(target, game)
        ), source);
        return true;
    }
}

class LwaxanaTroiReplacementEffect extends ReplacementEffectImpl {

    private final MageObjectReference lwaxanaMor;
    private final MageObjectReference targetMor;

    LwaxanaTroiReplacementEffect(MageObjectReference lwaxanaMor, MageObjectReference targetMor) {
        super(Duration.Custom, Outcome.Benefit);
        this.lwaxanaMor = lwaxanaMor;
        this.targetMor = targetMor;
    }

    private LwaxanaTroiReplacementEffect(final LwaxanaTroiReplacementEffect effect) {
        super(effect);
        this.lwaxanaMor = effect.lwaxanaMor;
        this.targetMor = effect.targetMor;
    }

    @Override
    public LwaxanaTroiReplacementEffect copy() {
        return new LwaxanaTroiReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.NUMBER_OF_TRIGGERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent lwaxana = lwaxanaMor.getPermanent(game);
        Permanent target = targetMor.getPermanent(game);
        if (lwaxana == null || !lwaxana.isTapped() || target == null) {
            discard();
            return false;
        }
        return targetMor.refersTo(event.getSourceId(), game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowInc(event.getAmount(), 1));
        return false;
    }
}
