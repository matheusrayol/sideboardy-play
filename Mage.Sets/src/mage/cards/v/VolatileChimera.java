package mage.cards.v;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CopyEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentCard;
import mage.players.Player;
import mage.util.functions.CopyApplier;

import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class VolatileChimera extends CardImpl {

    public VolatileChimera(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.CHIMERA);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Before you shuffle your deck to start the game, you may reveal this card from your deck and exile three or more creature cards you drafted that aren't in your deck.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new InfoEffect("Before you shuffle your deck to start the game, you may reveal {this} from your deck "
                        + "and exile three or more creature cards you drafted that aren't in your deck - not implemented.")
        ));

        // {1}{R}: Choose a card at random you exiled with cards named Volatile Chimera. This creature becomes a copy of that card, except it has this ability.
        this.addAbility(new SimpleActivatedAbility(new VolatileChimeraEffect(), new ManaCostsImpl<>("{1}{R}")));
    }

    private VolatileChimera(final VolatileChimera card) {
        super(card);
    }

    @Override
    public VolatileChimera copy() {
        return new VolatileChimera(this);
    }
}

class VolatileChimeraEffect extends OneShotEffect {

    VolatileChimeraEffect() {
        super(Outcome.Copy);
        staticText = "Choose a card at random you exiled with cards named {this}. {this} becomes a copy of that card, except it has this ability";
    }

    private VolatileChimeraEffect(final VolatileChimeraEffect effect) {
        super(effect);
    }

    @Override
    public VolatileChimeraEffect copy() {
        return new VolatileChimeraEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (controller == null || permanent == null) {
            return false;
        }
        Object raw = game.getState().getValue(controller.getId() + "_volatile_chimera_exile");
        if (!(raw instanceof Collection<?>) || ((Collection<?>) raw).isEmpty()) {
            return true;
        }
        CardsImpl exiled = new CardsImpl();
        for (Object id : (Collection<?>) raw) {
            if (id instanceof UUID) {
                Card c = game.getCard((UUID) id);
                if (c != null) {
                    exiled.add(c);
                }
            }
        }
        Card chosen = exiled.getRandom(game);
        if (chosen == null) {
            return true;
        }
        Permanent newBluePrint = new PermanentCard(chosen, controller.getId(), game);
        newBluePrint.assignNewId();
        CopyApplier applier = VolatileChimeraCopyApplier.instance;
        applier.apply(game, newBluePrint, source, permanent.getId());
        CopyEffect copyEffect = new CopyEffect(Duration.Custom, newBluePrint, permanent.getId());
        copyEffect.setApplier(applier);
        game.addEffect(copyEffect, source);
        return true;
    }
}

class VolatileChimeraCopyApplier extends CopyApplier {

    static final VolatileChimeraCopyApplier instance = new VolatileChimeraCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID targetObjectId) {
        blueprint.getAbilities().add(new SimpleActivatedAbility(new VolatileChimeraEffect(), new ManaCostsImpl<>("{1}{R}")));
        return true;
    }
}
