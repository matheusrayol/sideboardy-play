package mage.cards.h;

import mage.ApprovingObject;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterInstantOrSorceryCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class HelmutZemoMastermind extends CardImpl {

    public HelmutZemoMastermind(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.NOBLE);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever Helmut Zemo attacks, you may cast target instant or sorcery card with mana value less than or equal to his power from your graveyard.
        // If that spell would be put into your graveyard, exile it instead. If you cast a spell this way, put a +1/+1 counter on Helmut Zemo.
        Ability ability = new AttacksTriggeredAbility(new HelmutZemoEffect(), false);
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY_FROM_YOUR_GRAVEYARD));
        ability.setTargetAdjuster(HelmutZemoTargetAdjuster.instance);
        this.addAbility(ability);
    }

    private HelmutZemoMastermind(final HelmutZemoMastermind card) {
        super(card);
    }

    @Override
    public HelmutZemoMastermind copy() {
        return new HelmutZemoMastermind(this);
    }
}

enum HelmutZemoTargetAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        Permanent source = ability.getSourcePermanentOrLKI(game);
        int maxMv = source != null ? source.getPower().getValue() : 0;
        ability.getTargets().clear();
        FilterInstantOrSorceryCard filter = new FilterInstantOrSorceryCard(
                "instant or sorcery card with mana value " + maxMv + " or less from your graveyard"
        );
        filter.add(new ManaValuePredicate(ComparisonType.FEWER_THAN, maxMv + 1));
        ability.addTarget(new TargetCardInYourGraveyard(filter));
    }
}

class HelmutZemoEffect extends OneShotEffect {

    HelmutZemoEffect() {
        super(Outcome.PlayForFree);
        this.staticText = "you may cast target instant or sorcery card with mana value less than or equal to his power from your graveyard. "
                + "If that spell would be put into your graveyard, exile it instead. If you cast a spell this way, put a +1/+1 counter on {this}";
    }

    private HelmutZemoEffect(final HelmutZemoEffect effect) {
        super(effect);
    }

    @Override
    public HelmutZemoEffect copy() {
        return new HelmutZemoEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null || game.getState().getZone(card.getId()) != Zone.GRAVEYARD) {
            return false;
        }
        if (!controller.chooseUse(Outcome.Benefit, "Cast " + card.getLogName() + " from your graveyard?", source, game)) {
            return true;
        }
        int zcc = game.getState().getZoneChangeCounter(card.getId());
        game.getState().setValue("PlayFromNotOwnHandZone" + card.getId(), Boolean.TRUE);
        boolean cast = controller.cast(
                controller.chooseAbilityForCast(card, game, false),
                game, false, new ApprovingObject(source, game)
        );
        game.getState().setValue("PlayFromNotOwnHandZone" + card.getId(), null);
        if (cast) {
            game.addEffect(new HelmutZemoExileReplacementEffect(card.getId(), zcc + 1), source);
            Permanent zemo = source.getSourcePermanentIfItStillExists(game);
            if (zemo != null) {
                zemo.addCounters(CounterType.P1P1.createInstance(), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}

class HelmutZemoExileReplacementEffect extends ReplacementEffectImpl {

    private final UUID cardId;
    private final int zccOnStack;

    HelmutZemoExileReplacementEffect(UUID cardId, int zccOnStack) {
        super(Duration.EndOfTurn, Outcome.Exile);
        this.cardId = cardId;
        this.zccOnStack = zccOnStack;
    }

    private HelmutZemoExileReplacementEffect(final HelmutZemoExileReplacementEffect effect) {
        super(effect);
        this.cardId = effect.cardId;
        this.zccOnStack = effect.zccOnStack;
    }

    @Override
    public HelmutZemoExileReplacementEffect copy() {
        return new HelmutZemoExileReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        return cardId.equals(zEvent.getTargetId())
                && zEvent.getFromZone() == Zone.STACK
                && zEvent.getToZone() == Zone.GRAVEYARD
                && game.getState().getZoneChangeCounter(cardId) == zccOnStack;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(cardId);
        if (controller == null || card == null) {
            return false;
        }
        discard();
        return controller.moveCards(card, Zone.EXILED, source, game, false, false, false, event.getAppliedEffects());
    }
}
