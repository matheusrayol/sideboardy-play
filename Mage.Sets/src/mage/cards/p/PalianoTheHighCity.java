package mage.cards.p;

import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.SimpleManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.ColoredManaSymbol;
import mage.constants.Outcome;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class PalianoTheHighCity extends CardImpl {

    public PalianoTheHighCity(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.supertype.add(SuperType.LEGENDARY);

        // Reveal Paliano, the High City as you draft it. The player to your right chooses a color, you choose another color, then the player to your left chooses a third color.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Reveal {this} as you draft it. The player to your right chooses a color, "
                        + "you choose another color, then the player to your left chooses a third color - not implemented.")
        ));

        // {T}: Add one mana of any color chosen as you drafted cards named Paliano, the High City.
        this.addAbility(new SimpleManaAbility(Zone.BATTLEFIELD, new PalianoTheHighCityManaEffect(), new TapSourceCost()));
    }

    private PalianoTheHighCity(final PalianoTheHighCity card) {
        super(card);
    }

    @Override
    public PalianoTheHighCity copy() {
        return new PalianoTheHighCity(this);
    }
}

class PalianoTheHighCityManaEffect extends ManaEffect {

    PalianoTheHighCityManaEffect() {
        super();
        staticText = "Add one mana of any color chosen as you drafted cards named {this}";
    }

    private PalianoTheHighCityManaEffect(final PalianoTheHighCityManaEffect effect) {
        super(effect);
    }

    @Override
    public PalianoTheHighCityManaEffect copy() {
        return new PalianoTheHighCityManaEffect(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Mana> getNetMana(Game game, Ability source) {
        List<Mana> result = new ArrayList<>();
        if (game == null || source == null) {
            return result;
        }
        Object raw = game.getState().getValue(source.getSourceId() + "_palianoColors");
        if (raw instanceof List<?>) {
            for (Object item : (List<?>) raw) {
                if (item instanceof ObjectColor) {
                    result.add(new Mana(ColoredManaSymbol.lookup(item.toString().charAt(0))));
                }
            }
        }
        return result;
    }

    @Override
    public Mana produceMana(Game game, Ability source) {
        if (game == null) {
            return new Mana();
        }
        Player controller = game.getPlayer(source.getControllerId());
        Object raw = game.getState().getValue(source.getSourceId() + "_palianoColors");
        if (controller == null || !(raw instanceof List<?>) || ((List<?>) raw).isEmpty()) {
            return new Mana();
        }
        List<ObjectColor> colors = new ArrayList<>();
        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose mana color");
        for (Object item : (List<?>) raw) {
            if (item instanceof ObjectColor) {
                ObjectColor color = (ObjectColor) item;
                colors.add(color);
                choice.getChoices().add(color.getDescription());
            }
        }
        if (colors.isEmpty()) {
            return new Mana();
        }
        if (colors.size() == 1) {
            return new Mana(ColoredManaSymbol.lookup(colors.get(0).toString().charAt(0)));
        }
        if (!controller.choose(Outcome.PutManaInPool, choice, game)) {
            return new Mana();
        }
        for (ObjectColor c : colors) {
            if (c.getDescription().equals(choice.getChoice())) {
                return new Mana(ColoredManaSymbol.lookup(c.toString().charAt(0)));
            }
        }
        return new Mana();
    }
}
