package mage.cards.n;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;

/**
 *
 * @author muz
 */
public final class NyotaUhuraCommunications extends CardImpl {

    public NyotaUhuraCommunications(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Hailing Frequencies — As Nyota Uhura enters, choose a number between 0 and 10.
        this.addAbility(new AsEntersBattlefieldAbility(new NyotaUhuraChooseNumberEffect())
                .withFlavorWord("Hailing Frequencies"));

        // Open a Channel — Whenever an opponent casts a spell with mana value equal to the chosen number, you draw a card.
        this.addAbility(new NyotaUhuraTriggeredAbility());
    }

    private NyotaUhuraCommunications(final NyotaUhuraCommunications card) {
        super(card);
    }

    @Override
    public NyotaUhuraCommunications copy() {
        return new NyotaUhuraCommunications(this);
    }
}

class NyotaUhuraChooseNumberEffect extends OneShotEffect {

    NyotaUhuraChooseNumberEffect() {
        super(Outcome.Benefit);
        staticText = "choose a number between 0 and 10";
    }

    private NyotaUhuraChooseNumberEffect(final NyotaUhuraChooseNumberEffect effect) {
        super(effect);
    }

    @Override
    public NyotaUhuraChooseNumberEffect copy() {
        return new NyotaUhuraChooseNumberEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int numberChoice = controller.getAmount(0, 10, "Choose a number between 0 and 10", source, game);
        game.getState().setValue(source.getSourceId().toString() + "_number", numberChoice);
        Permanent permanent = game.getPermanentEntering(source.getSourceId());
        if (permanent != null) {
            permanent.addInfo("chosen number", "<font color = 'blue'>Chosen Number: " + numberChoice + "</font>", game);
            game.informPlayers(permanent.getLogName() + ", chosen number: " + numberChoice);
        }
        return true;
    }
}

class NyotaUhuraTriggeredAbility extends TriggeredAbilityImpl {

    NyotaUhuraTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawCardSourceControllerEffect(1), false);
        setTriggerPhrase("Whenever an opponent casts a spell with mana value equal to the chosen number, ");
        withFlavorWord("Open a Channel");
    }

    private NyotaUhuraTriggeredAbility(final NyotaUhuraTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public NyotaUhuraTriggeredAbility copy() {
        return new NyotaUhuraTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getOpponents(getControllerId()).contains(event.getPlayerId())) {
            return false;
        }
        Integer chosen = (Integer) game.getState().getValue(getSourceId().toString() + "_number");
        Spell spell = game.getStack().getSpell(event.getTargetId());
        return chosen != null && spell != null && spell.getManaValue() == chosen;
    }
}
