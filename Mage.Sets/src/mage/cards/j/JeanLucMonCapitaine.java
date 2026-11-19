package mage.cards.j;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.EntersTheBattlefieldEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class JeanLucMonCapitaine extends CardImpl {

    public JeanLucMonCapitaine(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Federation — Faeries you control enter with an additional +1/+1 counter on them for each creature type among non-Borg creatures you already control.
        this.addAbility(new SimpleStaticAbility(new JeanLucReplacementEffect()).withFlavorWord("Federation"));
    }

    private JeanLucMonCapitaine(final JeanLucMonCapitaine card) {
        super(card);
    }

    @Override
    public JeanLucMonCapitaine copy() {
        return new JeanLucMonCapitaine(this);
    }
}

class JeanLucReplacementEffect extends ReplacementEffectImpl {

    JeanLucReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature, false);
        this.staticText = "Faeries you control enter with an additional +1/+1 counter on them "
                + "for each creature type among non-Borg creatures you already control";
    }

    private JeanLucReplacementEffect(final JeanLucReplacementEffect effect) {
        super(effect);
    }

    @Override
    public JeanLucReplacementEffect copy() {
        return new JeanLucReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((EntersTheBattlefieldEvent) event).getTarget();
        return permanent != null
                && permanent.isControlledBy(source.getControllerId())
                && permanent.hasSubtype(SubType.FAERIE, game);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent entering = ((EntersTheBattlefieldEvent) event).getTarget();
        if (entering == null) {
            return false;
        }
        int count = countCreatureTypes(entering.getId(), source, game);
        if (count > 0) {
            entering.addCounters(
                    CounterType.P1P1.createInstance(count),
                    source.getControllerId(),
                    source,
                    game,
                    event.getAppliedEffects()
            );
        }
        return false;
    }

    private static int countCreatureTypes(UUID enteringId, Ability source, Game game) {
        Set<SubType> found = new HashSet<>();
        Set<SubType> allCreatureTypes = SubType.getCreatureTypes();
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game
        )) {
            if (permanent.getId().equals(enteringId) || permanent.hasSubtype(SubType.BORG, game)) {
                continue;
            }
            if (permanent.isAllCreatureTypes(game)) {
                return allCreatureTypes.size();
            }
            for (SubType subType : permanent.getSubtype(game)) {
                if (allCreatureTypes.contains(subType)) {
                    found.add(subType);
                }
            }
        }
        return found.size();
    }
}
