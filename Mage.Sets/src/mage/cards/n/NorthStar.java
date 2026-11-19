package mage.cards.n;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.AsThoughManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.ManaPoolItem;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class NorthStar extends CardImpl {

    public NorthStar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // {4}, {T}: For one spell this turn, you may spend mana as though it were mana of any type to pay that spell's mana cost. (Additional costs are still paid normally.)
        Ability ability = new SimpleActivatedAbility(new NorthStarEffect(), new GenericManaCost(4));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private NorthStar(final NorthStar card) {
        super(card);
    }

    @Override
    public NorthStar copy() {
        return new NorthStar(this);
    }
}

class NorthStarEffect extends AsThoughEffectImpl implements AsThoughManaEffect {

    private MageObjectReference lockedSpellRef;
    private MageObjectReference pendingSpellRef;

    NorthStarEffect() {
        super(AsThoughEffectType.SPEND_OTHER_MANA, Duration.EndOfTurn, Outcome.Benefit);
        staticText = "For one spell this turn, you may spend mana as though it were mana of any type to pay that spell's mana cost";
    }

    private NorthStarEffect(final NorthStarEffect effect) {
        super(effect);
        this.lockedSpellRef = effect.lockedSpellRef;
        this.pendingSpellRef = effect.pendingSpellRef;
    }

    @Override
    public NorthStarEffect copy() {
        return new NorthStarEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability affectedAbility, Ability source, Game game, UUID playerId) {
        if (!source.isControlledBy(playerId) || !(affectedAbility instanceof SpellAbility)) {
            return false;
        }
        if (lockedSpellRef != null) {
            return lockedSpellRef.refersTo(objectId, game);
        }
        if (!game.inCheckPlayableState() && Zone.STACK.equals(game.getState().getZone(objectId))) {
            pendingSpellRef = new MageObjectReference(objectId, game);
        }
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        return false;
    }

    @Override
    public ManaType getAsThoughManaType(ManaType manaType, ManaPoolItem mana, UUID affectedControllerId, Ability source, Game game) {
        if (mana.get(manaType) > 0) {
            return manaType;
        }
        if (!game.inCheckPlayableState() && lockedSpellRef == null && pendingSpellRef != null) {
            lockedSpellRef = pendingSpellRef;
        }
        return mana.getFirstAvailable();
    }
}
