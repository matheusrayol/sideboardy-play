package mage.server.websocket.service.deckimport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web client bridge: public Sideboardy decks read via {@code /api/decks/{id}/play-export}.
 */
final class SideboardySource implements DeckSource {

    private static final Pattern DECK_PATH = Pattern.compile("^/(?:api/)?(?:decks|colecoes)/(\\d+)(?:/.*)?$");

    @Override
    public String id() {
        return "sideboardy";
    }

    @Override
    public List<String> pageHosts() {
        return Arrays.asList("sideboardy.app", "www.sideboardy.app");
    }

    @Override
    public List<String> fetchHosts() {
        return Arrays.asList("sideboardy.app", "www.sideboardy.app");
    }

    @Override
    public String deckId(URI url) {
        Matcher matcher = DECK_PATH.matcher(url.getPath() == null ? "" : url.getPath());
        return matcher.matches() ? matcher.group(1) : null;
    }

    @Override
    public ImportedDeck fetch(URI url, String deckId, Fetcher fetcher) throws DeckImportException {
        String body = fetcher.get(URI.create("https://sideboardy.app/api/decks/" + deckId + "/play-export"), "application/json");
        return read(body, deckId);
    }

    static ImportedDeck read(String body, String deckId) throws DeckImportException {
        JsonObject json = SourceSupport.parseObject(body, "Sideboardy");
        if (!json.has("cards")) {
            throw new DeckImportException(DeckImportException.Reason.SITE_CHANGED, "Sideboardy's deck has no card list");
        }
        ImportedDeck deck = new ImportedDeck("sideboardy", deckId, "https://sideboardy.app/decks/" + deckId);
        deck.setName(SourceSupport.string(json, "name"));
        deck.setAuthor(SourceSupport.string(json, "author"));
        deck.setRemoteUpdatedAt(SourceSupport.isoMillis(SourceSupport.string(json, "updatedAt")));
        String format = SourceSupport.deckType(SourceSupport.string(json, "format"));
        if (format != null) {
            deck.setFormat(format);
        }

        for (JsonElement element : SourceSupport.array(json, "cards")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String name = SourceSupport.cardName(SourceSupport.string(entry, "cardName"));
            String setCode = SourceSupport.string(entry, "setCode");
            String number = SourceSupport.string(entry, "cardNumber");
            int amount = SourceSupport.integer(entry, "amount", 0);
            String sec = SourceSupport.string(entry, "section");
            ImportedDeck.Section section;
            if ("commander".equalsIgnoreCase(sec)) {
                section = ImportedDeck.Section.COMMANDER;
            } else if ("companion".equalsIgnoreCase(sec)) {
                section = ImportedDeck.Section.COMPANION;
            } else if ("side".equalsIgnoreCase(sec)) {
                section = ImportedDeck.Section.SIDE;
            } else if ("maybe".equalsIgnoreCase(sec)) {
                section = ImportedDeck.Section.MAYBE;
            } else {
                section = ImportedDeck.Section.MAIN;
            }
            deck.add(section, name, setCode, number, amount);
        }
        if (deck.cardCount() == 0) {
            throw new DeckImportException(DeckImportException.Reason.NOT_FOUND, "Sideboardy's deck " + deckId + " has no cards");
        }
        return deck;
    }
}
