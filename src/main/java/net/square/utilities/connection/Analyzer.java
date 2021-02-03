package net.square.utilities.connection;

import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Analyzer {

    @Getter
    private final String address;
    private final JsonParser jsonParser = new JsonParser();

    public Analyzer(String address) {
        this.address = address;
    }

    public boolean checkAddress() throws Exception {
        return this.jsonParser.parse(
            IOUtils.toString(new URL("https://api.iplegit.com/info?ip=" + this.address), StandardCharsets.UTF_8)
        ).getAsJsonObject().get("bad").getAsBoolean();
    }
}