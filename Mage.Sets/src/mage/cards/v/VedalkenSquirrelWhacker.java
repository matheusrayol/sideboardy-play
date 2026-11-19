package mage.cards.v;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class VedalkenSquirrelWhacker extends CardImpl {

    public VedalkenSquirrelWhacker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add(SubType.VEDALKEN);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // As this creature enters, roll a six-sided die twice. Its base power becomes the first result and its base toughness becomes the second result.
        this.addAbility(new EntersBattlefieldAbility(
                new VedalkenSquirrelWhackerEntersEffect(),
                null,
                "As {this} enters, roll a six-sided die twice. Its base power becomes the first result and its base toughness becomes the second result.",
                null
        ));
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new VedalkenSquirrelWhackerPTEffect()));

        // If you would roll one or more six-sided dice, instead roll them and you may exchange one result with this creature's base power or base toughness.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "If you would roll one or more six-sided dice, instead roll them and you may exchange one result with this creature's base power or base toughness"
        )));
    }

    private VedalkenSquirrelWhacker(final VedalkenSquirrelWhacker card) {
        super(card);
    }

    @Override
    public VedalkenSquirrelWhacker copy() {
        return new VedalkenSquirrelWhacker(this);
    }
}

class VedalkenSquirrelWhackerEntersEffect extends OneShotEffect {

    VedalkenSquirrelWhackerEntersEffect() {
        super(Outcome.Benefit);
    }

    private VedalkenSquirrelWhackerEntersEffect(final VedalkenSquirrelWhackerEntersEffect effect) {
        super(effect);
    }

    @Override
    public VedalkenSquirrelWhackerEntersEffect copy() {
        return new VedalkenSquirrelWhackerEntersEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        List<Integer> rolls = controller.rollDice(Outcome.Benefit, source, game, 6, 2, 0);
        if (!rolls.isEmpty()) {
            int p = rolls.get(0);
            int t = rolls.size() > 1 ? rolls.get(1) : p;
            game.getState().setValue("vsw_p_" + source.getSourceId(), p);
            game.getState().setValue("vsw_t_" + source.getSourceId(), t);
        }
        return true;
    }
}

class VedalkenSquirrelWhackerPTEffect extends ContinuousEffectImpl {

    VedalkenSquirrelWhackerPTEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.SetPT_7b, Outcome.Neutral);
    }

    private VedalkenSquirrelWhackerPTEffect(final VedalkenSquirrelWhackerPTEffect effect) {
        super(effect);
    }

    @Override
    public VedalkenSquirrelWhackerPTEffect copy() {
        return new VedalkenSquirrelWhackerPTEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        Integer p = (Integer) game.getState().getValue("vsw_p_" + source.getSourceId());
        Integer t = (Integer) game.getState().getValue("vsw_t_" + source.getSourceId());
        if (p != null) {
            permanent.getPower().setModifiedBaseValue(p);
        }
        if (t != null) {
            permanent.getToughness().setModifiedBaseValue(t);
        }
        return true;
    }
}
