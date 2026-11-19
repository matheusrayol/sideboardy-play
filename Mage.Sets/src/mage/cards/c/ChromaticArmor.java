package mage.cards.c;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.EntersBattlefieldWithCountersAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.PreventionEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class ChromaticArmor extends CardImpl {

    public ChromaticArmor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{W}{U}");

        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // As this Aura enters, choose a color.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseColorEffect(Outcome.Benefit)));

        // This Aura enters with a sleight counter on it.
        this.addAbility(new EntersBattlefieldWithCountersAbility(new Counter("sleight")));

        // Prevent all damage that would be dealt to enchanted creature by sources of the last chosen color.
        this.addAbility(new SimpleStaticAbility(new ChromaticArmorPreventionEffect()));

        // {X}: Put a sleight counter on this Aura and choose a color. X is the number of sleight counters on this Aura.
        SimpleActivatedAbility ability = new ChromaticArmorActivatedAbility();
        this.addAbility(ability);
    }

    private ChromaticArmor(final ChromaticArmor card) {
        super(card);
    }

    @Override
    public ChromaticArmor copy() {
        return new ChromaticArmor(this);
    }
}

class ChromaticArmorActivatedAbility extends SimpleActivatedAbility {

    ChromaticArmorActivatedAbility() {
        super(new AddCountersSourceEffect(new Counter("sleight")), new ManaCostsImpl<>("{0}"));
        this.addEffect(new ChooseColorEffect(Outcome.Benefit));
        this.setCostAdjuster(ChromaticArmorCostAdjuster.instance);
    }

    private ChromaticArmorActivatedAbility(final ChromaticArmorActivatedAbility ability) {
        super(ability);
    }

    @Override
    public ChromaticArmorActivatedAbility copy() {
        return new ChromaticArmorActivatedAbility(this);
    }

    @Override
    public String getRule() {
        return "{X}: Put a sleight counter on {this} and choose a color. X is the number of sleight counters on {this}.";
    }
}

enum ChromaticArmorCostAdjuster implements CostAdjuster {
    instance;

    @Override
    public void prepareCost(Ability ability, Game game) {
        Permanent perm = ability.getSourcePermanentOrLKI(game);
        int x = perm != null ? perm.getCounters(game).getCount("sleight") : 0;
        ability.clearManaCostsToPay();
        if (x > 0) {
            ability.addManaCostsToPay(new GenericManaCost(x));
        }
    }
}

class ChromaticArmorPreventionEffect extends PreventionEffectImpl {

    ChromaticArmorPreventionEffect() {
        super(Duration.WhileOnBattlefield, Integer.MAX_VALUE, false);
        this.staticText = "prevent all damage that would be dealt to enchanted creature by sources of the last chosen color";
    }

    private ChromaticArmorPreventionEffect(final ChromaticArmorPreventionEffect effect) {
        super(effect);
    }

    @Override
    public ChromaticArmorPreventionEffect copy() {
        return new ChromaticArmorPreventionEffect(this);
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!super.applies(event, source, game)) {
            return false;
        }
        Permanent aura = game.getPermanent(source.getSourceId());
        if (aura == null || !event.getTargetId().equals(aura.getAttachedTo())) {
            return false;
        }
        ObjectColor chosenColor = (ObjectColor) game.getState().getValue(aura.getId() + "_color");
        if (chosenColor == null) {
            return false;
        }
        MageObject damageSource = game.getPermanentOrLKIBattlefield(event.getSourceId());
        if (damageSource == null) {
            damageSource = game.getObject(event.getSourceId());
        }
        return damageSource != null && damageSource.getColor(game).shares(chosenColor);
    }
}
