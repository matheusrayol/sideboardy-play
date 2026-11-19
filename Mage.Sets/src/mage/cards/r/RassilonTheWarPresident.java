package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.LoseLifeSourceControllerEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.abilities.keyword.ConspireAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class RassilonTheWarPresident extends CardImpl {

    public RassilonTheWarPresident(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.subtype.add(SubType.NOBLE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // At the beginning of your upkeep, you lose 2 life and exile the top card of your library. You may play that card for as long as it remains exiled.
        Ability ability = new BeginningOfUpkeepTriggeredAbility(new LoseLifeSourceControllerEffect(2));
        ability.addEffect(new RassilonExileEffect());
        this.addAbility(ability);

        // Each noncreature spell you cast from exile has conspire.
        this.addAbility(new SimpleStaticAbility(new RassilonGainConspireEffect()));
    }

    private RassilonTheWarPresident(final RassilonTheWarPresident card) {
        super(card);
    }

    @Override
    public RassilonTheWarPresident copy() {
        return new RassilonTheWarPresident(this);
    }
}

class RassilonExileEffect extends OneShotEffect {

    RassilonExileEffect() {
        super(Outcome.Benefit);
        this.staticText = "and exile the top card of your library. You may play that card for as long as it remains exiled";
    }

    private RassilonExileEffect(final RassilonExileEffect effect) {
        super(effect);
    }

    @Override
    public RassilonExileEffect copy() {
        return new RassilonExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Card card = controller.getLibrary().getFromTop(game);
        if (card == null) {
            return true;
        }
        controller.moveCardsToExile(
                card, source, game, true,
                CardUtil.getExileZoneId(game, source),
                CardUtil.getSourceName(game, source)
        );
        game.addEffect(new PlayFromNotOwnHandZoneTargetEffect(Zone.EXILED, Duration.Custom)
                .setTargetPointer(new FixedTarget(card, game)), source);
        return true;
    }
}

class RassilonGainConspireEffect extends ContinuousEffectImpl {

    private final ConspireAbility conspireAbility;

    RassilonGainConspireEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Each noncreature spell you cast from exile has conspire";
        this.conspireAbility = new ConspireAbility(ConspireAbility.ConspireTargets.MORE);
    }

    private RassilonGainConspireEffect(final RassilonGainConspireEffect effect) {
        super(effect);
        this.conspireAbility = effect.conspireAbility;
    }

    @Override
    public RassilonGainConspireEffect copy() {
        return new RassilonGainConspireEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (StackObject stackObject : game.getStack()) {
            if (!(stackObject instanceof Spell) || !stackObject.isControlledBy(source.getControllerId())) {
                continue;
            }
            Spell spell = (Spell) stackObject;
            if (!spell.wasCast() || spell.getFromZone() != Zone.EXILED || spell.isCreature(game)) {
                continue;
            }
            game.getState().addOtherAbility(spell.getCard(), conspireAbility.setAddedById(source.getSourceId()));
        }
        return true;
    }
}
