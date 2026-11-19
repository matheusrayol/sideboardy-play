package mage.cards.c;

import mage.MageObject;
import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.SimpleManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceColor;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class CrypticSpires extends CardImpl {

    public CrypticSpires(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // As you create your deck, circle two of the colors below.
        // Cryptic Spires enters tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());
        this.addAbility(new AsEntersBattlefieldAbility(new CrypticSpiresChooseColorsEffect()));

        // {T}: Add one mana of either of the circled colors.
        this.addAbility(new SimpleManaAbility(Zone.BATTLEFIELD, new CrypticSpiresManaEffect(), new TapSourceCost()));
    }

    private CrypticSpires(final CrypticSpires card) {
        super(card);
    }

    @Override
    public CrypticSpires copy() {
        return new CrypticSpires(this);
    }
}

class CrypticSpiresChooseColorsEffect extends OneShotEffect {

    CrypticSpiresChooseColorsEffect() {
        super(Outcome.PutManaInPool);
        staticText = "choose two different colors (circled colors)";
    }

    private CrypticSpiresChooseColorsEffect(final CrypticSpiresChooseColorsEffect effect) {
        super(effect);
    }

    @Override
    public CrypticSpiresChooseColorsEffect copy() {
        return new CrypticSpiresChooseColorsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject mageObject = game.getPermanentEntering(source.getSourceId());
        if (mageObject == null) {
            mageObject = game.getObject(source);
        }
        if (controller == null || mageObject == null) {
            return false;
        }
        ChoiceColor choice1 = new ChoiceColor();
        choice1.setMessage("Choose first circled color for " + mageObject.getLogName());
        if (!controller.choose(Outcome.PutManaInPool, choice1, game) || choice1.getColor() == null) {
            return false;
        }
        ChoiceColor choice2 = new ChoiceColor();
        choice2.setMessage("Choose second circled color for " + mageObject.getLogName());
        choice2.removeColorFromChoices(choice1.getChoice());
        if (!controller.choose(Outcome.PutManaInPool, choice2, game) || choice2.getColor() == null) {
            return false;
        }
        game.getState().setValue(mageObject.getId() + "_color1", choice1.getColor());
        game.getState().setValue(mageObject.getId() + "_color2", choice2.getColor());
        game.informPlayers(mageObject.getLogName() + ": " + controller.getLogName() + " chose " + choice1.getChoice() + " and " + choice2.getChoice());
        if (mageObject instanceof Permanent) {
            ((Permanent) mageObject).addInfo(
                    "circled colors",
                    CardUtil.addToolTipMarkTags("Circled colors: " + choice1.getChoice() + ", " + choice2.getChoice()),
                    game
            );
        }
        return true;
    }
}

class CrypticSpiresManaEffect extends ManaEffect {

    CrypticSpiresManaEffect() {
        super();
        staticText = "Add one mana of either of the circled colors";
    }

    private CrypticSpiresManaEffect(final CrypticSpiresManaEffect effect) {
        super(effect);
    }

    @Override
    public CrypticSpiresManaEffect copy() {
        return new CrypticSpiresManaEffect(this);
    }

    @Override
    public List<Mana> getNetMana(Game game, Ability source) {
        List<Mana> netMana = new ArrayList<>();
        if (game == null || source == null) {
            return netMana;
        }
        ObjectColor c1 = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color1");
        ObjectColor c2 = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color2");
        if (c1 != null) {
            netMana.add(new Mana(ColoredManaSymbol.lookup(c1.toString().charAt(0))));
        }
        if (c2 != null) {
            netMana.add(new Mana(ColoredManaSymbol.lookup(c2.toString().charAt(0))));
        }
        return netMana;
    }

    @Override
    public Mana produceMana(Game game, Ability source) {
        if (game == null) {
            return new Mana();
        }
        Player controller = game.getPlayer(source.getControllerId());
        ObjectColor c1 = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color1");
        ObjectColor c2 = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color2");
        if (controller == null || c1 == null || c2 == null) {
            return new Mana();
        }
        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose mana color");
        choice.getChoices().add(c1.getDescription());
        choice.getChoices().add(c2.getDescription());
        if (!controller.choose(Outcome.PutManaInPool, choice, game)) {
            return new Mana();
        }
        ObjectColor chosen = c1.getDescription().equals(choice.getChoice()) ? c1 : c2;
        return new Mana(ColoredManaSymbol.lookup(chosen.toString().charAt(0)));
    }
}
