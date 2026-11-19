package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.game.Game;
import mage.players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CentaurOfAttention extends CardImpl {

    public CentaurOfAttention(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{G}");

        this.subtype.add(SubType.CENTAUR);
        this.subtype.add(SubType.PERFORMER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, roll five six-sided dice and store those results on it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new CentaurOfAttentionStoreEffect()));

        // At the beginning of combat on your turn, you may reroll any number of this creature's stored results.
        this.addAbility(new BeginningOfCombatTriggeredAbility(
                TargetController.YOU, new CentaurOfAttentionRerollEffect(), true
        ));

        // This creature gets +X/+X, where X is the greatest number of stored results on it of the same value.
        this.addAbility(new SimpleStaticAbility(new BoostSourceEffect(
                CentaurOfAttentionValue.instance,
                CentaurOfAttentionValue.instance,
                Duration.WhileOnBattlefield
        ).setText("This creature gets +X/+X, where X is the greatest number of stored results on it of the same value")));
    }

    private CentaurOfAttention(final CentaurOfAttention card) {
        super(card);
    }

    @Override
    public CentaurOfAttention copy() {
        return new CentaurOfAttention(this);
    }
}

class CentaurOfAttentionStoreEffect extends OneShotEffect {

    CentaurOfAttentionStoreEffect() {
        super(Outcome.Benefit);
        staticText = "roll five six-sided dice and store those results on it";
    }

    private CentaurOfAttentionStoreEffect(final CentaurOfAttentionStoreEffect effect) {
        super(effect);
    }

    @Override
    public CentaurOfAttentionStoreEffect copy() {
        return new CentaurOfAttentionStoreEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        List<Integer> results = new ArrayList<>(controller.rollDice(Outcome.Benefit, source, game, 6, 5, 0));
        game.getState().setValue("coa_" + source.getSourceId(), results);
        return true;
    }
}

class CentaurOfAttentionRerollEffect extends OneShotEffect {

    CentaurOfAttentionRerollEffect() {
        super(Outcome.Benefit);
        staticText = "reroll any number of {this}'s stored results";
    }

    private CentaurOfAttentionRerollEffect(final CentaurOfAttentionRerollEffect effect) {
        super(effect);
    }

    @Override
    public CentaurOfAttentionRerollEffect copy() {
        return new CentaurOfAttentionRerollEffect(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Object raw = game.getState().getValue("coa_" + source.getSourceId());
        if (!(raw instanceof List)) {
            return false;
        }
        List<Integer> stored = new ArrayList<>((List<Integer>) raw);
        if (stored.isEmpty()) {
            return false;
        }
        Map<Integer, Integer> freq = new HashMap<>();
        int mode = stored.get(0);
        int bestCount = 0;
        for (int v : stored) {
            int count = freq.merge(v, 1, Integer::sum);
            if (count > bestCount) {
                bestCount = count;
                mode = v;
            }
        }
        List<Integer> updated = new ArrayList<>();
        int rerollCount = 0;
        for (int v : stored) {
            if (v == mode) {
                updated.add(v);
            } else {
                rerollCount++;
            }
        }
        if (rerollCount > 0) {
            List<Integer> rerolls = controller.rollDice(Outcome.Benefit, source, game, 6, rerollCount, 0);
            updated.addAll(rerolls);
            game.getState().setValue("coa_" + source.getSourceId(), updated);
        }
        return true;
    }
}

enum CentaurOfAttentionValue implements DynamicValue {
    instance;

    @Override
    @SuppressWarnings("unchecked")
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Object raw = game.getState().getValue("coa_" + sourceAbility.getSourceId());
        if (!(raw instanceof List)) {
            return 0;
        }
        Map<Integer, Integer> freq = new HashMap<>();
        int maxCount = 0;
        for (Integer v : (List<Integer>) raw) {
            if (v != null) {
                int count = freq.merge(v, 1, Integer::sum);
                if (count > maxCount) {
                    maxCount = count;
                }
            }
        }
        return maxCount;
    }

    @Override
    public CentaurOfAttentionValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "the greatest number of stored results on {this} of the same value";
    }
}
