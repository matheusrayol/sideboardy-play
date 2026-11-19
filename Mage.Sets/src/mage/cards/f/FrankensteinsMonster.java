package mage.cards.f;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.BoostCounter;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class FrankensteinsMonster extends CardImpl {

    public FrankensteinsMonster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{X}{B}{B}");

        this.subtype.add(SubType.ZOMBIE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // As Frankenstein's Monster enters, exile X creature cards from your graveyard. If you can't, put it into its owner's graveyard instead of onto the battlefield. For each creature card exiled this way, it enters with a +2/+0, +1/+1, or +0/+2 counter on it.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new FrankensteinsMonsterEffect()));
    }

    private FrankensteinsMonster(final FrankensteinsMonster card) {
        super(card);
    }

    @Override
    public FrankensteinsMonster copy() {
        return new FrankensteinsMonster(this);
    }
}

class FrankensteinsMonsterEffect extends ReplacementEffectImpl {

    FrankensteinsMonsterEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        staticText = "As {this} enters, exile X creature cards from your graveyard. If you can't, put {this} into its owner's graveyard instead of onto the battlefield. "
                + "For each creature card exiled this way, {this} enters with a +2/+0, +1/+1, or +0/+2 counter on it";
    }

    private FrankensteinsMonsterEffect(final FrankensteinsMonsterEffect effect) {
        super(effect);
    }

    @Override
    public FrankensteinsMonsterEffect copy() {
        return new FrankensteinsMonsterEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanentEntering(event.getTargetId());
        if (controller == null || permanent == null) {
            return false;
        }
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        if (xValue <= 0) {
            return false;
        }
        if (controller.getGraveyard().count(StaticFilters.FILTER_CARD_CREATURE, game) < xValue) {
            controller.moveCards(permanent, Zone.GRAVEYARD, source, game);
            return true;
        }
        TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(xValue, xValue, StaticFilters.FILTER_CARD_CREATURE);
        target.withNotTarget(true);
        if (!controller.chooseTarget(Outcome.Exile, target, source, game)) {
            controller.moveCards(permanent, Zone.GRAVEYARD, source, game);
            return true;
        }
        Cards toExile = new CardsImpl();
        for (UUID cardId : target.getTargets()) {
            Card card = controller.getGraveyard().get(cardId, game);
            if (card != null) {
                toExile.add(card);
            }
        }
        if (toExile.size() < xValue) {
            controller.moveCards(permanent, Zone.GRAVEYARD, source, game);
            return true;
        }
        controller.moveCards(toExile, Zone.EXILED, source, game);
        int exiledCount = 0;
        for (UUID cardId : toExile) {
            if (game.getState().getZone(cardId) == Zone.EXILED) {
                exiledCount++;
            }
        }
        if (exiledCount < xValue) {
            controller.moveCards(permanent, Zone.GRAVEYARD, source, game);
            return true;
        }
        int p2p0 = 0;
        int p1p1 = 0;
        int p0p2 = 0;
        for (int i = 0; i < exiledCount; i++) {
            Choice choice = new ChoiceImpl(true);
            choice.setMessage("Choose counter " + (i + 1) + " of " + exiledCount + " for " + permanent.getName());
            choice.getChoices().add("+2/+0");
            choice.getChoices().add("+1/+1");
            choice.getChoices().add("+0/+2");
            if (!controller.choose(Outcome.BoostCreature, choice, game)) {
                p1p1++;
                continue;
            }
            switch (choice.getChoice()) {
                case "+2/+0":
                    p2p0++;
                    break;
                case "+0/+2":
                    p0p2++;
                    break;
                default:
                    p1p1++;
                    break;
            }
        }
        if (p2p0 > 0) {
            permanent.addCounters(new BoostCounter(2, 0, p2p0), controller.getId(), source, game);
        }
        if (p1p1 > 0) {
            permanent.addCounters(CounterType.P1P1.createInstance(p1p1), controller.getId(), source, game);
        }
        if (p0p2 > 0) {
            permanent.addCounters(new BoostCounter(0, 2, p0p2), controller.getId(), source, game);
        }
        return false;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENTERS_THE_BATTLEFIELD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.getSourceId().equals(event.getTargetId());
    }
}
