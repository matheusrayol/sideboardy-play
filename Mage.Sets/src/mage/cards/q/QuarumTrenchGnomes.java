package mage.cards.q;

import mage.MageInt;
import mage.MageObjectReference;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.FilterPermanent;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ManaEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class QuarumTrenchGnomes extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent(SubType.PLAINS, "Plains");

    public QuarumTrenchGnomes(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.GNOME);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: If target Plains is tapped for mana, it produces colorless mana instead of white mana. (This effect lasts indefinitely.)
        Ability ability = new SimpleActivatedAbility(new QuarumTrenchGnomesEffect(), new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private QuarumTrenchGnomes(final QuarumTrenchGnomes card) {
        super(card);
    }

    @Override
    public QuarumTrenchGnomes copy() {
        return new QuarumTrenchGnomes(this);
    }
}

class QuarumTrenchGnomesEffect extends ReplacementEffectImpl {

    private MageObjectReference targetRef;

    QuarumTrenchGnomesEffect() {
        super(Duration.Custom, Outcome.Detriment);
        staticText = "If target Plains is tapped for mana, it produces colorless mana instead of white mana";
    }

    private QuarumTrenchGnomesEffect(final QuarumTrenchGnomesEffect effect) {
        super(effect);
        this.targetRef = effect.targetRef;
    }

    @Override
    public QuarumTrenchGnomesEffect copy() {
        return new QuarumTrenchGnomesEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent != null) {
            this.targetRef = new MageObjectReference(permanent, game);
        } else {
            this.discard();
        }
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        if (targetRef == null || targetRef.getPermanent(game) == null) {
            return true;
        }
        return super.isInactive(source, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Mana mana = ((ManaEvent) event).getMana();
        int white = mana.getWhite();
        if (white > 0) {
            mana.setColorless(mana.getColorless() + white);
            mana.setWhite(0);
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TAPPED_FOR_MANA;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (targetRef == null) {
            return false;
        }
        Permanent permanent = ((TappedForManaEvent) event).getPermanent();
        return permanent != null && targetRef.refersTo(permanent, game);
    }
}
