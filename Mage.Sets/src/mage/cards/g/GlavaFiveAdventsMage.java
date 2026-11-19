package mage.cards.g;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAbilityTriggeredAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.meta.OrTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterAbility;
import mage.filter.FilterSpell;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.mageobject.PermanentPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class GlavaFiveAdventsMage extends CardImpl {

    private static final FilterSpell filterPermanentX = new FilterSpell("a permanent spell with {X} in its mana cost");
    private static final FilterAbility filterAbilityX = new FilterAbility("an ability with {X} in its activation cost that isn't a mana ability");

    static {
        filterPermanentX.add(PermanentPredicate.instance);
        filterPermanentX.add((Predicate<StackObject>) (input, game) ->
                input instanceof Spell && ((Spell) input).getSpellAbility().getManaCosts().containsX());
        filterAbilityX.add((Predicate<Ability>) (input, game) ->
                input != null && !input.isManaAbility()
                        && (input.getManaCosts().containsX() || input.getManaCostsToPay().containsX()));
    }

    public GlavaFiveAdventsMage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Whenever you cast a permanent spell with {X} in its mana cost or activate an ability with {X} in its activation cost that isn't a mana ability, you may have the value of X become 5. Do this only once each turn.
        this.addAbility(new OrTriggeredAbility(
                Zone.BATTLEFIELD,
                new GlavaFiveAdventsMageEffect(),
                false,
                "Whenever you cast a permanent spell with {X} in its mana cost or activate an ability with {X} in its activation cost that isn't a mana ability, ",
                new SpellCastControllerTriggeredAbility(null, filterPermanentX, false, SetTargetPointer.SPELL),
                new ActivateAbilityTriggeredAbility(null, filterAbilityX, SetTargetPointer.SPELL)
        ));
    }

    private GlavaFiveAdventsMage(final GlavaFiveAdventsMage card) {
        super(card);
    }

    @Override
    public GlavaFiveAdventsMage copy() {
        return new GlavaFiveAdventsMage(this);
    }
}

class GlavaFiveAdventsMageEffect extends OneShotEffect {

    GlavaFiveAdventsMageEffect() {
        super(Outcome.Benefit);
        this.staticText = "you may have the value of X become 5. Do this only once each turn";
    }

    private GlavaFiveAdventsMageEffect(final GlavaFiveAdventsMageEffect effect) {
        super(effect);
    }

    @Override
    public GlavaFiveAdventsMageEffect copy() {
        return new GlavaFiveAdventsMageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        String key = "GlavaFiveAdventsUsed_" + game.getTurnNum() + "_" + new MageObjectReference(source.getSourceId(), source.getStackMomentSourceZCC(), game);
        if (Boolean.TRUE.equals(game.getState().getValue(key))) {
            return false;
        }
        StackObject stackObject = game.getStack().getStackObject(getTargetPointer().getFirst(game, source));
        if (stackObject == null || stackObject.getStackAbility() == null) {
            return false;
        }
        if (!controller.chooseUse(Outcome.Benefit, "Have the value of X become 5 for " + stackObject.getName() + "?", source, game)) {
            return false;
        }
        game.getState().setValue(key, Boolean.TRUE);
        CardUtil.getSourceCostsTagsMap(game, stackObject.getStackAbility()).put("X", 5);
        return true;
    }
}
